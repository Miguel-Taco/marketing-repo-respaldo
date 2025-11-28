package pe.unmsm.crm.marketing.campanas.encuestas.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.unmsm.crm.marketing.campanas.encuestas.domain.model.Encuesta.EstadoEncuesta;
import pe.unmsm.crm.marketing.campanas.encuestas.domain.model.Pregunta.TipoPregunta;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta completa para una encuesta con todas sus preguntas y
 * opciones.
 * Usado para evitar problemas de serialización JSON con entidades JPA.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EncuestaCompletaDto {

    private Integer idEncuesta;
    private String titulo;
    private String descripcion;
    private EstadoEncuesta estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private Long totalRespuestas;
    private List<PreguntaDto> preguntas;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PreguntaDto {
        private Integer idPregunta;
        private String textoPregunta;
        private TipoPregunta tipoPregunta;
        private Integer orden;
        private List<OpcionDto> opciones;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OpcionDto {
        private Integer idOpcion;
        private String textoOpcion;
        private Integer orden;
        private Boolean esAlertaUrgente;
    }
}
