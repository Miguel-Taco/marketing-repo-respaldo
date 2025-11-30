package pe.unmsm.crm.marketing.campanas.mailing.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrearCampanaMailingRequest {

    @NotNull(message = "ID de campaña de gestión es obligatorio")
    private Long idCampanaGestion;

    @NotNull(message = "ID de segmento es obligatorio")
    private Long idSegmento;

    private Integer idEncuesta;

    @NotNull(message = "ID de agente asignado es obligatorio")
    private Integer idAgenteAsignado;

    @NotNull(message = "Nombre es obligatorio")
    private String nombre;

    private String descripcion;

    private String tematica;

    @NotNull(message = "Prioridad es obligatoria")
    private String prioridad;

    @NotNull(message = "Fecha de inicio es obligatoria")
    private LocalDateTime fechaInicio;

    @NotNull(message = "Fecha de fin es obligatoria")
    private LocalDateTime fechaFin;

    @NotBlank(message = "CTA URL es obligatoria")
    private String ctaUrl;

    private String asunto;
    private String cuerpo;
    private String ctaTexto;
}
