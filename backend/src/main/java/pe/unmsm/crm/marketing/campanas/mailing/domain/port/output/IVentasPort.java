package pe.unmsm.crm.marketing.campanas.mailing.domain.port.output;

public interface IVentasPort {
    void derivarInteresado(Integer idCampanaMailingId, Integer idAgenteAsignado, Long idLead, Long idSegmento, Long idCampanaGestion);
}
