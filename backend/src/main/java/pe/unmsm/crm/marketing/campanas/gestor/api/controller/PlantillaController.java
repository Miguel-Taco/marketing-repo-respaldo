package pe.unmsm.crm.marketing.campanas.gestor.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.unmsm.crm.marketing.campanas.encuestas.domain.repository.EncuestaRepository;
import pe.unmsm.crm.marketing.campanas.gestor.api.dto.request.CrearPlantillaRequest;
import pe.unmsm.crm.marketing.campanas.gestor.api.dto.response.PlantillaResponse;
import pe.unmsm.crm.marketing.campanas.gestor.application.mapper.CampanaMapper;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.PlantillaCampana;
import pe.unmsm.crm.marketing.campanas.gestor.domain.port.input.IPlantillaUseCase;
import pe.unmsm.crm.marketing.segmentacion.domain.repository.SegmentoRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador REST para la gestión de plantillas de campanas.
 */
@RestController
@RequestMapping("/api/v1/plantillas")
@RequiredArgsConstructor
public class PlantillaController {

    private final IPlantillaUseCase plantillaUseCase;
    private final CampanaMapper mapper;
    private final SegmentoRepository segmentoRepository;
    private final EncuestaRepository encuestaRepository;

    /**
     * GET /api/v1/plantillas - Listar plantillas con filtros
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listarPlantillas(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String canalEjecucion,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<PlantillaCampana> plantillas = plantillaUseCase.listar(nombre, canalEjecucion);

        // Paginación simple
        int start = page * size;
        int end = Math.min(start + size, plantillas.size());
        List<PlantillaCampana> paginatedList = plantillas.subList(
                Math.min(start, plantillas.size()),
                end);

        // Mapear a PlantillaResponse con nombres
        List<PlantillaResponse> content = paginatedList.stream().map(p -> {
            String nombreSegmento = null;
            if (p.getIdSegmento() != null) {
                nombreSegmento = segmentoRepository.findById(p.getIdSegmento())
                        .map(s -> s.getNombre())
                        .orElse(null);
            }

            String tituloEncuesta = null;
            if (p.getIdEncuesta() != null) {
                tituloEncuesta = encuestaRepository.findById(p.getIdEncuesta())
                        .map(e -> e.getTitulo())
                        .orElse(null);
            }

            return PlantillaResponse.builder()
                    .idPlantilla(p.getIdPlantilla())
                    .nombre(p.getNombre())
                    .tematica(p.getTematica())
                    .descripcion(p.getDescripcion())
                    .canalEjecucion(p.getCanalEjecucion())
                    .idSegmento(p.getIdSegmento())
                    .nombreSegmento(nombreSegmento)
                    .idEncuesta(p.getIdEncuesta())
                    .tituloEncuesta(tituloEncuesta)
                    .fechaCreacion(p.getFechaCreacion())
                    .fechaModificacion(p.getFechaModificacion())
                    .build();
        }).collect(Collectors.toList());

        Page<PlantillaResponse> pageResult = new PageImpl<>(
                content,
                PageRequest.of(page, size),
                plantillas.size());

        Map<String, Object> response = new HashMap<>();
        response.put("content", pageResult.getContent());
        response.put("page", pageResult.getNumber());
        response.put("size", pageResult.getSize());
        response.put("total_elements", pageResult.getTotalElements());
        response.put("total_pages", pageResult.getTotalPages());

        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/plantillas - Crear plantilla
     */
    @PostMapping
    public ResponseEntity<PlantillaCampana> crearPlantilla(
            @Valid @RequestBody CrearPlantillaRequest request) {

        PlantillaCampana plantilla = mapper.toEntity(request);
        PlantillaCampana created = plantillaUseCase.crear(plantilla);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/v1/plantillas/{id} - Editar plantilla
     */
    @PutMapping("/{id}")
    public ResponseEntity<PlantillaCampana> editarPlantilla(
            @PathVariable Integer id,
            @Valid @RequestBody CrearPlantillaRequest request) {

        PlantillaCampana datosActualizados = mapper.toEntity(request);
        PlantillaCampana updated = plantillaUseCase.editar(id, datosActualizados);

        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/v1/plantillas/{id} - Eliminar plantilla
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPlantilla(@PathVariable Integer id) {
        plantillaUseCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
