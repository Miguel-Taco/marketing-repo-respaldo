package pe.unmsm.crm.marketing.segmentacion.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.unmsm.crm.marketing.segmentacion.application.SegmentoService;
import pe.unmsm.crm.marketing.segmentacion.infra.persistence.JpaSegmentoMiembroRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * API interna para que otros módulos (como Campañas) accedan a información de
 * segmentos.
 * Esta API está separada del SegmentoController principal para evitar
 * conflictos.
 */
@RestController
@RequestMapping("/api/v1/internal/segmentos")
public class SegmentoInternalController {

    private final SegmentoService segmentoService;
    private final SegmentoMapper mapper;
    private final JpaSegmentoMiembroRepository miembroRepository;

    public SegmentoInternalController(SegmentoService segmentoService,
            SegmentoMapper mapper,
            JpaSegmentoMiembroRepository miembroRepository) {
        this.segmentoService = segmentoService;
        this.mapper = mapper;
        this.miembroRepository = miembroRepository;
    }

    /**
     * Listar todos los segmentos activos (para selección en campañas)
     * Solo devuelve segmentos con estado ACTIVO
     */
    @GetMapping("/activos")
    public List<SegmentoDto> listarSegmentosActivos() {
        return segmentoService.listarSegmentos().stream()
                .filter(s -> "ACTIVO".equals(s.getEstado()))
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtener un segmento específico por ID (para campañas)
     */
    @GetMapping("/{id}")
    public ResponseEntity<SegmentoDto> obtenerSegmento(@PathVariable Long id) {
        return segmentoService.obtenerSegmento(id)
                .map(mapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Obtener los IDs de los miembros de un segmento (leads que pertenecen al
     * segmento)
     * Útil para campañas que necesitan saber a quién enviar mensajes
     */
    @GetMapping("/{id}/miembros")
    public ResponseEntity<List<Long>> obtenerMiembrosSegmento(@PathVariable Long id) {
        List<Long> miembros = segmentoService.obtenerMiembrosSegmento(id);
        return ResponseEntity.ok(miembros);
    }

    /**
     * Obtener información resumida de un segmento (para mostrar en campañas)
     */
    @GetMapping("/{id}/resumen")
    public ResponseEntity<SegmentoResumenDto> obtenerResumenSegmento(@PathVariable Long id) {
        return segmentoService.obtenerSegmento(id)
                .map(segmento -> {
                    SegmentoResumenDto resumen = new SegmentoResumenDto();
                    resumen.setId(segmento.getId());
                    resumen.setNombre(segmento.getNombre());
                    resumen.setDescripcion(segmento.getDescripcion());
                    resumen.setTipoAudiencia(segmento.getTipoAudiencia());

                    // Calculate member count using repository
                    long count = miembroRepository.countByIdSegmento(segmento.getId());
                    resumen.setCantidadMiembros((int) count);

                    resumen.setEstado(segmento.getEstado());
                    return ResponseEntity.ok(resumen);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DTO simplificado para resumen de segmento
     */
    public static class SegmentoResumenDto {
        private Long id;
        private String nombre;
        private String descripcion;
        private String tipoAudiencia;
        private Integer cantidadMiembros;
        private String estado;

        // Getters y Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getTipoAudiencia() {
            return tipoAudiencia;
        }

        public void setTipoAudiencia(String tipoAudiencia) {
            this.tipoAudiencia = tipoAudiencia;
        }

        public Integer getCantidadMiembros() {
            return cantidadMiembros;
        }

        public void setCantidadMiembros(Integer cantidadMiembros) {
            this.cantidadMiembros = cantidadMiembros;
        }

        public String getEstado() {
            return estado;
        }

        public void setEstado(String estado) {
            this.estado = estado;
        }
    }
}
