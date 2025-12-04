package pe.unmsm.crm.marketing.leads.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadMetricsDTO {
    private Long totalLeads;
    private Long leadsNuevos;
    private Long leadsCalificados;
    private Long leadsDescartados;
    private Double tasaConversion;
    private Double tasaDescarte;
    private Long leadsPorWeb;
    private Long leadsPorImportacion;
    private Map<String, Long> distribucionPorEstado;
    private Map<String, Long> distribucionPorFuente;
}
