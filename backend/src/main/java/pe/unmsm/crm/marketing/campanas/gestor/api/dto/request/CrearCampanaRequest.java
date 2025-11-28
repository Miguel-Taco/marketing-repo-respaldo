package pe.unmsm.crm.marketing.campanas.gestor.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.CanalEjecucion;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.Prioridad;

/**
 * DTO para crear una nueva campaña en estado Borrador
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrearCampanaRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "La temática es obligatoria")
    private String tematica;

    private String descripcion;

    @NotNull(message = "La prioridad es obligatoria")
    private Prioridad prioridad;

    @NotNull(message = "El canal de ejecución es obligatorio")
    private CanalEjecucion canalEjecucion;

    // Opcionales en borrador
    private Integer idAgente;
    private Long idSegmento;
    private Integer idEncuesta;
}
