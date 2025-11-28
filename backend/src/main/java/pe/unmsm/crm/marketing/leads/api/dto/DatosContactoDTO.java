package pe.unmsm.crm.marketing.leads.api.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatosContactoDTO {
    private String email;
    private String telefono;
}
