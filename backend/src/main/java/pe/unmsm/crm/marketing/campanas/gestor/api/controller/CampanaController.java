package pe.unmsm.crm.marketing.campanas.gestor.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.unmsm.crm.marketing.campanas.gestor.api.dto.request.*;
import pe.unmsm.crm.marketing.campanas.gestor.api.dto.response.CampanaDetalleResponse;
import pe.unmsm.crm.marketing.campanas.gestor.api.dto.response.CampanaListItemResponse;
import pe.unmsm.crm.marketing.campanas.gestor.api.dto.response.HistorialItemResponse;
import pe.unmsm.crm.marketing.campanas.gestor.application.mapper.CampanaMapper;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.Campana;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.HistorialCampana;
import pe.unmsm.crm.marketing.campanas.gestor.domain.port.input.IGestorCampanaUseCase;
import pe.unmsm.crm.marketing.campanas.gestor.domain.port.output.HistorialRepositoryPort;
import pe.unmsm.crm.marketing.shared.api.dto.PageResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador REST para la gestión de campanas de marketing.
 * Expone todos los endpoints según la especificación OpenAPI.
 */
@RestController
@RequestMapping("/api/v1/campanas")
@RequiredArgsConstructor
public class CampanaController {

        private final IGestorCampanaUseCase gestorCampanaUseCase;
        private final HistorialRepositoryPort historialRepository;
        private final CampanaMapper mapper;

        /**
         * GET /api/v1/campanas - Listar campanas con filtros y paginación
         */
        @GetMapping
        public ResponseEntity<PageResponse<CampanaListItemResponse>> listarCampanas(
                        @RequestParam(required = false) String nombre,
                        @RequestParam(required = false) String estado,
                        @RequestParam(required = false) String prioridad,
                        @RequestParam(required = false) String canalEjecucion,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

                // Por defecto, excluir archivadas
                List<Campana> campanas = gestorCampanaUseCase.listar(nombre, estado, prioridad, canalEjecucion, false);

                // Convertir a DTOs ligeros
                List<CampanaListItemResponse> responses = campanas.stream()
                                .map(mapper::toListItemResponse)
                                .collect(Collectors.toList());

                // Paginación manual
                int start = page * size;
                int end = Math.min(start + size, responses.size());
                List<CampanaListItemResponse> paginatedList = responses.subList(
                                Math.min(start, responses.size()),
                                end);

                int totalPages = (int) Math.ceil((double) responses.size() / size);

                PageResponse<CampanaListItemResponse> pageResponse = PageResponse.<CampanaListItemResponse>builder()
                                .content(paginatedList)
                                .page(page)
                                .size(size)
                                .totalElements(responses.size())
                                .totalPages(totalPages)
                                .first(page == 0)
                                .last(page >= totalPages - 1)
                                .build();

                return ResponseEntity.ok(pageResponse);
        }

        /**
         * POST /api/v1/campanas - Crear campaña en Borrador
         */
        @PostMapping
        public ResponseEntity<CampanaDetalleResponse> crearCampana(
                        @Valid @RequestBody CrearCampanaRequest request) {

                Campana campana = mapper.toEntity(request);
                Campana created = gestorCampanaUseCase.crear(campana);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(mapper.toDetailResponse(created));
        }

        /**
         * GET /api/v1/campanas/{id} - Obtener detalle de campaña
         */
        @GetMapping("/{id}")
        public ResponseEntity<CampanaDetalleResponse> obtenerCampana(@PathVariable Long id) {
                Campana campana = gestorCampanaUseCase.obtenerPorId(id);
                return ResponseEntity.ok(mapper.toDetailResponse(campana));
        }

        /**
         * PUT /api/v1/campanas/{id} - Editar campaña
         */
        @PutMapping("/{id}")
        public ResponseEntity<CampanaDetalleResponse> editarCampana(
                        @PathVariable Long id,
                        @Valid @RequestBody EditarCampanaRequest request) {

                Campana datosActualizados = Campana.builder()
                                .nombre(request.getNombre())
                                .tematica(request.getTematica())
                                .descripcion(request.getDescripcion())
                                .prioridad(request.getPrioridad())
                                .canalEjecucion(request.getCanalEjecucion())
                                .idAgente(request.getIdAgente())
                                .idSegmento(request.getIdSegmento())
                                .idEncuesta(request.getIdEncuesta())
                                .build();

                Campana updated = gestorCampanaUseCase.editar(id, datosActualizados);
                return ResponseEntity.ok(mapper.toDetailResponse(updated));
        }

        /**
         * DELETE /api/v1/campanas/{id} - Eliminar campaña (solo Borrador)
         */
        @DeleteMapping("/{id}")
        public ResponseEntity<Void> eliminarCampana(@PathVariable Long id) {
                gestorCampanaUseCase.eliminar(id);
                return ResponseEntity.noContent().build();
        }

