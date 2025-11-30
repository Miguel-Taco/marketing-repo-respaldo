package pe.unmsm.crm.marketing.campanas.telefonicas.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para rendimiento de agente en una campaña
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RendimientoAgenteDTO {
    private Long idAgente;
    private String nombreAgente;
    private Long llamadasRealizadas;
    private Long contactosEfectivos;
    private Double tasaExito; // porcentaje
    private Integer duracionPromedio;
    private Long llamadasHoy;
}
