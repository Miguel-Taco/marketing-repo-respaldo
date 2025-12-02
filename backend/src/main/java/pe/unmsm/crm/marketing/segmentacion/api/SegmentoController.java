package pe.unmsm.crm.marketing.segmentacion.api;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.unmsm.crm.marketing.segmentacion.application.SegmentoExportService;
import pe.unmsm.crm.marketing.segmentacion.application.SegmentoPreviewService;
import pe.unmsm.crm.marketing.segmentacion.application.SegmentoService;
import pe.unmsm.crm.marketing.segmentacion.domain.model.Segmento;
import pe.unmsm.crm.marketing.shared.infra.exception.NotFoundException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/segmentos")
public class SegmentoController {

    private final SegmentoService segmentoService;
    private final SegmentoPreviewService previewService;
    private final SegmentoMapper mapper;
    private final SegmentoExportService exportService;

    public SegmentoController(SegmentoService segmentoService,
            SegmentoPreviewService previewService,
            SegmentoMapper mapper,
            SegmentoExportService exportService) {
        this.segmentoService = segmentoService;
        this.previewService = previewService;
        this.mapper = mapper;
        this.exportService = exportService;
    }

    @GetMapping
    public List<SegmentoDto> listar(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String tipoAudiencia,
            @RequestParam(required = false, defaultValue = "false") boolean includeDeleted) {

        List<Segmento> segmentos;

        if (estado != null && !estado.isEmpty()) {
            segmentos = segmentoService.listarSegmentosPorEstado(estado);
        } else if (includeDeleted) {
            // Return ALL segments including ELIMINADO
            segmentos = segmentoService.listarTodosLosSegmentos();
        } else {
            // Default: exclude ELIMINADO
            segmentos = segmentoService.listarSegmentos();
        }

        // Filtrar por tipo de audiencia si se proporciona
        if (tipoAudiencia != null && !tipoAudiencia.isEmpty()) {
            segmentos = segmentos.stream()
                    .filter(s -> s.getTipoAudiencia().equals(tipoAudiencia))
                    .collect(Collectors.toList());
        }

        return segmentos.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SegmentoDto> obtener(@PathVariable Long id) {
        return segmentoService.obtenerSegmento(id)
                .map(mapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public SegmentoDto crear(@RequestBody SegmentoDto dto) {
        Segmento segmento = mapper.toDomain(dto);
        return mapper.toDto(segmentoService.crearSegmento(segmento));
    }

    @PatchMapping("/{id}")
    public SegmentoDto actualizar(@PathVariable Long id, @RequestBody SegmentoDto dto) {
        Segmento segmento = mapper.toDomain(dto);
        return mapper.toDto(segmentoService.actualizarSegmento(id, segmento));
    }

    /**
     * Actualización rápida de campos básicos sin rematerialización
     */
    @PatchMapping("/{id}/quick")
    public SegmentoDto actualizarRapido(@PathVariable Long id, @RequestBody Map<String, String> campos) {
        String nombre = campos.get("nombre");
        String descripcion = campos.get("descripcion");
        String estado = campos.get("estado");

        Segmento updated = segmentoService.actualizarSegmentoBasico(id, nombre, descripcion, estado);
        return mapper.toDto(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        segmentoService.eliminarSegmento(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/preview")
    public ResponseEntity<Long> preview(@PathVariable Long id) {
        return ResponseEntity.ok(previewService.previsualizarConteo(id));
    }

    @PostMapping("/{id}/materializar")
    public ResponseEntity<Void> materializar(@PathVariable Long id) {
        segmentoService.materializarSegmento(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Previsualiza un segmento temporal sin guardarlo
     */
    @PostMapping("/preview-temp")
    public ResponseEntity<Map<String, Object>> previewTemporal(@RequestBody SegmentoDto dto) {
        Segmento segmento = mapper.toDomain(dto);
        Map<String, Object> result = previewService.previsualizarSegmentoTemporal(segmento);
        return ResponseEntity.ok(result);
    }

    /**
     * Exporta un segmento a Excel con información del segmento y sus miembros
     */
    @GetMapping("/{id}/export")
    public ResponseEntity<byte[]> exportarSegmento(@PathVariable Long id) throws IOException {
        Segmento segmento = segmentoService.obtenerSegmento(id)
                .orElseThrow(() -> new NotFoundException("Segmento", id));

        byte[] excelBytes = exportService.exportSegmentoToExcel(segmento);
        String filename = "segmento_" + segmento.getNombre().replaceAll("\\s+", "_") + "_"
                + java.time.LocalDate.now() + ".xlsx";

        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .body(excelBytes);
    }
}
