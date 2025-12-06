package pe.unmsm.crm.marketing.campanas.telefonicas.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.unmsm.crm.marketing.campanas.telefonicas.api.dto.GrabacionDTO;
import pe.unmsm.crm.marketing.campanas.telefonicas.api.dto.SubirGrabacionRequest;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.event.RecordingUploadedEvent;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.event.RecordingDeletedEvent;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.event.TelemarketingEventPublisher;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.entity.AgenteMarketingEntity;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.entity.CampaniaTelefonicaEntity;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.entity.GrabacionLlamadaEntity;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.repository.AgenteMarketingRepository;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.repository.CampaniaTelefonicaRepository;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.repository.GrabacionLlamadaRepository;
import pe.unmsm.crm.marketing.leads.domain.model.Lead;
import pe.unmsm.crm.marketing.leads.domain.repository.LeadRepository;
import pe.unmsm.crm.marketing.shared.services.FirebaseStorageService;
import pe.unmsm.crm.marketing.shared.services.SupabaseStorageService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio principal para gestión de grabaciones de llamadas
 */
@Service
@Slf4j
public class GrabacionService {

    private static final String SUPABASE_BUCKET = "grabaciones_llamada";
    private static final DateTimeFormatter FILENAME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final GrabacionLlamadaRepository grabacionRepository;
    private final AgenteMarketingRepository agenteRepository;
    private final CampaniaTelefonicaRepository campaniaRepository;
    private final LeadRepository leadRepository;
    private final FirebaseStorageService firebaseStorage;
    private final SupabaseStorageService supabaseStorage;
    private final GeminiTranscriptionService geminiService;
    
    // Event publisher OPCIONAL - no falla si no está disponible
    @Autowired(required = false)
    private TelemarketingEventPublisher eventPublisher;

    public GrabacionService(
            GrabacionLlamadaRepository grabacionRepository,
            AgenteMarketingRepository agenteRepository,
            CampaniaTelefonicaRepository campaniaRepository,
            LeadRepository leadRepository,
            FirebaseStorageService firebaseStorage,
            SupabaseStorageService supabaseStorage,
            GeminiTranscriptionService geminiService) {
        this.grabacionRepository = grabacionRepository;
        this.agenteRepository = agenteRepository;
        this.campaniaRepository = campaniaRepository;
        this.leadRepository = leadRepository;
        this.firebaseStorage = firebaseStorage;
        this.supabaseStorage = supabaseStorage;
        this.geminiService = geminiService;
    }
    
    /**
     * Publica un evento si el publisher está disponible
     */
    private void publishEventIfAvailable(Object event) {
        if (eventPublisher != null) {
            try {
                eventPublisher.publish(event);
            } catch (Exception e) {
                log.warn("Error publicando evento {}: {}", event.getClass().getSimpleName(), e.getMessage());
            }
        }
    }

