package pe.unmsm.crm.marketing.campanas.encuestas.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pe.unmsm.crm.marketing.campanas.encuestas.api.dto.CreateEncuestaDto;
import pe.unmsm.crm.marketing.campanas.encuestas.api.dto.EncuestaCompletaDto;
import pe.unmsm.crm.marketing.campanas.encuestas.api.dto.EncuestaDisponibleDto;
import pe.unmsm.crm.marketing.campanas.encuestas.api.dto.EncuestaDto;
import pe.unmsm.crm.marketing.campanas.encuestas.application.service.EncuestaService;
import pe.unmsm.crm.marketing.campanas.encuestas.application.service.ListarEncuestasActivasService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/marketing/campanas/encuestas")
public class EncuestaController {

    @Autowired
    private EncuestaService encuestaService;

    @Autowired
    private ListarEncuestasActivasService listarEncuestasActivasService;

    @GetMapping
    public List<EncuestaDto> listarEncuestas() {
        return encuestaService.obtenerTodasConEstadisticas();
    }

    /**
     * Lista todas las encuestas disponibles (en estado ACTIVA).
     * Retorna solo el ID y título para optimizar la respuesta.
     * 
     * @return Lista de encuestas disponibles con id_encuesta y titulo
     */
    @GetMapping("/disponibles")
    public List<EncuestaDisponibleDto> listarEncuestasDisponibles() {
        return listarEncuestasActivasService.obtenerEncuestasActivas();
    }

    @PostMapping
    public EncuestaCompletaDto crearEncuesta(
            @jakarta.validation.Valid @RequestBody CreateEncuestaDto dto) {
        return encuestaService.crearEncuesta(dto);
    }

    @GetMapping("/{id}")
    public EncuestaCompletaDto obtenerEncuesta(@PathVariable Integer id) {
        return encuestaService.obtenerPorId(id);
    }

    @PutMapping("/{id}")
    public EncuestaCompletaDto actualizarEncuesta(
            @PathVariable Integer id,
            @jakarta.validation.Valid @RequestBody CreateEncuestaDto dto) {
        return encuestaService.actualizarEncuesta(id, dto);
    }

    @PutMapping("/{id}/archivar")
    public org.springframework.http.ResponseEntity<?> archivarEncuesta(@PathVariable Integer id) {
        try {
            encuestaService.archivarEncuesta(id);
            return org.springframework.http.ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.CONFLICT)
                    .body(java.util.Collections.singletonMap("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}/campanas")
    public org.springframework.http.ResponseEntity<List<pe.unmsm.crm.marketing.campanas.gestor.api.dto.response.CampanaListItemResponse>> listarCampanasAsociadas(
            @PathVariable Integer id) {
        List<pe.unmsm.crm.marketing.campanas.gestor.domain.model.Campana> campanas = encuestaService
                .listarCampanasAsociadas(id);

        List<pe.unmsm.crm.marketing.campanas.gestor.api.dto.response.CampanaListItemResponse> response = campanas
                .stream()
                .map(c -> pe.unmsm.crm.marketing.campanas.gestor.api.dto.response.CampanaListItemResponse.builder()
                        .idCampana(c.getIdCampana())
                        .nombre(c.getNombre())
                        .estado(c.getEstado().getNombre())
                        .prioridad(c.getPrioridad().name())
                        .canalEjecucion(c.getCanalEjecucion().name())
                        .fechaProgramadaInicio(c.getFechaProgramadaInicio())
                        .fechaProgramadaFin(c.getFechaProgramadaFin())
                        .build())
                .collect(java.util.stream.Collectors.toList());

        return org.springframework.http.ResponseEntity.ok(response);
    }
}
