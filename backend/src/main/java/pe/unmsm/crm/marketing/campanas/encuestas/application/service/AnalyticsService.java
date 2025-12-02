package pe.unmsm.crm.marketing.campanas.encuestas.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.unmsm.crm.marketing.campanas.encuestas.domain.model.Encuesta;
import pe.unmsm.crm.marketing.campanas.encuestas.domain.model.Pregunta;
import pe.unmsm.crm.marketing.campanas.encuestas.domain.model.RespuestaEncuesta;
import pe.unmsm.crm.marketing.campanas.encuestas.domain.model.Respuesta_Detalle;
import pe.unmsm.crm.marketing.campanas.encuestas.domain.repository.EncuestaRepository;
import pe.unmsm.crm.marketing.campanas.encuestas.domain.repository.RespuestaEncuestaRepository;
import pe.unmsm.crm.marketing.campanas.encuestas.domain.strategy.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final RespuestaEncuestaRepository respuestaEncuestaRepository;
    private final EncuestaRepository encuestaRepository;

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTendenciaRespuestas(Integer idEncuesta) {
        List<Object[]> resultados = respuestaEncuestaRepository.countRespuestasByFecha(idEncuesta);

        return resultados.stream()
                .map(obj -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("fecha", obj[0].toString());
                    map.put("cantidad", obj[1]);
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AnalisisResultadoDto> getIndicadores(Integer idEncuesta, String rango) {
        Encuesta encuesta = encuestaRepository.findById(idEncuesta)
                .orElseThrow(() -> new IllegalArgumentException("Encuesta no encontrada con ID: " + idEncuesta));

        List<RespuestaEncuesta> respuestasEncuesta;

        if (rango == null || rango.equals("all")) {
            respuestasEncuesta = respuestaEncuestaRepository.findByEncuesta_IdEncuesta(idEncuesta);
        } else {
            java.time.LocalDateTime fechaInicio = java.time.LocalDateTime.now();
            switch (rango) {
                case "7d":
                    fechaInicio = fechaInicio.minusDays(7);
                    break;
                case "14d":
                    fechaInicio = fechaInicio.minusDays(14);
                    break;
                case "28d":
                    fechaInicio = fechaInicio.minusDays(28);
                    break;
                default:
                    respuestasEncuesta = respuestaEncuestaRepository.findByEncuesta_IdEncuesta(idEncuesta);
                    return processIndicadores(encuesta, respuestasEncuesta);
            }
            respuestasEncuesta = respuestaEncuestaRepository.findByEncuesta_IdEncuestaAndFechaRespuestaAfter(idEncuesta,
                    fechaInicio);
        }

        return processIndicadores(encuesta, respuestasEncuesta);
    }

    @Transactional(readOnly = true)
    public pe.unmsm.crm.marketing.campanas.encuestas.api.dto.AnalyticsSummaryDto getResumen(Integer idEncuesta,
            String rango) {
        long totalRespuestas;
        long alertasUrgentes;

        if (rango == null || rango.equals("all")) {
            List<RespuestaEncuesta> respuestas = respuestaEncuestaRepository.findByEncuesta_IdEncuesta(idEncuesta);
            totalRespuestas = respuestas.size();
            alertasUrgentes = respuestaEncuestaRepository.countAlertasUrgentes(idEncuesta);
        } else {
            java.time.LocalDateTime fechaInicio = java.time.LocalDateTime.now();
            switch (rango) {
                case "7d":
                    fechaInicio = fechaInicio.minusDays(7);
                    break;
                case "14d":
                    fechaInicio = fechaInicio.minusDays(14);
                    break;
                case "28d":
                    fechaInicio = fechaInicio.minusDays(28);
                    break;
                default:
                    fechaInicio = java.time.LocalDateTime.now().minusYears(100);
            }
            List<RespuestaEncuesta> respuestas = respuestaEncuestaRepository
                    .findByEncuesta_IdEncuestaAndFechaRespuestaAfter(idEncuesta, fechaInicio);
            totalRespuestas = respuestas.size();
            alertasUrgentes = respuestaEncuestaRepository.countAlertasUrgentesAfter(idEncuesta, fechaInicio);
        }

        return pe.unmsm.crm.marketing.campanas.encuestas.api.dto.AnalyticsSummaryDto.builder()
                .totalRespuestas(totalRespuestas)
                .alertasUrgentes(alertasUrgentes)
                .build();
    }

    private List<AnalisisResultadoDto> processIndicadores(Encuesta encuesta,
            List<RespuestaEncuesta> respuestasEncuesta) {
        // Aplanar todos los detalles de todas las respuestas
        List<Respuesta_Detalle> todosDetalles = respuestasEncuesta.stream()
                .flatMap(r -> r.getDetalles().stream())
                .collect(Collectors.toList());

        List<AnalisisResultadoDto> resultados = new ArrayList<>();

        for (Pregunta pregunta : encuesta.getPreguntas()) {
            // Filtrar respuestas para esta pregunta específica
            List<Respuesta_Detalle> respuestasPregunta = todosDetalles.stream()
                    .filter(d -> d.getPregunta().getIdPregunta().equals(pregunta.getIdPregunta()))
                    .collect(Collectors.toList());

            ICalculoIndicador strategy = getStrategy(pregunta.getTipoPregunta());
            if (strategy != null) {
                AnalisisResultadoDto resultado = strategy.calcular(pregunta, respuestasPregunta);
                // Enriquecer el DTO con información de la pregunta
                resultado.setEtiqueta(pregunta.getTextoPregunta());
                resultados.add(resultado);
            }
        }

        return resultados;
    }

    private ICalculoIndicador getStrategy(Pregunta.TipoPregunta tipo) {
        switch (tipo) {
            case ESCALA:
                return new PromedioStrategy();
            case UNICA:
                return new FrecuenciaSimpleStrategy();
            case MULTIPLE:
                return new FrecuenciaMultipleStrategy();
            default:
                return null;
        }
    }
}
