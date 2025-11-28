package pe.unmsm.crm.marketing.campanas.gestor.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.CanalEjecucion;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.Prioridad;

/**
 * DTO para editar una campaña existente (solo Borrador/Pausada)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditarCampanaRequest {

    // Todos los campos son opcionales
    private String nombre;
    private String tematica;
    private String descripcion;
    private CanalEjecucion canalEjecucion;
    private Prioridad prioridad;
    private Integer idAgente;
    private Long idSegmento;
    private Integer idEncuesta;
}
