package pe.unmsm.crm.marketing.campanas.encuestas.domain.strategy;

import pe.unmsm.crm.marketing.campanas.encuestas.domain.model.Opcion;
import pe.unmsm.crm.marketing.campanas.encuestas.domain.model.Pregunta;
import pe.unmsm.crm.marketing.campanas.encuestas.domain.model.Respuesta_Detalle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FrecuenciaMultipleStrategy implements ICalculoIndicador {

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

        // Base de encuestados únicos (para calcular porcentajes sobre personas, no
        // sobre votos totales)
        long totalEncuestadosUnicos = respuestas.stream()
                .map(r -> r.getRespuestaEncuesta().getIdRespuestaEncuesta())
                .distinct()
                .count();

        Map<String, Long> conteoPorOpcion = respuestas.stream()
                .filter(r -> r.getOpcion() != null)
                .collect(Collectors.groupingBy(r -> r.getOpcion().getTextoOpcion(), Collectors.counting()));

        // Asegurar opciones con 0
        if (pregunta.getOpciones() != null) {
            for (Opcion opcion : pregunta.getOpciones()) {
                conteoPorOpcion.putIfAbsent(opcion.getTextoOpcion(), 0L);
            }
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("distribucion", conteoPorOpcion);
        metadata.put("totalEncuestadosUnicos", totalEncuestadosUnicos);

        // Calcular porcentajes relativos a la base de encuestados
        Map<String, Double> porcentajes = new HashMap<>();
        if (totalEncuestadosUnicos > 0) {
            for (Map.Entry<String, Long> entry : conteoPorOpcion.entrySet()) {
                double pct = (entry.getValue() * 100.0) / totalEncuestadosUnicos;
                porcentajes.put(entry.getKey(), pct);
            }
        }
        metadata.put("porcentajes", porcentajes);

        return AnalisisResultadoDto.builder()
                .etiqueta("Frecuencia Múltiple")
                .valor((double) totalEncuestadosUnicos)
                .porcentaje(100.0)
                .metadata(metadata)
                .build();
    }
}
