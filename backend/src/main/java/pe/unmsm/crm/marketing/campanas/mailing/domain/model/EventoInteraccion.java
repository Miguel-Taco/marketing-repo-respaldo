package pe.unmsm.crm.marketing.campanas.mailing.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class EventoInteraccion {
    private Integer id_campana_mailing;
    private TipoInteraccion tipo_evento;      // APERTURA, CLIC, REBOTE, BAJA
    private String email_contacto;
    private Integer id_contacto_crm;           // Derivado del email (LOOKUP)
    private LocalDateTime fecha_evento;
    private String metadata;                   // JSON: user_agent, ip, etc.
    private String sendgrid_event_id;          // Para deduplicación
    
    // Auxiliar: referencia a campaña para contexto
    private CampanaMailing campana;
}