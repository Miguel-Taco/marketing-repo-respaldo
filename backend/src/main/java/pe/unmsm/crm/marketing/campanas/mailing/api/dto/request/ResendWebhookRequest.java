package pe.unmsm.crm.marketing.campanas.mailing.api.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

/**
 * DTO para recibir webhooks de Resend.
 * 
 * Documentación: https://resend.com/docs/dashboard/webhooks/introduction
 * 
 * Eventos soportados:
 * - email.sent        → Email enviado al servidor de Resend
 * - email.delivered   → Email entregado al destinatario
 * - email.opened      → Email abierto por el destinatario
 * - email.clicked     → Link clickeado en el email
 * - email.bounced     → Email rebotado
 * - email.complained  → Marcado como spam
 * - email.delivery_delayed → Entrega retrasada
 * 
 * Nota: Resend envía webhooks firmados con Svix. Los headers importantes son:
 * - svix-id: ID único del webhook
 * - svix-timestamp: Timestamp del envío
 * - svix-signature: Firma para validación
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResendWebhookRequest {

    /**
     * Tipo de evento (ej: "email.opened", "email.clicked", "email.bounced")
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
        
        /**
         * ID único del email en Resend
         */
        @JsonProperty("email_id")
        private String emailId;

        /**
         * Email del remitente
         */
        private String from;

        /**
         * Lista de destinatarios
         */
        private List<String> to;

        /**
         * Asunto del email
         */
        private String subject;

        /**
         * Timestamp de creación del email
         */
        @JsonProperty("created_at")
        private String createdAt;

        // ======== Campos específicos por tipo de evento ========

        /**
         * Para email.clicked: información del clic
         */
        private Click click;

        /**
         * Para email.bounced: razón del rebote
         */
        private Bounce bounce;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Click {
            /**
             * URL que fue clickeada
             */
            private String link;
            
            /**
             * Timestamp del clic
             */
            private String timestamp;
            
            /**
             * User agent del navegador
             */
            @JsonProperty("user_agent")
            private String userAgent;
            
            /**
             * IP del usuario
             */
            @JsonProperty("ip_address")
            private String ipAddress;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Bounce {
            /**
             * Tipo de rebote: "hard" o "soft"
             * - hard: email no existe o dominio inválido
             * - soft: buzón lleno u otro error temporal
             */
            private String type;
            
            /**
             * Mensaje de error del servidor de correo
             */
            private String message;
        }
    }

    // ======== Métodos de utilidad ========

    /**
     * Obtiene el primer email destinatario
     */
    public String getFirstRecipient() {
        if (data != null && data.getTo() != null && !data.getTo().isEmpty()) {
            return data.getTo().get(0);
        }
        return null;
    }

    /**
     * Obtiene el ID del email
     */
    public String getEmailId() {
        return data != null ? data.getEmailId() : null;
    }

    /**
     * Verifica si es un evento de apertura
     */
    public boolean isOpenEvent() {
        return "email.opened".equals(type);
    }

    /**
     * Verifica si es un evento de clic
     */
    public boolean isClickEvent() {
        return "email.clicked".equals(type);
    }

    /**
     * Verifica si es un evento de rebote
     */
    public boolean isBounceEvent() {
        return "email.bounced".equals(type);
    }

    /**
     * Verifica si es un evento de entrega
     */
    public boolean isDeliveredEvent() {
        return "email.delivered".equals(type);
    }

    /**
     * Verifica si es un evento de queja (spam)
     */
    public boolean isComplaintEvent() {
        return "email.complained".equals(type);
    }

    /**
     * Verifica si es un evento de envío
     */
    public boolean isSentEvent() {
        return "email.sent".equals(type);
    }
}