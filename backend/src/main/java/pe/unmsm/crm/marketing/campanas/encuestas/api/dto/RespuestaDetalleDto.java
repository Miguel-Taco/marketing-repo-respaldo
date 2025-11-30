package pe.unmsm.crm.marketing.campanas.encuestas.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RespuestaDetalleDto {

    @NotNull(message = "El ID de la pregunta es obligatorio")
    private Integer idPregunta;

    private Integer idOpcion;

    private Byte valorRespuesta;
}
