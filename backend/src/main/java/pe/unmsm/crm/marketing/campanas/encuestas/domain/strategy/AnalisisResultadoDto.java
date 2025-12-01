package pe.unmsm.crm.marketing.campanas.encuestas.domain.strategy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalisisResultadoDto {
    private String etiqueta;
    private Double valor;
    private Double porcentaje;
    private Map<String, Object> metadata;
}
