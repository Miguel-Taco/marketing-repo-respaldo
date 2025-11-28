package pe.unmsm.crm.marketing.campanas.mailing.domain.port.input;

public interface IMailingUseCase {
    CampanaMailing crearCampana(CrearCampanaMailingRequest req);
    List<CampanaMailing> listarPendientes(Integer idAgente);
    CampanaMailing obtenerDetalle(Integer idCampana);
    void guardarBorrador(Integer idCampana, ActualizarContenidoRequest req);
    void marcarListo(Integer idCampana);
    MetricasCampana obtenerMetricas(Integer idCampana);
}