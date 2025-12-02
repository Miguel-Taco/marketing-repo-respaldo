package pe.unmsm.crm.marketing.campanas.mailing.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReprogramarCampanaRequest {
    
    @NotNull(message = "Fecha de inicio es obligatoria")
    private LocalDateTime fechaInicio;
    
    @NotNull(message = "Fecha de fin es obligatoria")
    private LocalDateTime fechaFin;
}