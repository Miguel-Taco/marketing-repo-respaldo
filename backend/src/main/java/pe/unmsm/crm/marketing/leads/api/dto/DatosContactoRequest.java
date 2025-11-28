package pe.unmsm.crm.marketing.leads.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatosContactoRequest {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    private String email;

    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;

    @Pattern(regexp = "\\d{6}", message = "El distrito debe tener exactamente 6 dígitos")
    private String distritoId;
}
