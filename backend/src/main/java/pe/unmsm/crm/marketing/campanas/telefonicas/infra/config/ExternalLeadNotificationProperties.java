package pe.unmsm.crm.marketing.campanas.telefonicas.infra.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propiedades de configuración para notificación de leads a sistema externo.
 * 
 * Permite configurar el envío automático de datos de leads marcados como
 * INTERESADO
 * a un endpoint externo mediante variables de entorno.
 */
@Data
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
}
