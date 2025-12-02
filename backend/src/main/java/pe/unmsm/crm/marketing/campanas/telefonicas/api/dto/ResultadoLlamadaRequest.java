package pe.unmsm.crm.marketing.campanas.telefonicas.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResultadoLlamadaRequest {

    @NotNull(message = "El ID del contacto es obligatorio")
    private Long idContacto;

    private Long idContactoCola; // ID en la tabla cola_llamada

    @NotNull(message = "El ID del lead es obligatorio")
    private Long idLead;

    private Long idResultado; // ID del resultado en catálogo

    @NotBlank(message = "El resultado es obligatorio")
    private String resultado;

    private String motivo;

    private String notas;

    private LocalDateTime fechaReagendamiento;

    private Boolean derivadoVentas;

    private String tipoOportunidad;

    @NotNull(message = "La duración es obligatoria")
    private Integer duracionSegundos;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDateTime inicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDateTime fin;

    // Encuesta post-llamada
    private Boolean enviarEncuesta;
}
