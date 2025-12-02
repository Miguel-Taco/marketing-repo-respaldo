package pe.unmsm.crm.marketing.campanas.encuestas.domain.strategy;

import pe.unmsm.crm.marketing.campanas.encuestas.domain.model.Opcion;
import pe.unmsm.crm.marketing.campanas.encuestas.domain.model.Pregunta;
import pe.unmsm.crm.marketing.campanas.encuestas.domain.model.Respuesta_Detalle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FrecuenciaSimpleStrategy implements ICalculoIndicador {

    @Override
    public AnalisisResultadoDto calcular(Pregunta pregunta, List<Respuesta_Detalle> respuestas) {
        if (respuestas == null || respuestas.isEmpty()) {
            return AnalisisResultadoDto.builder()
                    .etiqueta("Sin respuestas")
                    .valor(0.0)
                    .porcentaje(0.0)
                    .metadata(new HashMap<>())
                    .build();
        }

        long totalRespuestas = respuestas.size();
        Map<String, Long> conteoPorOpcion = respuestas.stream()
                .filter(r -> r.getOpcion() != null)
                .collect(Collectors.groupingBy(r -> r.getOpcion().getTextoOpcion(), Collectors.counting()));

        // Asegurar que todas las opciones de la pregunta estén presentes, incluso con 0
        if (pregunta.getOpciones() != null) {
            for (Opcion opcion : pregunta.getOpciones()) {
                conteoPorOpcion.putIfAbsent(opcion.getTextoOpcion(), 0L);
            }
        }

        Map<String, Double> porcentajes = new HashMap<>();
        if (totalRespuestas > 0) {
            for (Map.Entry<String, Long> entry : conteoPorOpcion.entrySet()) {
                double pct = (entry.getValue() * 100.0) / totalRespuestas;
                // Redondear a 1 decimal
                pct = Math.round(pct * 10.0) / 10.0;
                porcentajes.put(entry.getKey(), pct);
            }
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("distribucion", conteoPorOpcion);
        metadata.put("porcentajes", porcentajes);
        metadata.put("totalRespuestas", totalRespuestas);

        // Para frecuencia simple, retornamos el total como valor principal,
        // pero la data rica está en la metadata
        return AnalisisResultadoDto.builder()
                .etiqueta("Frecuencia Simple")
                .valor((double) totalRespuestas)
                .porcentaje(100.0)
                .metadata(metadata)
                .build();
    }
}
