package pe.unmsm.crm.marketing.campanas.telefonicas.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para distribución de resultados de llamadas
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoDistribucionDTO {
    private String resultado; // CONTACTADO, NO_CONTESTA, etc.
    private String nombre; // Display name
    private Long count;
    private Double porcentaje;
}
