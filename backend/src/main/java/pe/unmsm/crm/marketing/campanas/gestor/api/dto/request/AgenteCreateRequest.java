package pe.unmsm.crm.marketing.campanas.gestor.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para crear o actualizar un Agente de Marketing.
 * Si idAgente es proporcionado y existe, se actualiza; de lo contrario, se
 * crea.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgenteCreateRequest {

    /**
     * ID externo del agente (opcional para creación, usado para upsert).
     */
    private Integer idAgente;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    private String nombre;

    @Email(message = "El email debe tener un formato válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    private String email;

    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    private String telefono;

    /**
     * Indica si el agente está activo. Por defecto true.
     */
    @Builder.Default
    private Boolean activo = true;
}
