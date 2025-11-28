package pe.unmsm.crm.marketing.campanas.mailing.domain.port.input;

import pe.unmsm.crm.marketing.campanas.mailing.api.dto.request.ActualizarContenidoRequest;
import pe.unmsm.crm.marketing.campanas.mailing.api.dto.request.CrearCampanaMailingRequest;
import pe.unmsm.crm.marketing.campanas.mailing.api.dto.response.CampanaMailingResponse;
import pe.unmsm.crm.marketing.campanas.mailing.api.dto.response.MetricasMailingResponse;
import pe.unmsm.crm.marketing.campanas.mailing.domain.model.CampanaMailing;

import java.util.List;

public interface IMailingUseCase {
    CampanaMailing crearCampana(CrearCampanaMailingRequest request);
    List<CampanaMailing> listarPendientes(Integer idAgente);
    List<CampanaMailing> listarListos(Integer idAgente);
    List<CampanaMailing> listarEnviados(Integer idAgente);
    List<CampanaMailing> listarFinalizados(Integer idAgente);
    CampanaMailing obtenerDetalle(Integer idCampana);
    void guardarBorrador(Integer idCampana, ActualizarContenidoRequest request);
    void marcarListo(Integer idCampana);
    MetricasMailingResponse obtenerMetricas(Integer idCampana);
}