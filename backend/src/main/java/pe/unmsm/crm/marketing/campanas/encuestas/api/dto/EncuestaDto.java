package pe.unmsm.crm.marketing.campanas.encuestas.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.unmsm.crm.marketing.campanas.encuestas.domain.model.Encuesta;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EncuestaDto {
    private Integer idEncuesta;
    private String titulo;
    private String descripcion;
    private Encuesta.EstadoEncuesta estado;
    private LocalDateTime fechaModificacion;
    private Long totalRespuestas;
}
