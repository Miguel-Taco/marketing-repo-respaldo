package pe.unmsm.crm.marketing.campanas.telefonicas.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.unmsm.crm.marketing.campanas.telefonicas.api.dto.GuionArchivoDTO;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.model.GuionArchivo;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.repository.GuionArchivoRepository;
import pe.unmsm.crm.marketing.shared.services.SupabaseStorageService;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.entity.GuionEntity;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de aplicación para gestionar archivos de guiones.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GuionArchivoService {

    private static final String BUCKET_NAME = "guiones";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final GuionArchivoRepository repository;
    private final SupabaseStorageService storageService;
    private final GuionService guionService;

    /**
     * Sube un guión general para una campaña.
     */
    @Transactional
    public GuionArchivoDTO subirGuionGeneral(Long idCampania, MultipartFile file, Long idUsuario) throws IOException {
        return subirGuionGeneral(idCampania, file, idUsuario, null);
    }

    /**
     * Vincula un guión estructurado existente a una campaña.
     * Genera el archivo markdown y lo sube a Supabase.
     */
    @Transactional
    public GuionArchivoDTO vincularGuionACampaña(Long idCampania, Integer idGuion, Long idUsuario) {
        // 1. Obtener el guión estructurado
        pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.entity.GuionEntity guionEntity = guionService
                .obtenerEntidadGuion(idGuion);

        // 2. Generar contenido Markdown
        String markdownContent = guionService.generarMarkdown(guionEntity);
        byte[] contentBytes = markdownContent.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        // 3. Definir nombre de archivo
        String filename = guionEntity.getNombre().replaceAll("[^a-zA-Z0-9.-]", "_") + ".md";
        String path = buildPath(idCampania, null, filename);

        // 4. Subir a Supabase
        storageService.uploadFile(BUCKET_NAME, path, contentBytes, "text/markdown");

        // 5. Guardar metadata en GuionArchivo
        GuionArchivo guionArchivo = GuionArchivo.builder()
                .idCampania(idCampania)
                .idAgente(null) // General
                .nombre(filename)
                .descripcion(guionEntity.getObjetivo()) // Usar objetivo como descripción
                .rutaSupabase(path)
                .tipoArchivo("md")
                .creadoPor(idUsuario)
                .estado("ACTIVO")
                .activo(true)
                .build();

        guionArchivo = repository.save(guionArchivo);
        log.info("Guión vinculado: {} para campaña {}", filename, idCampania);

        return toDTO(guionArchivo);
    }

    /**
     * Sube un guión general para una campaña con descripción opcional.
     */
    @Transactional
    public GuionArchivoDTO subirGuionGeneral(Long idCampania, MultipartFile file, Long idUsuario, String descripcion)
            throws IOException {
        String path = buildPath(idCampania, null, file.getOriginalFilename());
        return subirGuion(idCampania, null, file, idUsuario, path, descripcion);
    }

    /**
     * Sube un guión específico de un agente para una campaña.
     */
    @Transactional
    public GuionArchivoDTO subirGuionAgente(Long idCampania, Long idAgente, MultipartFile file, Long idUsuario)
            throws IOException {
        return subirGuionAgente(idCampania, idAgente, file, idUsuario, null);
    }

    /**
     * Sube un guión específico de un agente para una campaña con descripción
     * opcional.
     */
    @Transactional
    public GuionArchivoDTO subirGuionAgente(Long idCampania, Long idAgente, MultipartFile file, Long idUsuario,
            String descripcion) throws IOException {
        String path = buildPath(idCampania, idAgente, file.getOriginalFilename());
        return subirGuion(idCampania, idAgente, file, idUsuario, path, descripcion);
    }

    /**
     * Lista los guiones generales de una campaña.
     */
    public List<GuionArchivoDTO> listarGuionesGenerales(Long idCampania) {
        return repository.findByIdCampaniaAndIdAgenteIsNull(idCampania)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lista los guiones de un agente específico en una campaña.
     */
    public List<GuionArchivoDTO> listarGuionesAgente(Long idCampania, Long idAgente) {
        return repository.findByIdCampaniaAndIdAgente(idCampania, idAgente)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Elimina un guión (metadata y archivo físico).
     */
    @Transactional
    public void eliminarGuion(Integer idGuion) {
        GuionArchivo guion = repository.findById(idGuion)
                .orElseThrow(() -> new RuntimeException("Guión no encontrado con ID: " + idGuion));

        // Eliminar archivo de Supabase
        try {
            storageService.deleteFile(BUCKET_NAME, guion.getRutaSupabase());
        } catch (Exception e) {
            log.error("Error al eliminar archivo de Supabase: {}", e.getMessage());
            // Continuar con la eliminación de metadata aunque falle el archivo
        }

        // Eliminar metadata
        repository.deleteById(idGuion);
        log.info("Guión eliminado: {}", idGuion);
    }

    /**
     * Descarga un guión como byte array.
     */
    public byte[] descargarGuion(Integer idGuion) {
        GuionArchivo guion = repository.findById(idGuion)
                .orElseThrow(() -> new RuntimeException("Guión no encontrado con ID: " + idGuion));

        return storageService.downloadFile(BUCKET_NAME, guion.getRutaSupabase());
    }

    /**
     * Obtiene el contenido markdown de un guión como String.
     * Siempre lee desde Supabase Storage.
     */
    public String obtenerContenidoMarkdown(Integer idGuion) {
        GuionArchivo guion = repository.findById(idGuion)
                .orElseThrow(() -> new RuntimeException("Guión no encontrado con ID: " + idGuion));

        return storageService.downloadFileAsString(BUCKET_NAME, guion.getRutaSupabase());
    }

    // ========== Métodos privados ==========

    private GuionArchivoDTO subirGuion(Long idCampania, Long idAgente, MultipartFile file, Long idUsuario, String path,
            String descripcion) throws IOException {
        // Subir archivo a Supabase
        storageService.uploadFile(BUCKET_NAME, path, file);

        // Guardar metadata
        GuionArchivo guion = GuionArchivo.builder()
                .idCampania(idCampania)
                .idAgente(idAgente)
                .nombre(file.getOriginalFilename())
                .descripcion(descripcion)
                .rutaSupabase(path)
                .tipoArchivo("md")
                .creadoPor(idUsuario)
                .estado("BORRADOR")
                .activo(true)
                .build();

        guion = repository.save(guion);
        log.info("Guión guardado: {} para campaña {}", guion.getNombre(), idCampania);

        return toDTO(guion);
    }

    private String buildPath(Long idCampania, Long idAgente, String filename) {
        if (idAgente == null) {
            return String.format("campana/%d/general/%s", idCampania, filename);
        } else {
            return String.format("campana/%d/%d/%s", idCampania, idAgente, filename);
        }
    }

    private GuionArchivoDTO toDTO(GuionArchivo guion) {
        return GuionArchivoDTO.builder()
                .id(guion.getId())
                .idCampania(guion.getIdCampania())
                .idAgente(guion.getIdAgente())
                .nombre(guion.getNombre())
                .descripcion(guion.getDescripcion())
                .tipoArchivo(guion.getTipoArchivo())
                .fechaCreacion(
                        guion.getFechaCreacion() != null ? guion.getFechaCreacion().format(DATE_FORMATTER) : null)
                .fechaModificacion(
                        guion.getFechaModificacion() != null ? guion.getFechaModificacion().format(DATE_FORMATTER)
                                : null)
                .urlDescarga(storageService.getPublicUrl(BUCKET_NAME, guion.getRutaSupabase()))
                .estado(guion.getEstado())
                .activo(guion.getActivo())
                .esGeneral(guion.esGeneral())
                .build();
    }
}
