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
    public List<AnalisisResultadoDto> getIndicadores(Integer idEncuesta) {
        Encuesta encuesta = encuestaRepository.findById(idEncuesta)
                .orElseThrow(() -> new IllegalArgumentException("Encuesta no encontrada con ID: " + idEncuesta));

        List<RespuestaEncuesta> respuestasEncuesta = respuestaEncuestaRepository.findByEncuesta_IdEncuesta(idEncuesta);

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