    /**
     * Procesa y guarda una nueva grabación
     */
    @Transactional
    public GrabacionDTO procesarGrabacion(SubirGrabacionRequest request, Integer idAgente) throws IOException {
        // 1. Validar archivo
        validarArchivo(request.getArchivo());

        // 2. Crear registro en BD con estado PENDIENTE
        GrabacionLlamadaEntity grabacion = new GrabacionLlamadaEntity();
        grabacion.setIdCampania(request.getIdCampania());
        grabacion.setIdAgente(idAgente);
        grabacion.setIdLead(request.getIdLead());
        grabacion.setIdLlamada(request.getIdLlamada());
        grabacion.setDuracionSegundos(request.getDuracionSegundos());
        grabacion.setResultado(request.getResultado());
        grabacion.setEstadoProcesamiento(GrabacionLlamadaEntity.EstadoProcesamiento.PENDIENTE);

        // 3. Construir ruta para Firebase
        String timestamp = LocalDateTime.now().format(FILENAME_FORMATTER);
        String rutaFirebase = String.format("grabaciones/%d/%d/%s_%d.mp3",
                request.getIdCampania(),
                idAgente,
                timestamp,
                request.getIdLead());
        grabacion.setRutaAudioFirebase(rutaFirebase);

        grabacion = grabacionRepository.save(grabacion);
        log.info("Grabación creada con ID: {} en estado PENDIENTE", grabacion.getId());

        try {
            // 4. Subir a Firebase Storage
            firebaseStorage.subirAudio(rutaFirebase, request.getArchivo());
            log.info("Audio subido a Firebase Storage: {}", rutaFirebase);

            // 5. Procesar transcripción de forma asíncrona
            procesarTranscripcionAsync(grabacion.getId(), request.getArchivo().getBytes());

            // 6. Publicar evento de grabación subida
            publishEventIfAvailable(new RecordingUploadedEvent(
                grabacion.getId(),
                Long.valueOf(request.getIdCampania()),
                Long.valueOf(idAgente),
                request.getIdLead()
            ));

            // 7. Retornar DTO
            return toDTO(grabacion);

        } catch (Exception e) {
            // Si falla la subida, marcar como ERROR
            grabacion.setEstadoProcesamiento(GrabacionLlamadaEntity.EstadoProcesamiento.ERROR);
            grabacion.setMensajeError(e.getMessage());
            grabacionRepository.save(grabacion);
            throw new IOException("Error al procesar grabación", e);
        }
    }

    /**
     * Procesa la transcripción de forma asíncrona
     */
    @Async
    @Transactional
    public void procesarTranscripcionAsync(Long idGrabacion, byte[] audioBytes) {
        Optional<GrabacionLlamadaEntity> optGrabacion = grabacionRepository.findById(idGrabacion);
        if (optGrabacion.isEmpty()) {
            log.error("Grabación no encontrada: {}", idGrabacion);
            return;
        }

        GrabacionLlamadaEntity grabacion = optGrabacion.get();

        try {
            // Actualizar estado a PROCESANDO
            grabacion.setEstadoProcesamiento(GrabacionLlamadaEntity.EstadoProcesamiento.PROCESANDO);
            grabacion.setIntentosProcesamiento(grabacion.getIntentosProcesamiento() + 1);
            grabacionRepository.save(grabacion);

            // Obtener metadata para el prompt
            Map<String, String> metadata = construirMetadata(grabacion);

            // Transcribir con Gemini
            log.info("Iniciando transcripción con Gemini para grabación: {}", idGrabacion);
            String transcripcion = geminiService.transcribirAudio(audioBytes, metadata);

            // Guardar transcripción en Supabase
            String rutaSupabase = String.format("grabaciones_llamada/%d/%d/%s_%d.md",
                    grabacion.getIdCampania(),
                    grabacion.getIdAgente(),
                    grabacion.getFechaHora().format(FILENAME_FORMATTER),
                    grabacion.getIdLead());

            supabaseStorage.uploadFile(
                    SUPABASE_BUCKET,
                    rutaSupabase,
                    transcripcion.getBytes(StandardCharsets.UTF_8),
                    "text/markdown");
            log.info("Transcripción guardada en Supabase: {}", rutaSupabase);

            // Actualizar grabación con ruta y estado COMPLETADO
            grabacion.setRutaTranscripcionSupabase(rutaSupabase);
            grabacion.setEstadoProcesamiento(GrabacionLlamadaEntity.EstadoProcesamiento.COMPLETADO);
            grabacion.setMensajeError(null);
            grabacionRepository.save(grabacion);

            log.info("Transcripción completada exitosamente para grabación: {}", idGrabacion);

        } catch (Exception e) {
            log.error("Error al procesar transcripción para grabación {}: {}", idGrabacion, e.getMessage());
            grabacion.setEstadoProcesamiento(GrabacionLlamadaEntity.EstadoProcesamiento.ERROR);
            grabacion.setMensajeError(e.getMessage());
            grabacionRepository.save(grabacion);
        }
    }

