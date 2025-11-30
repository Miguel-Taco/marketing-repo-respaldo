package pe.unmsm.crm.marketing.campanas.encuestas.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RegistrarRespuestaDto {

    @NotNull(message = "El ID del lead es obligatorio")
    private Long leadId;

    @NotNull(message = "El ID de la encuesta es obligatorio")
    private Integer idEncuesta;

    @NotEmpty(message = "Debe proporcionar al menos una respuesta")
    @Valid
    private List<RespuestaDetalleDto> respuestas;
}
