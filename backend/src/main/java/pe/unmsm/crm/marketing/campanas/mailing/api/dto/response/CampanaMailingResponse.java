package pe.unmsm.crm.marketing.campanas.mailing.api.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampanaMailingResponse {
    private Integer id;
    private Long idCampanaGestion;
    private Long idSegmento;
    private Integer idEncuesta;
    private Integer idAgenteAsignado;
    private Integer idEstado;
    private String estadoNombre;
    private String prioridad;
    private String nombre;
    private String descripcion;
    private String tematica;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String asunto;
    private String cuerpo;
    private String ctaTexto;
    private String ctaUrl;
    private String nombreEncuesta;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
