package pe.unmsm.crm.marketing.campanas.mailing.domain.port.output;

public interface IVentasPort {
    void derivarInteresado(Integer id_campana_mailing, Integer id_lead, Integer id_segmento);
}
