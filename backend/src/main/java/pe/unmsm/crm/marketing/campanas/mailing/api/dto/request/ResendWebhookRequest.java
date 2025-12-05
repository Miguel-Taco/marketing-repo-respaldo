package pe.unmsm.crm.marketing.campanas.mailing.api.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

/**
 * DTO para recibir webhooks de Resend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResendWebhookRequest {

    /**
     * Tipo de evento (ej: "email.opened", "email.clicked", etc.)
     */
    private String type;

    /**
     * Timestamp del evento en formato ISO-8601
     */
    @JsonProperty("created_at")
    private String createdAt;

    /**
     * Datos específicos del evento
     */
    private ResendWebhookData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResendWebhookData {

        @JsonProperty("email_id")
        private String emailId;

        private String from;

        private List<String> to;

        private String subject;

        @JsonProperty("created_at")
        private String createdAt;

        /**
         * SOLO SI ES email.clicked
         */
        private Click click;

        /**
         * SOLO SI ES email.bounced
         */
        private Bounce bounce;

        /**
         * SOLO SI ES email.opened
         */
        private Open open;


        // ======== Email.clicked ========
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Click {

            private String link;

            private String timestamp;

            @JsonProperty("userAgent")
            private String userAgent;

            @JsonProperty("ipAddress")
            private String ipAddress;
        }


        // ======== Email.bounced ========
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Bounce {
            private String type;
            private String message;
        }


        // ======== Email.opened ========
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Open {

            private String timestamp;

            @JsonProperty("userAgent")
            private String userAgent;

            @JsonProperty("ipAddress")
            private String ipAddress;
        }
    }

    // ======== Métodos de ayuda ========

    public String getFirstRecipient() {
        if (data != null && data.getTo() != null && !data.getTo().isEmpty()) {
            return data.getTo().get(0);
        }
        return null;
    }

    public String getEmailId() {
        return data != null ? data.getEmailId() : null;
    }

    public boolean isOpenEvent() {
        return "email.opened".equals(type);
    }

    public boolean isClickEvent() {
        return "email.clicked".equals(type);
    }

    public boolean isBounceEvent() {
        return "email.bounced".equals(type);
    }

    public boolean isDeliveredEvent() {
        return "email.delivered".equals(type);
    }

    public boolean isComplaintEvent() {
        return "email.complained".equals(type);
    }

    public boolean isSentEvent() {
        return "email.sent".equals(type);
    }
}
