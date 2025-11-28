package pe.unmsm.crm.marketing.campanas.gestor.api.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para programar una campaña (Borrador → Programada)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgramarCampanaRequest {

    @NotNull(message = "La fecha de inicio es obligatoria")
    @Future(message = "La fecha de inicio debe ser futura")
    private LocalDateTime fechaProgramadaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDateTime fechaProgramadaFin;

    @NotNull(message = "El agente es obligatorio")
    private Integer idAgente;

    @NotNull(message = "El segmento es obligatorio")
    private Long idSegmento;

    // Opcional
    private Integer idEncuesta;
}
