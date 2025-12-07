package pe.unmsm.crm.marketing.segmentacion.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.unmsm.crm.marketing.segmentacion.domain.model.Segmento;
import pe.unmsm.crm.marketing.segmentacion.domain.repository.SegmentoRepository;
import pe.unmsm.crm.marketing.segmentacion.infra.persistence.JpaSegmentoMiembroRepository;
import pe.unmsm.crm.marketing.segmentacion.infra.persistence.JpaSegmentoRepository;
import pe.unmsm.crm.marketing.segmentacion.infra.persistence.SegmentoMiembroBatchRepository;
import pe.unmsm.crm.marketing.shared.logging.AuditoriaService;
import pe.unmsm.crm.marketing.shared.logging.ModuloLog;
import pe.unmsm.crm.marketing.shared.logging.AccionLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SegmentoService {

    private final SegmentoRepository segmentoRepository;
    private final JpaSegmentoRepository jpaSegmentoRepository;
    private final LeadServicePort leadServicePort;
    private final ClienteServicePort clienteServicePort;
    private final JpaSegmentoMiembroRepository miembroRepository;
    private final SegmentoMiembroBatchRepository batchRepository;
    private final AuditoriaService auditoriaService;

    public SegmentoService(SegmentoRepository segmentoRepository,
            JpaSegmentoRepository jpaSegmentoRepository,
            LeadServicePort leadServicePort,
            ClienteServicePort clienteServicePort,
            JpaSegmentoMiembroRepository miembroRepository,
            SegmentoMiembroBatchRepository batchRepository,
            AuditoriaService auditoriaService) {
        this.segmentoRepository = segmentoRepository;
        this.jpaSegmentoRepository = jpaSegmentoRepository;
        this.leadServicePort = leadServicePort;
        this.clienteServicePort = clienteServicePort;
        this.miembroRepository = miembroRepository;
        this.batchRepository = batchRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public Segmento crearSegmento(Segmento segmento) {
        // Guardar el segmento
        Segmento savedSegmento = segmentoRepository.save(segmento);

        // AUDITORÍA: Registrar creación
        auditoriaService.registrarEvento(
                ModuloLog.SEGMENTOS,
                AccionLog.CREAR,
                savedSegmento.getId(),
                null, // TODO: Agregar ID de usuario cuando esté disponible
                String.format("Segmento '%s' creado con tipo de audiencia %s",
                        savedSegmento.getNombre(), savedSegmento.getTipoAudiencia()));

        // Materialize automatically on creation
        System.out.println("✓ Segmento creado ID: " + savedSegmento.getId() + " - Materializando automáticamente...");
        materializarSegmento(savedSegmento.getId());

        return savedSegmento;
    }

    @Transactional
    public Segmento actualizarSegmento(Long id, Segmento segmento) {
        return segmentoRepository.findById(id).map(existing -> {
            String nombreAnterior = existing.getNombre();
            existing.setNombre(segmento.getNombre());
            existing.setDescripcion(segmento.getDescripcion());
            existing.setEstado(segmento.getEstado());
            existing.setReglaPrincipal(segmento.getReglaPrincipal());
            existing.actualizarFecha();

            Segmento updated = segmentoRepository.save(existing);

            // AUDITORÍA: Registrar actualización
            auditoriaService.registrarEvento(
                    ModuloLog.SEGMENTOS,
                    AccionLog.ACTUALIZAR,
                    id,
                    null, // TODO: Agregar ID de usuario
                    String.format("Segmento actualizado: '%s' -> '%s'. Rematerializando...",
                            nombreAnterior, updated.getNombre()));

            // Re-materialize automatically to update members based on new filters
            System.out.println(
                    "✓ Segmento actualizado ID: " + updated.getId() + " - Rematerializando automáticamente...");
            // Use the updated object directly to ensure we use the latest rules
            materializarSegmento(updated);

            return updated;
        }).orElseThrow(() -> new RuntimeException("Segmento no encontrado"));
    }

    /**
     * Actualiza solo campos básicos del segmento (nombre, descripción, estado)
     * SIN rematerializar. Usado para ediciones rápidas.
     */
    @Transactional
    public Segmento actualizarSegmentoBasico(Long id, String nombre, String descripcion, String estado) {
        return segmentoRepository.findById(id).map(existing -> {
            if (nombre != null) {
                existing.setNombre(nombre);
            }
            if (descripcion != null) {
                existing.setDescripcion(descripcion);
            }
            if (estado != null) {
                existing.setEstado(estado);
            }
            existing.actualizarFecha();

            Segmento updated = segmentoRepository.save(existing);

            // AUDITORÍA: Registrar actualización básica
            auditoriaService.registrarEvento(
                    ModuloLog.SEGMENTOS,
                    AccionLog.ACTUALIZAR,
                    id,
                    null, // TODO: Agregar ID de usuario
                    String.format("Actualización básica (sin rematerializar): nombre='%s', estado='%s'",
                            updated.getNombre(), updated.getEstado()));

            System.out.println("✓ Segmento actualizado (básico) ID: " + updated.getId() + " - Sin rematerialización");

            return updated;
        }).orElseThrow(() -> new RuntimeException("Segmento no encontrado"));
    }

    public Optional<Segmento> obtenerSegmento(Long id) {
        return segmentoRepository.findById(id);
    }

    public List<Segmento> listarSegmentos() {
        return segmentoRepository.findAll().stream()
                .filter(s -> !"ELIMINADO".equals(s.getEstado()))
                .collect(java.util.stream.Collectors.toList());
    }

    public List<Segmento> listarTodosLosSegmentos() {
        // Return ALL segments including ELIMINADO
        return segmentoRepository.findAll();
    }

    public List<Segmento> listarSegmentosPorEstado(String estado) {
        return segmentoRepository.findAll().stream()
                .filter(s -> estado.equals(s.getEstado()))
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional
    public void eliminarSegmento(Long id) {
        segmentoRepository.findById(id).ifPresent(segmento -> {
            String nombreSegmento = segmento.getNombre();
            segmento.setEstado("ELIMINADO");
            segmento.actualizarFecha();
            segmentoRepository.save(segmento);

            // AUDITORÍA: Registrar eliminación lógica
            auditoriaService.registrarEvento(
                    ModuloLog.SEGMENTOS,
                    AccionLog.ELIMINAR,
                    id,
                    null, // TODO: Agregar ID de usuario
                    String.format("Segmento '%s' marcado como ELIMINADO", nombreSegmento));
        });
    }

    @Transactional
    public void materializarSegmento(Long id) {
        Segmento segmento = segmentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Segmento no encontrado"));
        materializarSegmento(segmento);
    }

    @Transactional
    public void materializarSegmento(Segmento segmento) {
        Long id = segmento.getId();
        String tipoAudiencia = segmento.getTipoAudiencia();
        System.out.println("=== Materializando segmento ID: " + id + " (Audiencia: " + tipoAudiencia + ") ===");

        // Obtener IDs según el tipo de audiencia
        List<Long> memberIds;
        if ("CLIENTE".equals(tipoAudiencia)) {
            memberIds = clienteServicePort.findClientesBySegmento(segmento);
            System.out.println("Clientes encontrados: " + memberIds.size());
        } else if ("LEAD".equals(tipoAudiencia)) {
            memberIds = leadServicePort.findLeadsBySegmento(segmento);
            System.out.println("Leads encontrados: " + memberIds.size());
        } else {
            // MIXTO: combinar ambos (por ahora solo leads, se puede mejorar)
            memberIds = leadServicePort.findLeadsBySegmento(segmento);
            System.out.println("Leads encontrados (MIXTO): " + memberIds.size());
        }

        // Limpiar miembros existentes usando JDBC
        batchRepository.deleteByIdSegmento(id);

        if (memberIds.isEmpty()) {
            System.out.println("⚠ No hay miembros que cumplan con los filtros");

            // AUDITORÍA: Registrar materialización vacía
            auditoriaService.registrarEvento(
                    ModuloLog.SEGMENTOS,
                    AccionLog.CAMBIAR_ESTADO,
                    id,
                    null, // TODO: Agregar ID de usuario
                    String.format("Segmento '%s' materializado: 0 miembros (audiencia: %s)",
                            segmento.getNombre(), tipoAudiencia));
            return;
        }

        // JDBC BATCH INSERT: Insertar todos de una vez (1 query real)
        String tipoMiembro = segmento.getTipoAudiencia();
        LocalDateTime ahora = LocalDateTime.now();

        batchRepository.batchInsertMiembros(id, tipoMiembro, memberIds, ahora);

        // Actualizar cantidad de miembros en el segmento (OPTIMIZADO: solo UPDATE, sin
        // tocar filtros)
        jpaSegmentoRepository.updateCantidadMiembros(id, memberIds.size());

        // AUDITORÍA: Registrar materialización exitosa
        auditoriaService.registrarEvento(
                ModuloLog.SEGMENTOS,
                AccionLog.CAMBIAR_ESTADO,
                id,
                null, // TODO: Agregar ID de usuario
                String.format("Segmento '%s' materializado exitosamente: %d miembros (audiencia: %s)",
                        segmento.getNombre(), memberIds.size(), tipoAudiencia));

        System.out.println("✓ Segmento materializado con " + memberIds.size() + " miembros");
    }

    /**
     * Obtener los IDs de los miembros de un segmento
     * Útil para campañas que necesitan saber a quién enviar mensajes
     */
    public List<Long> obtenerMiembrosSegmento(Long idSegmento) {
        return miembroRepository.findByIdSegmento(idSegmento).stream()
                .map(miembro -> miembro.getIdMiembro())
                .collect(java.util.stream.Collectors.toList());
    }
}
