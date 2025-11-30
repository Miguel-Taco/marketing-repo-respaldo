package pe.unmsm.crm.marketing.campanas.telefonicas.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para llamadas agrupadas por día
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlamadasPorDiaDTO {
    private LocalDate fecha;
    private Long totalLlamadas;
    private Long llamadasEfectivas;
}
