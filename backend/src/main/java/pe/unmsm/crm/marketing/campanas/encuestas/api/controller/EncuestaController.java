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
}
