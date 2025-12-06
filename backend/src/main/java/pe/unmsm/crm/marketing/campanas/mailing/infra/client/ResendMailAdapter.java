package pe.unmsm.crm.marketing.campanas.mailing.infra.client;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.unmsm.crm.marketing.campanas.mailing.domain.model.CampanaMailing;
import pe.unmsm.crm.marketing.campanas.mailing.domain.model.EmailMetadata;
import pe.unmsm.crm.marketing.campanas.mailing.domain.port.output.IMailingPort;
import pe.unmsm.crm.marketing.campanas.mailing.domain.port.output.ILeadPort;
import pe.unmsm.crm.marketing.campanas.mailing.infra.config.ResendConfig;
import pe.unmsm.crm.marketing.campanas.mailing.infra.persistence.repository.JpaEmailMetadataRepository;
import pe.unmsm.crm.marketing.shared.infra.exception.ExternalServiceException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Adapter para envío de emails usando Resend.
 * 
 * FLUJO DE TRACKING Y REDIRECCIÓN A ENCUESTA:
 * 1. Usuario recibe email con botón CTA
 * 2. Botón apunta a: /api/v1/mailing/track/click?cid={campanaId}&email={email}&redirect={urlEncuesta}
 * 3. Backend registra el clic y deriva a Ventas
 * 4. Backend redirige al usuario a la encuesta: /q/{idEncuesta}/{idLead}
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ResendMailAdapter implements IMailingPort {

    private final Resend resend;
    private final ResendConfig resendConfig;
    private final JpaEmailMetadataRepository emailMetadataRepo;
    private final ILeadPort leadPort;

    @Override
    public void enviarEmails(CampanaMailing campana, List<String> emails) {
        if (emails == null || emails.isEmpty()) {
            throw new ExternalServiceException("Resend", "Lista de emails vacía");
        }

        log.info("╔════════════════════════════════════════════════════════════╗");
        log.info("║           INICIANDO ENVÍO CON RESEND                       ║");
        log.info("╠════════════════════════════════════════════════════════════╣");
        log.info("║ Campaña: {} (ID: {})", campana.getNombre(), campana.getId());
        log.info("║ Encuesta ID: {}", campana.getIdEncuesta());
        log.info("║ Total destinatarios: {}", emails.size());
        log.info("║ From: {}", resendConfig.getFormattedFrom());
        log.info("║ Backend URL: {}", resendConfig.getBackendUrl());
        log.info("║ Frontend URL: {}", resendConfig.getFrontendUrl());
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
            // ✅ BUSCAR LEAD_ID ANTES DE CONSTRUIR EL HTML
            Long leadId = leadPort.findLeadIdByEmail(destinatario);
            
            if (leadId == null) {
                log.warn("    ⚠ No se encontró lead_id para {}, se enviará sin tracking de encuesta", destinatario);
            } else {
                log.debug("    ✓ Lead encontrado: {} -> lead_id: {}", destinatario, leadId);
            }

            // ✅ CONSTRUIR HTML CON LA URL DE ENCUESTA CORRECTA
            String htmlContent = construirHtmlConTracking(campana, destinatario, leadId);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(resendConfig.getFormattedFrom())
                    .to(destinatario)
                    .subject(campana.getAsunto())
                    .html(htmlContent)
                    .build();

            CreateEmailResponse response = resend.emails().send(params);
            
            String resendEmailId = response.getId();
            log.debug("    Email ID de Resend: {}", resendEmailId);

            // Guardar metadata para mapear webhooks con campañas
            guardarEmailMetadata(resendEmailId, campana, destinatario, leadId);

        } catch (ResendException e) {
            log.error("ResendException enviando a {}: {}", destinatario, e.getMessage());
            throw new ExternalServiceException("Resend", "Error al enviar: " + e.getMessage());
        }
    }

    /**
     * Guarda la metadata del email enviado.
     */
    private void guardarEmailMetadata(String resendEmailId, CampanaMailing campana, 
                                     String destinatario, Long leadId) {
        try {
            EmailMetadata metadata = EmailMetadata.builder()
                    .resendEmailId(resendEmailId)
                    .idCampanaMailing(campana.getId())
                    .emailDestinatario(destinatario)
                    .idLead(leadId)
                    .fechaEnvio(LocalDateTime.now())
                    .build();

            emailMetadataRepo.save(metadata);
            log.debug("    ✓ Metadata guardada: {} -> campaña {}, lead {}", 
                resendEmailId, campana.getId(), leadId);

        } catch (Exception e) {
            log.warn("    ⚠ No se pudo guardar metadata para {}: {}", destinatario, e.getMessage());
        }
    }

    /**
     * ✅ MÉTODO CORREGIDO: Construye el HTML con tracking de clics
     * 
     * CAMBIO PRINCIPAL: Ahora recibe leadId como parámetro y construye 
     * la URL de encuesta correctamente
     */
    private String construirHtmlConTracking(CampanaMailing campana, String destinatario, Long leadId) {
        // ✅ CONSTRUIR URL DE ENCUESTA (donde el usuario será redirigido)
        String urlEncuesta = construirUrlEncuesta(campana, leadId);
        
        log.debug("    URL Encuesta construida: {}", urlEncuesta);
        
        // ✅ URL DE TRACKING (pasa por nuestro backend para registrar clic)
        String trackingClickUrl = String.format(
            "%s/api/v1/mailing/track/click?cid=%d&email=%s&redirect=%s",
            resendConfig.getBackendUrl(),
            campana.getId(),
            URLEncoder.encode(destinatario, StandardCharsets.UTF_8),
            URLEncoder.encode(urlEncuesta, StandardCharsets.UTF_8) // ✅ Aquí va la URL de la encuesta
        );

        // URL para unsubscribe
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
                        <!-- Header -->
                        <div style="background: linear-gradient(135deg, #3C83F6 0%%, #2563EB 100%%); padding: 30px 20px; text-align: center;">
                            <h1 style="color: white; margin: 0; font-size: 24px; font-weight: 600;">
                                %s
                            </h1>
                        </div>
                        
                        <!-- Contenido -->
                        <div style="padding: 30px 25px;">
                            %s
                        </div>
                        
                        <!-- Botón CTA -->
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
                        
                        <!-- Separador -->
                        <div style="border-top: 1px solid #eee; margin: 0 25px;"></div>
                        
                        <!-- Footer -->
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
                    
                    <!-- Copyright -->
                    <div style="text-align: center; padding: 20px; color: #999; font-size: 11px;">
                        <p style="margin: 0;">
                            © 2025 Marketing CRM - Universidad Nacional Mayor de San Marcos
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                campana.getAsunto(),           // Title
                campana.getNombre(),           // Header H1
                campana.getCuerpo(),           // Contenido
                trackingClickUrl,              // ✅ URL del botón (pasa por tracking)
                campana.getCtaTexto(),         // Texto del botón
                campana.getNombre(),           // Nombre campaña en footer
                unsubscribeUrl                 // Link unsubscribe
            );
    }

    private String construirUrlEncuesta(CampanaMailing campana, Long leadId) {
        Integer idEncuesta = campana.getIdEncuesta();
        
        log.debug("");
        log.debug("    ╔═══════════════════════════════════════════════════════╗");
        log.debug("    ║  CONSTRUCCIÓN DE URL DE ENCUESTA                      ║");
        log.debug("    ╠═══════════════════════════════════════════════════════╣");
        log.debug("    ║  ID Encuesta: {}", idEncuesta);
        log.debug("    ║  Lead ID: {}", leadId);
        log.debug("    ║  CTA URL: {}", campana.getCtaUrl());
        log.debug("    ║  Frontend URL config: {}", resendConfig.getFrontendUrl());
        log.debug("    ╚═══════════════════════════════════════════════════════╝");
        
        // Si no hay encuesta configurada, usar la URL original del CTA
        if (idEncuesta == null || idEncuesta == 0) {
            log.warn("    ⚠️  PROBLEMA: No hay encuesta configurada (idEncuesta = {})", idEncuesta);
            log.warn("       Se usará ctaUrl como fallback: {}", campana.getCtaUrl());
            String fallbackUrl = campana.getCtaUrl() != null ? campana.getCtaUrl() : resendConfig.getFrontendUrl();
            log.warn("       URL final fallback: {}", fallbackUrl);
            return fallbackUrl;
        }
        
        // Si no tenemos el leadId, enviar solo a la encuesta sin leadId
        if (leadId == null) {
            log.warn("    ⚠️  ADVERTENCIA: No se encontró lead_id para este email");
            log.warn("       La encuesta no podrá vincular respuestas al lead");
            String url = String.format("%s/q/%d", resendConfig.getFrontendUrl(), idEncuesta);
            log.info("    ✓ URL construida SIN lead: {}", url);
            return url;
        }
        
        // ✅ FORMATO CORRECTO: /q/{idEncuesta}/{idLead}
        String urlEncuesta = String.format("%s/q/%d/%d", 
            resendConfig.getFrontendUrl(), 
            idEncuesta, 
            leadId
        );
        
        log.info("    ✅ URL ENCUESTA CORRECTA: {}", urlEncuesta);
        return urlEncuesta;
    }
}