    /**
     * Lista grabaciones del agente con filtros
     */
    public Page<GrabacionDTO> listarGrabaciones(
            Integer idAgente,
            Integer idCampania,
            String resultado,
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta,
            String busqueda,
            Pageable pageable) {
        Page<GrabacionLlamadaEntity> grabaciones;

        if (busqueda != null && !busqueda.isBlank()) {
            // Búsqueda por nombre o teléfono de lead
            grabaciones = grabacionRepository.searchByLeadInfo(idAgente, busqueda, pageable);
        } else {
            // Filtros múltiples
            grabaciones = grabacionRepository.findByMultipleFilters(
                    idAgente,
                    idCampania,
                    resultado,
                    fechaDesde,
                    fechaHasta,
                    pageable);
        }

        return grabaciones.map(this::toDTO);
    }

    /**
     * Obtiene una grabación específica
     * idAgente puede ser null para admins (sin validación de propiedad)
     */
    public GrabacionDTO obtenerGrabacion(Long idGrabacion, Integer idAgente) {
        GrabacionLlamadaEntity grabacion = grabacionRepository.findById(idGrabacion)
                .orElseThrow(() -> new RuntimeException("Grabación no encontrada"));

        // Verificar que pertenece al agente (skip si idAgente es null - Admin)
        if (idAgente != null && !grabacion.getIdAgente().equals(idAgente)) {
            throw new RuntimeException("Acceso no autorizado a esta grabación");
        }

        return toDTO(grabacion);
    }

    /**
     * Obtiene URL firmada para reproducir el audio
     * idAgente puede ser null para admins (sin validación de propiedad)
     */
    public String obtenerAudioUrl(Long idGrabacion, Integer idAgente) {
        GrabacionLlamadaEntity grabacion = grabacionRepository.findById(idGrabacion)
                .orElseThrow(() -> new RuntimeException("Grabación no encontrada"));

        // Verificar que pertenece al agente (skip si idAgente es null - Admin)
        if (idAgente != null && !grabacion.getIdAgente().equals(idAgente)) {
            throw new RuntimeException("Acceso no autorizado a esta grabación");
        }

        // Generar URL firmada válida por 1 hora
        return firebaseStorage.obtenerUrlFirmada(grabacion.getRutaAudioFirebase(), 1);
    }

    /**
     * Obtiene la transcripción en formato markdown
     * idAgente puede ser null para admins (sin validación de propiedad)
     */
    public String obtenerTranscripcion(Long idGrabacion, Integer idAgente) {
        GrabacionLlamadaEntity grabacion = grabacionRepository.findById(idGrabacion)
                .orElseThrow(() -> new RuntimeException("Grabación no encontrada"));

        // Verificar que pertenece al agente (skip si idAgente es null - Admin)
        if (idAgente != null && !grabacion.getIdAgente().equals(idAgente)) {
            throw new RuntimeException("Acceso no autorizado a esta grabación");
        }

        if (grabacion.getRutaTranscripcionSupabase() == null) {
            throw new RuntimeException("Transcripción no disponible aún");
        }

        return supabaseStorage.downloadFileAsString(SUPABASE_BUCKET, grabacion.getRutaTranscripcionSupabase());
    }

