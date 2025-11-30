package pe.unmsm.crm.marketing.campanas.telefonicas.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGuionRequest {

    private Long idCampania; // Optional: null for standalone scripts, set for campaign-specific scripts

    @NotBlank(message = "El nombre es requerido")
    private String nombre;

    @NotBlank(message = "El objetivo es requerido")
    private String objetivo;

    @NotBlank(message = "El tipo es requerido")
    private String tipo; // RENOVACION, VENTA_NUEVA, RECUPERO, etc.

    private String notasInternas;

    @NotNull(message = "Las secciones son requeridas")
    private List<SeccionGuionDTO> secciones;
}
