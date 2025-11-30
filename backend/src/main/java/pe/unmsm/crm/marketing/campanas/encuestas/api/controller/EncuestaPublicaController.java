package pe.unmsm.crm.marketing.campanas.encuestas.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pe.unmsm.crm.marketing.campanas.encuestas.api.dto.EncuestaCompletaDto;
import pe.unmsm.crm.marketing.campanas.encuestas.api.dto.RegistrarRespuestaDto;
import pe.unmsm.crm.marketing.campanas.encuestas.api.dto.RespuestaRegistradaDto;
import pe.unmsm.crm.marketing.campanas.encuestas.application.service.EncuestaService;
import pe.unmsm.crm.marketing.campanas.encuestas.application.service.RespuestaEncuestaService;
import pe.unmsm.crm.marketing.campanas.encuestas.domain.model.Encuesta;
import pe.unmsm.crm.marketing.campanas.encuestas.domain.repository.EncuestaRepository;

/**
 * Controlador público para acceder a encuestas activas.
 * No requiere autenticación y solo expone encuestas en estado ACTIVA.
 */
@RestController
@RequestMapping("/public/v1/encuestas")
@RequiredArgsConstructor
public class EncuestaPublicaController {

    private final EncuestaService encuestaService;
    private final EncuestaRepository encuestaRepository;
    private final RespuestaEncuestaService respuestaEncuestaService;

    /**
     * Obtiene el contenido completo de una encuesta activa.
     * 
     * @param idEncuesta ID de la encuesta
     * @return EncuestaCompletaDto con toda la estructura de la encuesta
     * @throws ResponseStatusException si la encuesta no existe o no está activa
     */
    @GetMapping("/contenido/{idEncuesta}")
    public EncuestaCompletaDto obtenerContenidoEncuesta(@PathVariable Integer idEncuesta) {
        // Buscar la encuesta
        Encuesta encuesta = encuestaRepository.findById(idEncuesta)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Encuesta no encontrada con ID: " + idEncuesta));

        // Validar que la encuesta esté en estado ACTIVA
        if (encuesta.getEstado() != Encuesta.EstadoEncuesta.ACTIVA) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "La encuesta no está disponible. Solo se pueden acceder a encuestas activas.");
        }

        // Retornar el DTO completo
        return encuestaService.obtenerPorId(idEncuesta);
    }

    /**
     * Registra las respuestas de un lead a una encuesta.
     * 
     * @param dto Datos de la respuesta (leadId, idEncuesta, respuestas)
     * @return RespuestaRegistradaDto con confirmación del registro
     */
    @PostMapping("/respuestas")
    public ResponseEntity<RespuestaRegistradaDto> registrarRespuesta(@Valid @RequestBody RegistrarRespuestaDto dto) {
        try {
            RespuestaRegistradaDto respuesta = respuestaEncuestaService.registrarRespuesta(dto);
            return ResponseEntity.ok(respuesta);
        } catch (IllegalArgumentException e) {
            // Errores de validación (lead no encontrado, pregunta inválida, etc.)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            // Errores de estado (encuesta no activa, ya respondió, etc.)
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            // Error inesperado
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al procesar la respuesta: " + e.getMessage());
        }
    }
}
