package pe.unmsm.crm.marketing.campanas.mailing.api.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InteraccionLogResponse {
    private Integer id;
    private Integer idCampanaMailingId;
    private Integer idTipoEvento;
    private String tipoEventoNombre;
    private Long idContactoCrm;
    private LocalDateTime fechaEvento;
}
