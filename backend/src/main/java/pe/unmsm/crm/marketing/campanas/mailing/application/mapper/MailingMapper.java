package pe.unmsm.crm.marketing.campanas.mailing.application.mapper;

import org.springframework.stereotype.Component;
import pe.unmsm.crm.marketing.campanas.mailing.api.dto.request.CrearCampanaMailingRequest;
import pe.unmsm.crm.marketing.campanas.mailing.api.dto.response.*;
import pe.unmsm.crm.marketing.campanas.mailing.domain.model.*;

@Component
public class MailingMapper {

    public CampanaMailing toEntity(CrearCampanaMailingRequest req) {
        return CampanaMailing.builder()
                .idCampanaGestion(req.getIdCampanaGestion())
                .idSegmento(req.getIdSegmento())
                .idEncuesta(req.getIdEncuesta())
                .idAgenteAsignado(req.getIdAgenteAsignado())
                .nombre(req.getNombre())
                .descripcion(req.getDescripcion())
                .tematica(req.getTematica())
                .prioridad(req.getPrioridad())
                .fechaInicio(req.getFechaInicio())
                .fechaFin(req.getFechaFin())
                .ctaUrl(req.getCtaUrl())
                .idEstado(1) // PENDIENTE
                .build();
    }

    public CampanaMailingResponse toResponse(CampanaMailing c) {
        return CampanaMailingResponse.builder()
                .id(c.getId())
                .idCampanaGestion(c.getIdCampanaGestion())
                .idSegmento(c.getIdSegmento())
                .idEncuesta(c.getIdEncuesta())
                .idAgenteAsignado(c.getIdAgenteAsignado())
                .idEstado(c.getIdEstado())
                .estadoNombre(EstadoCampanaMailing.fromId(c.getIdEstado()).getNombre())
                .prioridad(c.getPrioridad())
                .nombre(c.getNombre())
                .descripcion(c.getDescripcion())
                .tematica(c.getTematica())
                .fechaInicio(c.getFechaInicio())
                .fechaFin(c.getFechaFin())
                .asunto(c.getAsunto())
                .cuerpo(c.getCuerpo())
                .ctaTexto(c.getCtaTexto())
                .ctaUrl(c.getCtaUrl())
                .fechaCreacion(c.getFechaCreacion())
                .fechaActualizacion(c.getFechaActualizacion())
                .build();
    }

    public MetricasMailingResponse toMetricasResponse(MetricaCampana m) {
        double tasaApertura = m.getEnviados() > 0 
            ? (m.getAperturas().doubleValue() / m.getEnviados()) * 100 : 0.0;
        double tasaClics = m.getEnviados() > 0 
            ? (m.getClics().doubleValue() / m.getEnviados()) * 100 : 0.0;
        double tasaBajas = m.getEnviados() > 0 
            ? (m.getBajas().doubleValue() / m.getEnviados()) * 100 : 0.0;

        return MetricasMailingResponse.builder()
                .id(m.getId())
                .idCampanaMailingId(m.getCampanaMailing().getId())
                .enviados(m.getEnviados())
                .entregados(m.getEntregados())
                .aperturas(m.getAperturas())
                .clics(m.getClics())
                .rebotes(m.getRebotes())
                .bajas(m.getBajas())
                .tasaApertura(tasaApertura)
                .tasaClics(tasaClics)
                .tasaBajas(tasaBajas)
                .build();
    }
}