        /**
         * POST /api/v1/campanas/{id}/programar - Programar campaña
         */
        @PostMapping("/{id}/programar")
        public ResponseEntity<CampanaDetalleResponse> programarCampana(
                        @PathVariable Long id,
                        @Valid @RequestBody ProgramarCampanaRequest request) {

                Campana programada = gestorCampanaUseCase.programar(
                                id,
                                request.getFechaProgramadaInicio(),
                                request.getFechaProgramadaFin(),
                                request.getIdAgente(),
                                request.getIdSegmento(),
                                request.getIdEncuesta());

                return ResponseEntity.status(HttpStatus.ACCEPTED)
                                .body(mapper.toDetailResponse(programada));
        }

        /**
         * POST /api/v1/campanas/{id}/pausar - Pausar campaña
         */
        @PostMapping("/{id}/pausar")
        public ResponseEntity<CampanaDetalleResponse> pausarCampana(
                        @PathVariable Long id,
                        @RequestBody(required = false) MotivoRequest request) {

                String motivo = request != null ? request.getMotivo() : null;
                Campana pausada = gestorCampanaUseCase.pausar(id, motivo);

                return ResponseEntity.ok(mapper.toDetailResponse(pausada));
        }

        /**
         * POST /api/v1/campanas/{id}/reanudar - Reanudar campaña
         */
        @PostMapping("/{id}/reanudar")
        public ResponseEntity<CampanaDetalleResponse> reanudarCampana(@PathVariable Long id) {
                Campana reanudada = gestorCampanaUseCase.reanudar(id);
                return ResponseEntity.ok(mapper.toDetailResponse(reanudada));
        }

        /**
         * POST /api/v1/campanas/{id}/cancelar - Cancelar campaña
         */
        @PostMapping("/{id}/cancelar")
        public ResponseEntity<CampanaDetalleResponse> cancelarCampana(
                        @PathVariable Long id,
                        @RequestBody(required = false) MotivoRequest request) {

                String motivo = request != null ? request.getMotivo() : null;
                Campana cancelada = gestorCampanaUseCase.cancelar(id, motivo);

                return ResponseEntity.ok(mapper.toDetailResponse(cancelada));
        }

        /**
         * POST /api/v1/campanas/{id}/reprogramar - Reprogramar fechas
         */
        @PostMapping("/{id}/reprogramar")
        public ResponseEntity<CampanaDetalleResponse> reprogramarCampana(
                        @PathVariable Long id,
                        @Valid @RequestBody ReprogramarCampanaRequest request) {

                Campana reprogramada = gestorCampanaUseCase.reprogramar(
                                id,
                                request.getNuevaFechaInicio(),
                                request.getNuevaFechaFin());

                return ResponseEntity.ok(mapper.toDetailResponse(reprogramada));
        }

        /**
         * POST /api/v1/campanas/{id}/archivar - Archivar campaña
         */
        @PostMapping("/{id}/archivar")
        public ResponseEntity<CampanaDetalleResponse> archivarCampana(@PathVariable Long id) {
                Campana archivada = gestorCampanaUseCase.archivar(id);
                return ResponseEntity.ok(mapper.toDetailResponse(archivada));
        }

        /**
         * POST /api/v1/campanas/{id}/duplicar - Duplicar campaña
         */
        @PostMapping("/{id}/duplicar")
        public ResponseEntity<CampanaDetalleResponse> duplicarCampana(@PathVariable Long id) {
                Campana duplicada = gestorCampanaUseCase.duplicar(id);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(mapper.toDetailResponse(duplicada));
        }

        /**
         * GET /api/v1/campanas/historial - Ver historial de acciones
         */
        @GetMapping("/historial")
        public ResponseEntity<Map<String, Object>> listarHistorial(
                        @RequestParam(required = false) Long idCampana,
                        @RequestParam(required = false) String tipoAccion,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size) {

                List<HistorialCampana> historial = historialRepository.findByFiltros(
                                idCampana, tipoAccion, null, null);

                // Convertir a DTOs
                List<HistorialItemResponse> responses = historial.stream()
                                .map(mapper::toHistorialResponse)
                                .collect(Collectors.toList());

                // Paginación simple
                int start = page * size;
                int end = Math.min(start + size, responses.size());
                List<HistorialItemResponse> paginatedList = responses.subList(
                                Math.min(start, responses.size()),
                                end);

                Page<HistorialItemResponse> pageResult = new PageImpl<>(
                                paginatedList,
                                PageRequest.of(page, size),
                                responses.size());

                Map<String, Object> response = new HashMap<>();
                response.put("content", pageResult.getContent());
                response.put("page", pageResult.getNumber());
                response.put("size", pageResult.getSize());
                response.put("total_elements", pageResult.getTotalElements());
                response.put("total_pages", pageResult.getTotalPages());

                return ResponseEntity.ok(response);
        }
}
