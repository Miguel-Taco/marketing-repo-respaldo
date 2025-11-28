package pe.unmsm.crm.marketing.campanas.mailing.domain.port.output;

public interface IGestorCampanaPort {
    void pausarCampana(Long idCampanaGestion, String motivo);
}
