package pe.unmsm.crm.marketing.campanas.gestor.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para reprogramar fechas de una campaña (Programada o Pausada)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReprogramarCampanaRequest {

    private LocalDateTime nuevaFechaInicio;
    private LocalDateTime nuevaFechaFin;
}
