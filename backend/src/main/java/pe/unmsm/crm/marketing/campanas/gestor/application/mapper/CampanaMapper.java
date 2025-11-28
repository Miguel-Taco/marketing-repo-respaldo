package pe.unmsm.crm.marketing.campanas.gestor.application.mapper;

import org.springframework.stereotype.Component;
import pe.unmsm.crm.marketing.campanas.gestor.api.dto.EstadoCampanaEnum;
import pe.unmsm.crm.marketing.campanas.gestor.api.dto.request.CrearCampanaRequest;
import pe.unmsm.crm.marketing.campanas.gestor.api.dto.request.CrearPlantillaRequest;
import pe.unmsm.crm.marketing.campanas.gestor.api.dto.response.CampanaDetalleResponse;
import pe.unmsm.crm.marketing.campanas.gestor.api.dto.response.CampanaListItemResponse;
import pe.unmsm.crm.marketing.campanas.gestor.api.dto.response.ContextoEjecucionResponse;
import pe.unmsm.crm.marketing.campanas.gestor.api.dto.response.HistorialItemResponse;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.Campana;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.HistorialCampana;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.PlantillaCampana;

/**
 * Mapper manual para conversiones entre DTOs y entidades de dominio.
 * Usa mapeo manual (sin MapStruct) para mantener consistencia con otros
 * módulos.
 */
@Component
public class CampanaMapper {

    /**
     * Convierte CrearCampanaRequest a entidad Campana
     */
    public Campana toEntity(CrearCampanaRequest request) {
        return Campana.builder()
                .nombre(request.getNombre())
                .tematica(request.getTematica())
                .descripcion(request.getDescripcion())
                .prioridad(request.getPrioridad())
                .canalEjecucion(request.getCanalEjecucion())
                .idAgente(request.getIdAgente())
                .idEncuesta(request.getIdEncuesta())
                .idSegmento(request.getIdSegmento())
                .build();
    }

    /**
     * Convierte Campana a CampanaDetalleResponse
     */
    public CampanaDetalleResponse toDetailResponse(Campana campana) {
        return CampanaDetalleResponse.builder()
                .idCampana(campana.getIdCampana())
                .nombre(campana.getNombre())
                .tematica(campana.getTematica())
                .descripcion(campana.getDescripcion())
                .estado(EstadoCampanaEnum.fromNombre(campana.getEstado().getNombre()))
                .prioridad(campana.getPrioridad())
                .canalEjecucion(campana.getCanalEjecucion())
                .fechaProgramadaInicio(campana.getFechaProgramadaInicio())
                .fechaProgramadaFin(campana.getFechaProgramadaFin())
                .idPlantilla(campana.getIdPlantilla())
                .idAgente(campana.getIdAgente())
                .idSegmento(campana.getIdSegmento())
                .idEncuesta(campana.getIdEncuesta())
                .fechaCreacion(campana.getFechaCreacion())
                .fechaModificacion(campana.getFechaModificacion())
                .esArchivado(campana.getEsArchivado())
                .build();
    }

    /**
     * Convierte Campana a CampanaListItemResponse (DTO ligero para listados)
     */
    public CampanaListItemResponse toListItemResponse(Campana campana) {
        return CampanaListItemResponse.builder()
                .idCampana(campana.getIdCampana())
                .nombre(campana.getNombre())
                .estado(campana.getEstado() != null ? campana.getEstado().getNombre() : "Desconocido")
                .prioridad(campana.getPrioridad() != null ? campana.getPrioridad().name() : "Media")
                .canalEjecucion(campana.getCanalEjecucion() != null ? campana.getCanalEjecucion().name() : "N/A")
                .fechaProgramadaInicio(campana.getFechaProgramadaInicio())
                .fechaProgramadaFin(campana.getFechaProgramadaFin())
                .build();
    }

    /**
     * Convierte HistorialCampana a HistorialItemResponse
     */
    public HistorialItemResponse toHistorialResponse(HistorialCampana historial) {
        return HistorialItemResponse.builder()
                .idHistorial(historial.getIdHistorial())
                .idCampana(historial.getIdCampana())
                .fechaAccion(historial.getFechaAccion())
                .tipoAccion(historial.getTipoAccion().name())
                .descripcionDetalle(historial.getDescripcionDetalle())
                .build();
    }

    /**
     * Convierte Campana a ContextoEjecucionResponse
     * (Solo datos necesarios para Mailing/Llamadas, sin campos sensibles)
     */
    public ContextoEjecucionResponse toContextoResponse(Campana campana) {
        return ContextoEjecucionResponse.builder()
                .idCampana(campana.getIdCampana())
                .nombre(campana.getNombre())
                .tematica(campana.getTematica())
                .descripcion(campana.getDescripcion())
                .prioridad(campana.getPrioridad())
                .fechaInicio(campana.getFechaProgramadaInicio())
                .fechaFin(campana.getFechaProgramadaFin())
                .idAgente(campana.getIdAgente())
                .idSegmento(campana.getIdSegmento())
                .idEncuesta(campana.getIdEncuesta())
                .build();
    }

    /**
     * Convierte CrearPlantillaRequest a entidad PlantillaCampana
     */
    public PlantillaCampana toEntity(CrearPlantillaRequest request) {
        return PlantillaCampana.builder()
                .nombre(request.getNombre())
                .tematica(request.getTematica())
                .descripcion(request.getDescripcion())
                .canalEjecucion(request.getCanalEjecucion())
                .idSegmento(request.getIdSegmento())
                .idEncuesta(request.getIdEncuesta())
                .build();
    }
}
