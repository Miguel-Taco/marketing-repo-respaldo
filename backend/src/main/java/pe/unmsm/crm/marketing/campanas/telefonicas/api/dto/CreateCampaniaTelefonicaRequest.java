package pe.unmsm.crm.marketing.campanas.telefonicas.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateCampaniaTelefonicaRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate fechaFin;

    @NotNull(message = "El ID del segmento es obligatorio")
    private Long idSegmento;

    @NotNull(message = "El ID de la campaña de gestión es obligatorio")
    private Long idCampanaGestion;

    @NotBlank(message = "El estado es obligatorio")
    private String estado; // BORRADOR, ACTIVA, FINALIZADA

    @NotNull(message = "El ID del guion es obligatorio")
    private Long idGuion;

    @NotNull(message = "Los IDs de agentes son obligatorios")
    private List<Long> idsAgentes;

    @NotNull(message = "Los leads iniciales son obligatorios")
    private List<Long> leadsIniciales;

    private String prioridadColaDefault; // ALTA, MEDIA, BAJA
}
