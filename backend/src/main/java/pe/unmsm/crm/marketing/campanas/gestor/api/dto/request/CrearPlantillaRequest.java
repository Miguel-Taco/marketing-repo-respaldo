package pe.unmsm.crm.marketing.campanas.gestor.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.CanalEjecucion;

/**
 * DTO para crear una plantilla de campaña
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrearPlantillaRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "La temática es obligatoria")
    private String tematica;

    // Opcionales
    private String descripcion;
    private CanalEjecucion canalEjecucion;
    private Long idSegmento;
    private Integer idEncuesta;
}
