package pe.unmsm.crm.marketing.leads.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.unmsm.crm.marketing.leads.domain.enums.EstadoLead;
import pe.unmsm.crm.marketing.leads.domain.enums.TipoFuente;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadReportFilterDTO {
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private EstadoLead estado;
    private TipoFuente fuenteTipo;
    private String search;
    private Integer edadMin;
    private Integer edadMax;
    private String genero;
    private String distrito;
}
