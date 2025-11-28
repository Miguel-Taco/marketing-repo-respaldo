package pe.unmsm.crm.marketing.campanas.encuestas.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import pe.unmsm.crm.marketing.campanas.encuestas.domain.model.Encuesta.EstadoEncuesta;
import pe.unmsm.crm.marketing.campanas.encuestas.domain.model.Pregunta.TipoPregunta;
import java.util.List;

@Data
public class CreateEncuestaDto {
    @NotBlank(message = "El título es obligatorio")
    private String titulo;

    private String descripcion;

    @NotNull(message = "El estado es obligatorio")
    private EstadoEncuesta estado;

    @NotEmpty(message = "Debe haber al menos una pregunta")
    @Valid
    private List<PreguntaDto> preguntas;

    @Data
    public static class PreguntaDto {
        @NotBlank(message = "El texto de la pregunta es obligatorio")
        private String textoPregunta;

        @NotNull(message = "El tipo de pregunta es obligatorio")
        private TipoPregunta tipoPregunta;

        @NotNull(message = "El orden es obligatorio")
        private Integer orden;

        @Valid
        private List<OpcionDto> opciones;
    }

    @Data
    public static class OpcionDto {
        @NotBlank(message = "El texto de la opción es obligatorio")
        private String textoOpcion;

        @NotNull(message = "El orden es obligatorio")
        private Integer orden;

        private Boolean esAlertaUrgente;
    }
}
