package pe.unmsm.crm.marketing.campanas.gestor.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@PreAuthorize("hasRole('ADMIN')")
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

                Page<PlantillaCampana> pagePlantillas = plantillaUseCase.listar(nombre, canalEjecucion, page, size);

                // Recopilar todos los IDs necesarios para hacer consultas en batch
                List<Long> idsSegmentos = pagePlantillas.getContent().stream()
                                .map(PlantillaCampana::getIdSegmento)
                                .filter(id -> id != null)
                                .distinct()
                                .collect(Collectors.toList());

                List<Integer> idsEncuestas = pagePlantillas.getContent().stream()
                                .map(PlantillaCampana::getIdEncuesta)
                                .filter(id -> id != null)
                                .distinct()
                                .collect(Collectors.toList());

                // Cargar todos los segmentos y encuestas necesarios en batch
                Map<Long, String> nombresSegmentos = new HashMap<>();
                if (!idsSegmentos.isEmpty()) {
                        // SegmentoRepository no tiene findAllById, usamos findAll y filtramos
                        segmentoRepository.findAll().stream()
                                        .filter(s -> idsSegmentos.contains(s.getId()))
                                        .forEach(s -> nombresSegmentos.put(s.getId(), s.getNombre()));
                }

                Map<Integer, String> titulosEncuestas = new HashMap<>();
                if (!idsEncuestas.isEmpty()) {
                        encuestaRepository.findAllById(idsEncuestas)
                                        .forEach(e -> titulosEncuestas.put(e.getIdEncuesta(), e.getTitulo()));
                }

                // Mapear a PlantillaResponse usando los Maps (sin consultas adicionales)
                List<PlantillaResponse> content = pagePlantillas.getContent().stream().map(p -> {
                        String nombreSegmento = p.getIdSegmento() != null
                                        ? nombresSegmentos.get(p.getIdSegmento())
                                        : null;
                        String tituloEncuesta = p.getIdEncuesta() != null
                                        ? titulosEncuestas.get(p.getIdEncuesta())
                                        : null;

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

                Map<String, Object> response = new HashMap<>();
                response.put("content", content);
                response.put("page", pagePlantillas.getNumber());
                response.put("size", pagePlantillas.getSize());
                response.put("total_elements", pagePlantillas.getTotalElements());
                response.put("total_pages", pagePlantillas.getTotalPages());

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
