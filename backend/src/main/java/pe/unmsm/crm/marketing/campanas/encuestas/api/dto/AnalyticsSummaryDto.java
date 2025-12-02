package pe.unmsm.crm.marketing.campanas.encuestas.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsSummaryDto {
    private Long totalRespuestas;
    private Long alertasUrgentes;
}
