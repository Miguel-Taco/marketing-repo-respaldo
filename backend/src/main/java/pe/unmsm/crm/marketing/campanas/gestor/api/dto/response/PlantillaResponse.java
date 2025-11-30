package pe.unmsm.crm.marketing.campanas.gestor.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.CanalEjecucion;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlantillaResponse {
    private Integer idPlantilla;
    private String nombre;
    private String tematica;
    private String descripcion;
    private CanalEjecucion canalEjecucion;
    private Long idSegmento;
    private String nombreSegmento;
    private Integer idEncuesta;
    private String tituloEncuesta;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
}
