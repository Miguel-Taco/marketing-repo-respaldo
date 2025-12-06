package pe.unmsm.crm.marketing.campanas.mailing.infra.config;

import com.resend.Resend;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Configuración de Resend para el servicio de email.
 * 
 * Las credenciales se leen desde application.yml o variables de entorno.
 * 
 * Variables requeridas:
 * - RESEND_API_KEY: API Key de Resend (obtenida en https://resend.com/api-keys)
 * - RESEND_FROM_EMAIL: Email remitente (default: onboarding@resend.dev para free tier)
 * - RESEND_FROM_NAME: Nombre del remitente
 * - BACKEND_URL: URL del backend para tracking (importante para clics)
 */
@Configuration
@Getter
@Slf4j
public class ResendConfig {

    @Value("${app.resend.api-key}")
    private String apiKey;

    @Value("${app.resend.from-email:onboarding@resend.dev}")
    private String fromEmail;

    @Value("${app.resend.from-name:Marketing CRM UNMSM}")
    private String fromName;

    @Value("${app.resend.webhook-secret:}")
    private String webhookSecret;

    @Value("${app.backend.url:http://localhost:8080}")
    private String backendUrl;

    @Value("${app.encuestas_frontend.url:http://localhost:5173}")
    private String frontendUrl;    

    /**
     * Log de configuración al iniciar
     */
    @PostConstruct
    public void logConfig() {
        log.info("╔══════════════════════════════════════════════════╗");
        log.info("║  RESEND CONFIGURADO                              ║");
        log.info("╠══════════════════════════════════════════════════╣");
        log.info("║  From: {} <{}>", fromName, fromEmail);
        log.info("║  Backend URL: {}", backendUrl);
        log.info("║  API Key: {}...", apiKey.substring(0, Math.min(10, apiKey.length())));
        log.info("║  Webhook Secret: {}", webhookSecret.isEmpty() ? "NO CONFIGURADO" : "CONFIGURADO");
        log.info("╚══════════════════════════════════════════════════╝");
        
        if (webhookSecret.isEmpty()) {
            log.warn("⚠ RESEND_WEBHOOK_SECRET no configurado. Los webhooks no serán validados.");
            log.warn("  Configúralo en https://resend.com/webhooks después del deploy.");
        }
        
        if (backendUrl.contains("localhost")) {
            log.warn("⚠ BACKEND_URL apunta a localhost. El tracking de clics no funcionará externamente.");
            log.warn("  Actualiza BACKEND_URL con tu URL de producción después del deploy.");
        }
    }

    /**
     * Bean de Resend para inyección de dependencias.
     * Puede ser usado directamente si necesitas acceso al cliente de Resend.
     */
    @Bean
    public Resend resend() {
        return new Resend(apiKey);
    }

    /**
     * Obtiene el remitente formateado: "Nombre <email>"
     */
    public String getFormattedFrom() {
        return fromName + " <" + fromEmail + ">";
    }

    /**
     * Verifica si el webhook secret está configurado
     */
    public boolean isWebhookSecretConfigured() {
        return webhookSecret != null && !webhookSecret.isEmpty();
    }

    /**
     * Verifica si está en modo producción (URL no es localhost)
     */
    public boolean isProductionMode() {
        return !backendUrl.contains("localhost");
    }
}
