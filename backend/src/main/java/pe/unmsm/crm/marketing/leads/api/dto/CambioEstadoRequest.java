package pe.unmsm.crm.marketing.leads.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import pe.unmsm.crm.marketing.leads.domain.enums.EstadoLead;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CambioEstadoRequest {

    @NotNull(message = "El nuevo estado es obligatorio")
    private EstadoLead nuevoEstado;

    private String motivo;
}
