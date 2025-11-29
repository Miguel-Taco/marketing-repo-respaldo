package pe.unmsm.crm.marketing.campanas.mailing.infra.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pe.unmsm.crm.marketing.campanas.mailing.domain.model.CampanaMailing;
import pe.unmsm.crm.marketing.campanas.mailing.domain.port.output.IMailingSendGridPort;
import pe.unmsm.crm.marketing.shared.infra.exception.ExternalServiceException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class SendGridAdapter implements IMailingSendGridPort {

    @Value("${app.sendgrid.api-key}")
    private String sendGridApiKey;

    @Value("${app.sendgrid.from-email:noreply@crm.local}")
    private String fromEmail;

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void enviarEmails(CampanaMailing campaña, List<String> emails) {
        if (emails == null || emails.isEmpty()) {
            throw new ExternalServiceException("SendGrid", "Lista de emails vacía");
        }

        SendGrid sg = new SendGrid(sendGridApiKey);

        for (String email : emails) {
            try {
                String body = construirJson(campaña, email);

                Request request = new Request();
                request.setMethod(Method.POST);
                request.setEndpoint("mail/send");
                request.setBody(body);

                Response response = sg.api(request);

                if (response.getStatusCode() < 200 || response.getStatusCode() >= 300) {
                    log.error("SendGrid error: {} - {}", response.getStatusCode(), response.getBody());
                    throw new ExternalServiceException("SendGrid",
                            "Error al enviar email: " + response.getStatusCode());
                }

                log.debug("✓ Email enviado a: {}", email);

            } catch (IOException e) {
                log.error("IOException al enviar email a {}: {}", email, e.getMessage());
                throw new ExternalServiceException("SendGrid", "Error IO: " + e.getMessage());
            }
        }
    }

    private String construirJson(CampanaMailing campaña, String destinatario) throws IOException {
        Map<String, Object> mail = new HashMap<>();

        // FROM
        Map<String, Object> from = new HashMap<>();
        from.put("email", fromEmail);
        from.put("name", "Marketing CRM");
        mail.put("from", from);

        // PERSONALIZATION + CUSTOM ARGS ✅ NUEVO
        Map<String, Object> personalization = new HashMap<>();
        personalization.put("subject", campaña.getAsunto());

        List<Map<String, String>> toList = List.of(
                Map.of("email", destinatario)
        );
        personalization.put("to", toList);
        
        // ✅ AGREGAR METADATA (para identificar campaña en webhook)
        Map<String, String> customArgs = new HashMap<>();
        customArgs.put("campaign_id", String.valueOf(campaña.getId()));
        customArgs.put("agent_id", String.valueOf(campaña.getIdAgenteAsignado()));
        customArgs.put("segment_id", String.valueOf(campaña.getIdSegmento()));
        customArgs.put("campaign_gestion_id", String.valueOf(campaña.getIdCampanaGestion()));
        personalization.put("custom_args", customArgs);

        mail.put("personalizations", List.of(personalization));

        // CONTENT
        String htmlCompleto = construirHtmlConCTA(campaña);
        
        List<Map<String, String>> contentList = List.of(
                Map.of("type", "text/html", "value", htmlCompleto)
        );
        mail.put("content", contentList);

        return mapper.writeValueAsString(mail);
    }

    /**
     * Construye el HTML del correo incluyendo el botón CTA
     */
    private String construirHtmlConCTA(CampanaMailing campaña) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    %s
                    <div style="margin-top: 30px; text-align: center;">
                        <a href="%s" 
                        style="display: inline-block; padding: 12px 30px; background-color: #007bff; 
                                color: white; text-decoration: none; border-radius: 5px; font-weight: bold;">
                            %s
                        </a>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                campaña.getCuerpo(),
                campaña.getCtaUrl(),
                campaña.getCtaTexto()
            );
    }
}