package pe.unmsm.crm.marketing.segmentacion.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.unmsm.crm.marketing.segmentacion.domain.model.Segmento;
import pe.unmsm.crm.marketing.segmentacion.domain.repository.SegmentoRepository;
import pe.unmsm.crm.marketing.segmentacion.infra.persistence.JpaSegmentoMiembroRepository;
import pe.unmsm.crm.marketing.segmentacion.infra.persistence.SegmentoMiembroBatchRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SegmentoService {

    private final SegmentoRepository segmentoRepository;
    private final LeadServicePort leadServicePort;
    private final JpaSegmentoMiembroRepository miembroRepository;
    private final SegmentoMiembroBatchRepository batchRepository;

    public SegmentoService(SegmentoRepository segmentoRepository,
            LeadServicePort leadServicePort,
            JpaSegmentoMiembroRepository miembroRepository,
            SegmentoMiembroBatchRepository batchRepository) {
        this.segmentoRepository = segmentoRepository;
        this.leadServicePort = leadServicePort;
        this.miembroRepository = miembroRepository;
        this.batchRepository = batchRepository;
    }

    @Transactional
    public Segmento crearSegmento(Segmento segmento) {
        // Guardar el segmento
        Segmento savedSegmento = segmentoRepository.save(segmento);

        // Materialize automatically on creation
        System.out.println("✓ Segmento creado ID: " + savedSegmento.getId() + " - Materializando automáticamente...");
        materializarSegmento(savedSegmento.getId());

        return savedSegmento;
    }

    @Transactional
    public Segmento actualizarSegmento(Long id, Segmento segmento) {
        return segmentoRepository.findById(id).map(existing -> {
            existing.setNombre(segmento.getNombre());
            existing.setDescripcion(segmento.getDescripcion());
            existing.setEstado(segmento.getEstado());
            existing.setReglaPrincipal(segmento.getReglaPrincipal());
            existing.actualizarFecha();

            Segmento updated = segmentoRepository.save(existing);

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
            segmento.setEstado("ELIMINADO");
            segmento.actualizarFecha();
            segmentoRepository.save(segmento);
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
        System.out.println("=== Materializando segmento ID: " + id + " ===");

        // Obtener IDs de leads que cumplen con las reglas (desde caché)
        List<Long> leadIds = leadServicePort.findLeadsBySegmento(segmento);
        System.out.println("Leads encontrados: " + leadIds.size());

        // Limpiar miembros existentes usando JDBC
        batchRepository.deleteByIdSegmento(id);

        if (leadIds.isEmpty()) {
            System.out.println("⚠ No hay leads que cumplan con los filtros");
            return;
        }

        // JDBC BATCH INSERT: Insertar todos de una vez (1 query real)
        String tipoMiembro = segmento.getTipoAudiencia();
        LocalDateTime ahora = LocalDateTime.now();

        batchRepository.batchInsertMiembros(id, tipoMiembro, leadIds, ahora);

        // Actualizar cantidad de miembros en el segmento
        segmento.setCantidadMiembros(leadIds.size());
        segmentoRepository.save(segmento);

        System.out.println("✓ Segmento materializado con " + leadIds.size() + " miembros");
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
