package pe.unmsm.crm.marketing.leads.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import pe.unmsm.crm.marketing.leads.domain.enums.EstadoLead;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CambioEstadoLoteRequest {

    @NotEmpty(message = "La lista de IDs no puede estar vacía")
    private List<Long> ids;

    @NotNull(message = "El nuevo estado es obligatorio")
    private EstadoLead nuevoEstado;

    private String motivo;
}
