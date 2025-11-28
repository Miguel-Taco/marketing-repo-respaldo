package pe.unmsm.crm.marketing.campanas.mailing.domain.port.output;

public interface IMailingSendGridPort {
    void enviarEmails(CampanaMailing campana, List<String> emails);
    void configurarWebhook();
}