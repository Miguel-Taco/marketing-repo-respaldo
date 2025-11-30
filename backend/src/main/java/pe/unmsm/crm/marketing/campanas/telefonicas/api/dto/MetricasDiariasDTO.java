package pe.unmsm.crm.marketing.campanas.telefonicas.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para métricas diarias de una campaña telefónica
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricasDiariasDTO {
    private Long pendientes;
    private Long realizadasHoy;
    private Long efectivasHoy;
}
