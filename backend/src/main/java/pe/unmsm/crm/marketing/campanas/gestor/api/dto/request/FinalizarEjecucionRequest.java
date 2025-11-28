package pe.unmsm.crm.marketing.campanas.gestor.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.ResultadoEjecucion;

import java.time.LocalDateTime;

/**
 * DTO para webhook de finalización de ejecución (usado por Mailing/Llamadas)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalizarEjecucionRequest {

    @NotNull(message = "El resultado es obligatorio")
    private ResultadoEjecucion resultado;

    private String mensaje;

    @NotNull(message = "La fecha de finalización es obligatoria")
    private LocalDateTime fechaFin;
}
