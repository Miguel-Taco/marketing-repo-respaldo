package pe.unmsm.crm.marketing.leads.api.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatosDemograficosDTO {
    private Integer edad;
    private String genero;
    private String distrito;
    private String distritoNombre;
    private String provinciaNombre;
    private String departamentoNombre;
}
