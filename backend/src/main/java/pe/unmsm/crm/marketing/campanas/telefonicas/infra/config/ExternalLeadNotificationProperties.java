package pe.unmsm.crm.marketing.campanas.telefonicas.infra.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

/**
 * Propiedades de configuración para notificación de leads a sistema externo.
 * 
 * Permite configurar el envío automático de datos de leads marcados como
 * INTERESADO
 * a un endpoint externo mediante variables de entorno.
 */
@Data
@Slf4j
@Component
@ConfigurationProperties(prefix = "app.external-lead-notification")
public class ExternalLeadNotificationProperties {

    /**
     * Habilita o deshabilita el envío de notificaciones al sistema externo.
     * Por defecto: false (deshabilitado)
     */
    private boolean enabled = false;

    /**
     * URL del endpoint externo donde se enviarán los datos del lead.
     * Ejemplo: https://sistema-externo.com/api/leads
     */
    private String endpointUrl;

    /**
     * Timeout en segundos para la petición HTTP.
     * Por defecto: 5 segundos
     */
    private int timeoutSeconds = 5;

    /**
     * API Key opcional para autenticación con el sistema externo.
     */
    private String apiKey;

    /**
     * Log de configuración al iniciar - permite verificar qué valores se cargaron
     */
    @PostConstruct
    public void logConfig() {
        log.info("╔══════════════════════════════════════════════════╗");
        log.info("║  EXTERNAL LEAD NOTIFICATION CONFIG               ║");
        log.info("╠══════════════════════════════════════════════════╣");
        log.info("║  Enabled: {}", enabled);
        log.info("║  Endpoint URL: {}", endpointUrl != null ? endpointUrl : "NO CONFIGURADO");
        log.info("║  Timeout: {} seconds", timeoutSeconds);
        log.info("║  API Key: {}", apiKey != null && !apiKey.isEmpty() ? "CONFIGURADO" : "NO CONFIGURADO");
        log.info("╚══════════════════════════════════════════════════╝");

        if (!enabled) {
            log.warn("⚠ External lead notification DISABLED.");
            log.warn("  To enable, set: EXTERNAL_LEAD_NOTIFICATION_ENABLED=true");
        }

        if (enabled && (endpointUrl == null || endpointUrl.trim().isEmpty())) {
            log.error("❌ External lead notification ENABLED but NO ENDPOINT URL configured!");
            log.error("  Set: EXTERNAL_LEAD_NOTIFICATION_URL=https://your-endpoint.com/api");
        }

        if (enabled && endpointUrl != null && !endpointUrl.trim().isEmpty()) {
            log.info("✅ External lead notification is READY to send data");
        }
    }
}
