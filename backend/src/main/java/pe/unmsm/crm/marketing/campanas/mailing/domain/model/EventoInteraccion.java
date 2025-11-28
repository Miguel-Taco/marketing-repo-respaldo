package pe.unmsm.crm.marketing.campanas.mailing.domain.model;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class EventoInteraccion {
    private Integer idCampanaMailingId;
    private TipoInteraccion tipoEvento;
    private String emailContacto;
    private Long idContactoCrm;
    private LocalDateTime fechaEvento;
    private String metadata; // JSON con datos adicionales
    private String sendgridEventId; // Para deduplicación
}