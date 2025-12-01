package pe.unmsm.crm.marketing.campanas.encuestas.domain.strategy;

import pe.unmsm.crm.marketing.campanas.encuestas.domain.model.Pregunta;
import pe.unmsm.crm.marketing.campanas.encuestas.domain.model.Respuesta_Detalle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PromedioStrategy implements ICalculoIndicador {

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

        List<Integer> valores = respuestas.stream()
                .filter(r -> r.getValorRespuesta() != null)
                .map(r -> (int) r.getValorRespuesta())
                .collect(Collectors.toList());

        if (valores.isEmpty()) {
            return AnalisisResultadoDto.builder()
                    .etiqueta("Sin valores numéricos")
                    .valor(0.0)
                    .porcentaje(0.0)
                    .metadata(new HashMap<>())
                    .build();
        }

        double promedio = valores.stream().mapToInt(Integer::intValue).average().orElse(0.0);

        // Calcular moda
        Map<Integer, Long> frecuencias = valores.stream()
                .collect(Collectors.groupingBy(v -> v, Collectors.counting()));

        int moda = frecuencias.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(0);

        // Calcular desviación estándar
        double varianza = valores.stream()
                .mapToDouble(v -> Math.pow(v - promedio, 2))
                .average()
                .orElse(0.0);
        double desviacionEstandar = Math.sqrt(varianza);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("moda", moda);
        metadata.put("desviacionEstandar", desviacionEstandar);
        metadata.put("histograma", frecuencias);
        metadata.put("totalRespuestas", valores.size());

        return AnalisisResultadoDto.builder()
                .etiqueta("Promedio")
                .valor(promedio)
                .porcentaje(100.0) // En escala, el promedio representa el total del conjunto analizado
                .metadata(metadata)
                .build();
    }
}
