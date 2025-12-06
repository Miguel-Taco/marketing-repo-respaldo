package pe.unmsm.crm.marketing.campanas.telefonicas.infra.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Configuración para notificaciones de leads a sistema externo de ventas.
 * 
 * Lee las propiedades desde application.yml bajo el prefijo:
 * app.external-lead-notification
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "app.external-lead-notification")
public class ExternalLeadNotificationProperties {

    /**
     * Habilita o deshabilita el envío HTTP de notificaciones.
     * Por defecto: false (para que el sistema funcione sin configuración)
     */
    private boolean enabled = false;

    /**
     * URL base del sistema de ventas (se construye desde VENTAS_URL).
     * Ejemplo: https://mod-ventas.onrender.com
     */
    private String ventasUrl;

    /**
     * API Key opcional para autenticación con el sistema externo.
     */
    private String apiKey;

    /**
     * Timeout en segundos para las peticiones HTTP.
     * Por defecto: 5 segundos
     */
    private int timeoutSeconds = 5;

    /**
     * Obtiene la URL completa del endpoint para enviar leads.
     * Construye: {ventasUrl}/api/venta/lead/desde-marketing
     * 
     * @return URL completa del endpoint o null si ventasUrl no está configurado
     */
    public String getEndpointUrl() {
        if (ventasUrl == null || ventasUrl.trim().isEmpty()) {
            return null;
        }
        // Asegurar que no haya doble slash
        String baseUrl = ventasUrl.endsWith("/") ? ventasUrl.substring(0, ventasUrl.length() - 1) : ventasUrl;
        return baseUrl + "/api/venta/lead/desde-marketing";
    }

    /**
     * Log de configuración al iniciar la aplicación.
     */
    @PostConstruct
    public void logConfiguration() {
        log.info("=================================================================");
        log.info("EXTERNAL LEAD NOTIFICATION - Configuración cargada:");
        log.info("  Habilitado: {}", enabled);
        log.info("  Ventas URL base: {}", ventasUrl != null ? ventasUrl : "NO CONFIGURADO");
        log.info("  Endpoint completo: {}", getEndpointUrl() != null ? getEndpointUrl() : "NO CONFIGURADO");
        log.info("  Timeout: {} segundos", timeoutSeconds);
        log.info("  API Key configurada: {}", apiKey != null && !apiKey.trim().isEmpty() ? "SÍ" : "NO");

        if (enabled && getEndpointUrl() == null) {
            log.warn("⚠️  ADVERTENCIA: Notificación externa HABILITADA pero VENTAS_URL no configurado!");
            log.warn("⚠️  Configure VENTAS_URL en variables de entorno para enviar notificaciones.");
        } else if (enabled) {
            log.info("✅ Sistema de notificación externa LISTO para enviar a: {}", getEndpointUrl());
        } else {
            log.info("ℹ️  Notificación externa DESHABILITADA. Para habilitar: EXTERNAL_LEAD_NOTIFICATION_ENABLED=true");
        }
        log.info("=================================================================");
    }
}
