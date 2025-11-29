package pe.unmsm.crm.marketing.campanas.mailing.domain.port.output;

public interface ILeadPort {
    Long findLeadIdByEmail(String email);
}