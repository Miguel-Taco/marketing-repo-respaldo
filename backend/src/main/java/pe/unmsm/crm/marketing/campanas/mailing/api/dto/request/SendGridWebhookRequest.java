package pe.unmsm.crm.marketing.campanas.mailing.api.dto.request;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.*;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendGridWebhookRequest {
    private String event; // open, click, bounce, unsubscribed, delivered
    private String email;
    private Long timestamp;
    private String sendgridEventId;
    private String url; // Para click
    private String reason; // Para bounce/unsubscribed

    @JsonAnySetter
    @Builder.Default  // ✅ AGREGADO
    private Map<String, Object> additionalProperties = new HashMap<>();
}
