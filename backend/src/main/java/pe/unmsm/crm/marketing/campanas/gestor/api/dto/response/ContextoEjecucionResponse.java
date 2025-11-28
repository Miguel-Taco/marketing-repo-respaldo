package pe.unmsm.crm.marketing.campanas.gestor.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.Prioridad;

import java.time.LocalDateTime;

/**
 * DTO de respuesta con el contexto necesario para ejecutar una campaña.
 * Usado por los módulos de Mailing y Llamadas vía endpoint interno.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContextoEjecucionResponse {

    private Long idCampana;
    private String nombre;
    private String tematica;
    private String descripcion;
    private Prioridad prioridad;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Integer idAgente;
    private Long idSegmento;
    private Integer idEncuesta; // ID de encuesta (no URL)
}
