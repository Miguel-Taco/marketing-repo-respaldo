package pe.unmsm.crm.marketing.campanas.mailing.infra.client;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pe.unmsm.crm.marketing.campanas.mailing.domain.model.CampanaMailing;
import pe.unmsm.crm.marketing.campanas.mailing.domain.port.output.IMailingPort;
import pe.unmsm.crm.marketing.campanas.mailing.infra.config.ResendConfig;
import pe.unmsm.crm.marketing.shared.infra.exception.ExternalServiceException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@RequiredArgsConstructor  // <-- Cambiar a RequiredArgsConstructor
@Slf4j
public class ResendMailAdapter implements IMailingPort {

    private final Resend resend;  // <-- Inyectar el Bean
    private final ResendConfig resendConfig;  // <-- Inyectar config

    @Override
    public void enviarEmails(CampanaMailing campana, List<String> emails) {
        if (emails == null || emails.isEmpty()) {
            throw new ExternalServiceException("Resend", "Lista de emails vacía");
        }

        log.info("╔════════════════════════════════════════════════════════════╗");
        log.info("║           INICIANDO ENVÍO CON RESEND                       ║");
        log.info("╠════════════════════════════════════════════════════════════╣");
        log.info("║ Campaña: {} (ID: {})", campana.getNombre(), campana.getId());
        log.info("║ Total destinatarios: {}", emails.size());
        log.info("║ From: {}", resendConfig.getFormattedFrom());
        log.info("╚════════════════════════════════════════════════════════════╝");

        int enviados = 0;
        int fallidos = 0;

        for (String email : emails) {
            try {
                enviarEmailIndividual(campana, email);
                enviados++;
                log.debug("  ✓ Email enviado a: {}", email);
            } catch (Exception e) {
                fallidos++;
                log.error("  ✗ Error enviando a {}: {}", email, e.getMessage());
            }
        }

        log.info("╔════════════════════════════════════════════════════════════╗");
        log.info("║           ENVÍO COMPLETADO                                 ║");
        log.info("╠════════════════════════════════════════════════════════════╣");
        log.info("║ Enviados exitosamente: {}", enviados);
        log.info("║ Fallidos: {}", fallidos);
        log.info("╚════════════════════════════════════════════════════════════╝");

        if (enviados == 0 && fallidos > 0) {
            throw new ExternalServiceException("Resend", 
                "No se pudo enviar ningún email. Total fallidos: " + fallidos);
        }
    }

    private void enviarEmailIndividual(CampanaMailing campana, String destinatario) {
        try {
            String htmlContent = construirHtmlConTracking(campana, destinatario);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(resendConfig.getFormattedFrom())
                    .to(destinatario)
                    .subject(campana.getAsunto())
                    .html(htmlContent)
                    .build();

            CreateEmailResponse response = resend.emails().send(params);
            
            log.debug("    Email ID de Resend: {}", response.getId());

        } catch (ResendException e) {
            log.error("ResendException enviando a {}: {}", destinatario, e.getMessage());
            throw new ExternalServiceException("Resend", "Error al enviar: " + e.getMessage());
        }
    }

    private String construirHtmlConTracking(CampanaMailing campana, String destinatario) {
        String trackingUrl = String.format(
            "%s/api/v1/mailing/track/click?cid=%d&email=%s&redirect=%s",
            resendConfig.getBackendUrl(),
            campana.getId(),
            URLEncoder.encode(destinatario, StandardCharsets.UTF_8),
            URLEncoder.encode(campana.getCtaUrl(), StandardCharsets.UTF_8)
        );

        String unsubscribeUrl = String.format(
            "%s/api/v1/mailing/track/unsubscribe?cid=%d&email=%s",
            resendConfig.getBackendUrl(),
            campana.getId(),
            URLEncoder.encode(destinatario, StandardCharsets.UTF_8)
        );

        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s</title>
            </head>
            <body style="font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f5f5f5;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <div style="background-color: white; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); overflow: hidden;">
                        <div style="background: linear-gradient(135deg, #3C83F6 0%%, #2563EB 100%%); padding: 30px 20px; text-align: center;">
                            <h1 style="color: white; margin: 0; font-size: 24px; font-weight: 600;">
                                %s
                            </h1>
                        </div>
                        <div style="padding: 30px 25px;">
                            %s
                        </div>
                        <div style="padding: 0 25px 30px; text-align: center;">
                            <a href="%s" 
                               style="display: inline-block; 
                                      padding: 14px 40px; 
                                      background: linear-gradient(135deg, #3C83F6 0%%, #2563EB 100%%); 
                                      color: white; 
                                      text-decoration: none; 
                                      border-radius: 25px; 
                                      font-weight: 600; 
                                      font-size: 16px;
                                      box-shadow: 0 4px 15px rgba(60, 131, 246, 0.4);">
                                %s
                            </a>
                        </div>
                        <div style="border-top: 1px solid #eee; margin: 0 25px;"></div>
                        <div style="padding: 20px 25px; text-align: center; color: #999; font-size: 12px;">
                            <p style="margin: 0 0 10px 0;">
                                Este correo fue enviado por Marketing CRM - UNMSM
                            </p>
                            <p style="margin: 0 0 10px 0;">
                                Campaña: %s
                            </p>
                            <p style="margin: 0;">
                                <a href="%s" 
                                   style="color: #999; text-decoration: underline;">
                                    Cancelar suscripción
                                </a>
                            </p>
                        </div>
                    </div>
                    <div style="text-align: center; padding: 20px; color: #999; font-size: 11px;">
                        <p style="margin: 0;">
                            © 2025 Marketing CRM - Universidad Nacional Mayor de San Marcos
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                campana.getAsunto(),
                campana.getNombre(),
                campana.getCuerpo(),
                trackingUrl,
                campana.getCtaTexto(),
                campana.getNombre(),
                unsubscribeUrl
            );
    }
}