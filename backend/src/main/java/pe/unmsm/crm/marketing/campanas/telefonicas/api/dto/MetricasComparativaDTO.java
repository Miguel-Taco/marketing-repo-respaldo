package pe.unmsm.crm.marketing.campanas.telefonicas.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricasComparativaDTO {
    private String periodo; // "Últimos 30 días", "30 días anteriores"
    private Integer llamadasRealizadas;
    private Integer contactosEfectivos;
    private Double tasaContacto;
}