    /**
     * Elimina una grabación (audio y transcripción)
     * idAgente puede ser null para admins (sin validación de propiedad)
     */
    @Transactional
    public void eliminarGrabacion(Long idGrabacion, Integer idAgente) {
        GrabacionLlamadaEntity grabacion = grabacionRepository.findById(idGrabacion)
                .orElseThrow(() -> new RuntimeException("Grabación no encontrada"));

        // Verificar que pertenece al agente (skip si idAgente es null - Admin)
        if (idAgente != null && !grabacion.getIdAgente().equals(idAgente)) {
            throw new RuntimeException("Acceso no autorizado a esta grabación");
        }

        // Eliminar audio de Firebase
        try {
            firebaseStorage.eliminarAudio(grabacion.getRutaAudioFirebase());
        } catch (Exception e) {
            log.error("Error al eliminar audio de Firebase: {}", e.getMessage());
        }

        // Eliminar transcripción de Supabase
        if (grabacion.getRutaTranscripcionSupabase() != null) {
            try {
                supabaseStorage.deleteFile(SUPABASE_BUCKET, grabacion.getRutaTranscripcionSupabase());
            } catch (Exception e) {
                log.error("Error al eliminar transcripción de Supabase: {}", e.getMessage());
            }
        }

        // Eliminar registro de BD
        grabacionRepository.delete(grabacion);
        
        // Publicar evento de grabación eliminada
        publishEventIfAvailable(new RecordingDeletedEvent(idGrabacion));
        
        log.info("Grabación eliminada: {}", idGrabacion);
    }

    // ========== Métodos privados ==========

    private void validarArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("Archivo no puede estar vacío");
        }

        String contentType = archivo.getContentType();
        if (contentType == null || !contentType.startsWith("audio/")) {
            throw new IllegalArgumentException("El archivo debe ser de tipo audio");
        }

        // Máximo 50 MB
        long maxSize = 50 * 1024 * 1024;
        if (archivo.getSize() > maxSize) {
            throw new IllegalArgumentException("El archivo no puede superar 50 MB");
        }
    }

    private Map<String, String> construirMetadata(GrabacionLlamadaEntity grabacion) {
        Map<String, String> metadata = new HashMap<>();

        // Agente
        Optional<AgenteMarketingEntity> agente = agenteRepository.findById(grabacion.getIdAgente());
        metadata.put("nombreAgente", agente.map(AgenteMarketingEntity::getNombre).orElse("N/A"));

        // Campaña
        Optional<CampaniaTelefonicaEntity> campania = campaniaRepository.findById(grabacion.getIdCampania());
        metadata.put("nombreCampania", campania.map(CampaniaTelefonicaEntity::getNombre).orElse("N/A"));

        // Lead
        Optional<Lead> lead = leadRepository.findById(grabacion.getIdLead());
        metadata.put("nombreLead", lead.map(Lead::getNombre).orElse("N/A"));

        // Fecha
        metadata.put("fecha", grabacion.getFechaHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        return metadata;
    }

    private GrabacionDTO toDTO(GrabacionLlamadaEntity entity) {
        GrabacionDTO dto = GrabacionDTO.builder()
                .id(entity.getId())
                .idCampania(entity.getIdCampania())
                .idAgente(entity.getIdAgente())
                .idLead(entity.getIdLead())
                .idLlamada(entity.getIdLlamada())
                .fechaHora(entity.getFechaHora())
                .duracionSegundos(entity.getDuracionSegundos())
                .rutaAudioFirebase(entity.getRutaAudioFirebase())
                .rutaTranscripcionSupabase(entity.getRutaTranscripcionSupabase())
                .estadoProcesamiento(entity.getEstadoProcesamiento().name())
                .resultado(entity.getResultado())
                .mensajeError(entity.getMensajeError())
                .intentosProcesamiento(entity.getIntentosProcesamiento())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();

        // Agregar información adicional si las relaciones están cargadas
        if (entity.getAgente() != null) {
            dto.setNombreAgente(entity.getAgente().getNombre());
        }

        if (entity.getCampania() != null) {
            dto.setNombreCampania(entity.getCampania().getNombre());
        }

        // Obtener información del lead
        leadRepository.findById(entity.getIdLead()).ifPresent(lead -> {
            dto.setNombreLead(lead.getNombre());
            dto.setTelefonoLead(lead.getContacto() != null ? lead.getContacto().getTelefono() : null);
        });

        return dto;
    }
}