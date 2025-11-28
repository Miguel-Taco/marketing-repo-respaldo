package pe.unmsm.crm.marketing.leads.domain.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.Embeddable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatosContacto {
    String email;
    String telefono;
}