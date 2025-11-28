package pe.unmsm.crm.marketing.leads.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HistorialEstadoLeadDTO {
    private String fecha;
    private String estado;
    private String motivo;
}
