package pe.unmsm.crm.marketing.campanas.mailing.domain.port.output;

import pe.unmsm.crm.marketing.campanas.mailing.domain.model.CampanaMailing;
import java.util.List;

public interface IMailingSendGridPort {
    void enviarEmails(CampanaMailing campaña, List<String> emails);
}