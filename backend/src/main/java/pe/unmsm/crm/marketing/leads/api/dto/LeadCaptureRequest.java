package pe.unmsm.crm.marketing.leads.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadCaptureRequest {

    @NotBlank(message = "El nombre completo es obligatorio")
    private String nombreCompleto;

    @NotBlank(message = "El origen es obligatorio")
    private String origen;

    @NotNull(message = "Los datos de contacto son obligatorios")
    @Valid
    private DatosContactoRequest contacto;

    private DatosDemograficosRequest demograficos;

    private TrackingUTMRequest tracking;
}
