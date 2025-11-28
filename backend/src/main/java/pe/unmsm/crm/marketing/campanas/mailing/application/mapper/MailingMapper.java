package pe.unmsm.crm.marketing.campanas.mailing.application.mapper;

import org.springframework.stereotype.Component;
import pe.unmsm.crm.marketing.campanas.mailing.api.dto.request.CrearCampanaMailingRequest;
import pe.unmsm.crm.marketing.campanas.mailing.api.dto.response.*;
import pe.unmsm.crm.marketing.campanas.mailing.domain.model.*;

@Component
public class MailingMapper {

    public CampanaMailing toCampanaMailingEntity(CrearCampanaMailingRequest request) {
        return CampanaMailing.builder()
                .idCampanaGestion(request.getIdCampanaGestion())
                .idSegmento(request.getIdSegmento())
                .idEncuesta(request.getIdEncuesta())
                .idAgenteAsignado(request.getIdAgenteAsignado())
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .tematica(request.getTematica())
                .prioridad(request.getPrioridad())
                .fechaInicio(request.getFechaInicio())
                .fechaFin(request.getFechaFin())
                .ctaUrl(request.getCtaUrl())
                .idEstado(1) // PENDIENTE
                .build();
    }

    public CampanaMailingResponse toCampanaResponse(CampanaMailing campaña) {
        return CampanaMailingResponse.builder()
                .id(campaña.getId())
                .idCampanaGestion(campaña.getIdCampanaGestion())
                .idSegmento(campaña.getIdSegmento())
                .idEncuesta(campaña.getIdEncuesta())
                .idAgenteAsignado(campaña.getIdAgenteAsignado())
                .idEstado(campaña.getIdEstado())
                .estadoNombre(EstadoCampanaMailing.fromId(campaña.getIdEstado()).getNombre())
                .prioridad(campaña.getPrioridad())
                .nombre(campaña.getNombre())
                .descripcion(campaña.getDescripcion())
                .tematica(campaña.getTematica())
                .fechaInicio(campaña.getFechaInicio())
                .fechaFin(campaña.getFechaFin())
                .asunto(campaña.getAsunto())
                .cuerpo(campaña.getCuerpo())
                .ctaTexto(campaña.getCtaTexto())
                .ctaUrl(campaña.getCtaUrl())
                .fechaCreacion(campaña.getFechaCreacion())
                .fechaActualizacion(campaña.getFechaActualizacion())
                .build();
    }

    public MetricasMailingResponse toMetricasResponse(MetricaCampana metricas) {
        Double tasaApertura = metricas.getEnviados() > 0 
            ? (metricas.getAperturas().doubleValue() / metricas.getEnviados()) * 100 
            : 0.0;
        
        Double tasaClics = metricas.getEnviados() > 0 
            ? (metricas.getClics().doubleValue() / metricas.getEnviados()) * 100 
            : 0.0;
        
        Double tasaBajas = metricas.getEnviados() > 0 
            ? (metricas.getBajas().doubleValue() / metricas.getEnviados()) * 100 
            : 0.0;

        return MetricasMailingResponse.builder()
                .id(metricas.getId())
                .idCampanaMailingId(metricas.getCampanaMailing().getId())
                .enviados(metricas.getEnviados())
                .entregados(metricas.getEntregados())
                .aperturas(metricas.getAperturas())
                .clics(metricas.getClics())
                .rebotes(metricas.getRebotes())
                .bajas(metricas.getBajas())
                .tasaApertura(tasaApertura)
                .tasaClics(tasaClics)
                .tasaBajas(tasaBajas)
                .build();
    }

    public InteraccionLogResponse toInteraccionResponse(InteraccionLog interaccion) {
        return InteraccionLogResponse.builder()
                .id(interaccion.getId())
                .idCampanaMailingId(interaccion.getIdCampanaMailingId())
                .idTipoEvento(interaccion.getIdTipoEvento())
                .tipoEventoNombre(TipoInteraccion.fromId(interaccion.getIdTipoEvento()).getNombre())
                .idContactoCrm(interaccion.getIdContactoCrm())
                .fechaEvento(interaccion.getFechaEvento())
                .build();
    }
}
