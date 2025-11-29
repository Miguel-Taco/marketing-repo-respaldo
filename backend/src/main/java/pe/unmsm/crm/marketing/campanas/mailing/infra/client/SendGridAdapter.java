package pe.unmsm.crm.marketing.campanas.mailing.infra.client;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pe.unmsm.crm.marketing.campanas.mailing.domain.model.CampanaMailing;
import pe.unmsm.crm.marketing.campanas.mailing.domain.port.output.IMailingSendGridPort;
import pe.unmsm.crm.marketing.shared.infra.exception.ExternalServiceException;
import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SendGridAdapter implements IMailingSendGridPort {

    @Value("${app.sendgrid.api-key}")
    private String sendGridApiKey;

    @Value("${app.sendgrid.from-email:noreply@crm.local}")
    private String fromEmail;

    @Override
    public void enviarEmails(CampanaMailing campaña, List<String> emails) {
        if (emails == null || emails.isEmpty()) {
            throw new ExternalServiceException("SendGrid", "Lista de emails vacía");
        }

        SendGrid sg = new SendGrid(sendGridApiKey);
        
        for (String email : emails) {
            try {
                Mail mail = construirMail(campaña, email);
                Request request = new Request();
                request.setMethod(Method.POST);
                request.setEndpoint("mail/send");
                request.setBody(mail.build());
                
                Response response = sg.api(request);
                
                if (response.getStatusCode() < 200 || response.getStatusCode() >= 300) {
                    log.error("SendGrid error: {} - {}", response.getStatusCode(), response.getBody());
                    throw new ExternalServiceException("SendGrid", 
                        "Error al enviar email: " + response.getStatusCode());
                }
                
                log.debug("Email enviado a: {}", email);
                
            } catch (IOException e) {
                log.error("IOException al enviar email a {}: {}", email, e.getMessage());
                throw new ExternalServiceException("SendGrid", "Error IO: " + e.getMessage());
            }
        }
    }

    private Mail construirMail(CampanaMailing campaña, String destinatario) {
        Mail mail = new Mail();
        
        // Remitente
        mail.setFrom(new Email(fromEmail, "Marketing CRM"));
        
        // Personalization (destinatario)
        Personalization p = new Personalization();
        p.addTo(new Email(destinatario));
        p.setSubject(campaña.getAsunto());
        mail.addPersonalization(p);
        
        // Contenido
        Content content = new Content("text/html", campaña.getCuerpo());
        mail.addContent(content);
        
        // CTA Button (si lo quieres embebido en HTML, ya está en cuerpo)
        // Si necesitas tracking adicional:
        mail.setTrackingSettings(new TrackingSettings()
                .setClickTracking(new ClickTracking().setEnable(true))
                .setOpenTracking(new OpenTracking().setEnable(true))
                .setUnsubscribeTracking(new UnsubscribeTracking()
                        .setEnable(true)
                        .setHtml("<a href='{{unsubscribe}}'>Unsubscribe</a>")));
        
        return mail;
    }
}