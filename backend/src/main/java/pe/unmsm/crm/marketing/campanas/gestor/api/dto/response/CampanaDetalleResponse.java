package pe.unmsm.crm.marketing.campanas.gestor.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.unmsm.crm.marketing.campanas.gestor.api.dto.EstadoCampanaEnum;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.CanalEjecucion;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.Prioridad;

import java.time.LocalDateTime;

/**
 * DTO de respuesta con el detalle completo de una campaña
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampanaDetalleResponse {

    private Long idCampana;
    private String nombre;
    private String tematica;
    private String descripcion;
    private EstadoCampanaEnum estado;
    private Prioridad prioridad;
    private CanalEjecucion canalEjecucion;
    private LocalDateTime fechaProgramadaInicio;
    private LocalDateTime fechaProgramadaFin;
    private Integer idPlantilla;
    private Integer idAgente;
    private Long idSegmento;
    private Integer idEncuesta;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private Boolean esArchivado;
}
