package pe.unmsm.crm.marketing.campanas.encuestas.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.unmsm.crm.marketing.campanas.encuestas.api.dto.RegistrarRespuestaDto;
import pe.unmsm.crm.marketing.campanas.encuestas.api.dto.RespuestaDetalleDto;
import pe.unmsm.crm.marketing.campanas.encuestas.api.dto.RespuestaRegistradaDto;
import pe.unmsm.crm.marketing.campanas.encuestas.domain.observer.AlertaUrgenteDetectadaEvent;
import pe.unmsm.crm.marketing.campanas.encuestas.domain.model.*;
import pe.unmsm.crm.marketing.campanas.encuestas.domain.repository.EncuestaRepository;
import pe.unmsm.crm.marketing.campanas.encuestas.domain.repository.RespuestaEncuestaRepository;
import pe.unmsm.crm.marketing.leads.domain.model.Lead;
import pe.unmsm.crm.marketing.leads.domain.repository.LeadRepository;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RespuestaEncuestaService {

    private final RespuestaEncuestaRepository respuestaEncuestaRepository;
    private final EncuestaRepository encuestaRepository;
    private final LeadRepository leadRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public RespuestaRegistradaDto registrarRespuesta(RegistrarRespuestaDto dto) {
        // 1. Validar que el lead existe
        Lead lead = leadRepository.findById(dto.getLeadId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Lead no encontrado con ID: " + dto.getLeadId()));

        // 2. Validar que la encuesta existe
        Encuesta encuesta = encuestaRepository.findById(dto.getIdEncuesta())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Encuesta no encontrada con ID: " + dto.getIdEncuesta()));

        // 3. Validar que la encuesta está ACTIVA
        if (encuesta.getEstado() != Encuesta.EstadoEncuesta.ACTIVA) {
            throw new IllegalStateException(
                    "La encuesta no está disponible. Solo se pueden responder encuestas activas.");
        }

        // 4. Validar que el lead no ha respondido previamente
        if (respuestaEncuestaRepository.existsByLeadAndEncuesta(dto.getLeadId(), dto.getIdEncuesta())) {
            throw new IllegalStateException(
                    "Ya has respondido esta encuesta anteriormente.");
        }

        // 5. Validar que todas las preguntas fueron respondidas
        Set<Integer> preguntasRespondidas = new HashSet<>();
        for (RespuestaDetalleDto respuesta : dto.getRespuestas()) {
            preguntasRespondidas.add(respuesta.getIdPregunta());
        }

        int totalPreguntas = encuesta.getPreguntas().size();
        if (preguntasRespondidas.size() != totalPreguntas) {
            throw new IllegalArgumentException(
                    "Debe responder todas las preguntas de la encuesta. " +
                            "Preguntas totales: " + totalPreguntas + ", Respondidas: " + preguntasRespondidas.size());
        }

        // 6. Crear la respuesta de encuesta
        RespuestaEncuesta respuestaEncuesta = new RespuestaEncuesta();
        respuestaEncuesta.setEncuesta(encuesta);
        respuestaEncuesta.setLead(lead);

        // 7. Crear los detalles de respuesta
        for (RespuestaDetalleDto detalleDto : dto.getRespuestas()) {
            Respuesta_Detalle detalle = new Respuesta_Detalle();
            detalle.setRespuestaEncuesta(respuestaEncuesta);

            // Buscar la pregunta
            Pregunta pregunta = encuesta.getPreguntas().stream()
                    .filter(p -> p.getIdPregunta().equals(detalleDto.getIdPregunta()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Pregunta no encontrada con ID: " + detalleDto.getIdPregunta()));

            detalle.setPregunta(pregunta);

            // Manejo específico según el tipo de pregunta
            if (pregunta
                    .getTipoPregunta() == pe.unmsm.crm.marketing.campanas.encuestas.domain.model.Pregunta.TipoPregunta.ESCALA) {
                // Para preguntas de ESCALA: idOpcion debe ser null y valorRespuesta debe estar
                // presente
                if (detalleDto.getValorRespuesta() == null) {
                    throw new IllegalArgumentException(
                            "Debe proporcionar una calificación para la pregunta de escala: "
                                    + pregunta.getTextoPregunta());
                }
                detalle.setValorRespuesta(detalleDto.getValorRespuesta());
            } else {
                // Para otros tipos (UNICA, MULTIPLE): idOpcion es obligatorio
                if (detalleDto.getIdOpcion() != null) {
                    Opcion opcion = pregunta.getOpciones().stream()
                            .filter(o -> o.getIdOpcion().equals(detalleDto.getIdOpcion()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Opción no encontrada con ID: " + detalleDto.getIdOpcion()));
                    detalle.setOpcion(opcion);
                } else {
                    // Si no es escala y no tiene opción, podría ser un error dependiendo de la
                    // lógica de negocio
                    // Por ahora asumimos que preguntas no-escala requieren opción
                    throw new IllegalArgumentException(
                            "Debe seleccionar una opción para la pregunta: " + pregunta.getTextoPregunta());
                }
            }

            // Si hay valor de respuesta adicional (para casos mixtos si existieran),
            // asignarlo
            // Nota: Para ESCALA ya se asignó arriba, esto es redundante pero seguro
            if (detalleDto.getValorRespuesta() != null && detalle.getValorRespuesta() == null) {
                detalle.setValorRespuesta(detalleDto.getValorRespuesta());
            }

            respuestaEncuesta.getDetalles().add(detalle);
        }

        // 8. Guardar la respuesta
        RespuestaEncuesta respuestaGuardada = respuestaEncuestaRepository.save(respuestaEncuesta);

        // 9. Verificar si hay opciones de alerta urgente y publicar evento
        boolean tieneAlertaUrgente = respuestaGuardada.getDetalles().stream()
                .filter(detalle -> detalle.getOpcion() != null)
                .anyMatch(detalle -> Boolean.TRUE.equals(detalle.getOpcion().getEsAlertaUrgente()));

        if (tieneAlertaUrgente) {
            // Publicar evento - el servicio no sabe quién lo escucha (Observer Pattern)
            eventPublisher.publishEvent(new AlertaUrgenteDetectadaEvent(dto.getLeadId(), dto.getIdEncuesta()));
        }

        // 10. Retornar confirmación
        return new RespuestaRegistradaDto(
                respuestaGuardada.getIdRespuestaEncuesta(),
                "Respuesta registrada exitosamente. ¡Gracias por tu participación!",
                respuestaGuardada.getFechaRespuesta());
    }
}
