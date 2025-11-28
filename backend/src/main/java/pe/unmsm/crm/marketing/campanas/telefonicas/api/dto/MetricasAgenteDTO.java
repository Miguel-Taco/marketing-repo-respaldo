package pe.unmsm.crm.marketing.campanas.telefonicas.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricasAgenteDTO {
    private Long idAgente;
    private Long idCampania;
    private Integer llamadasRealizadas;
    private Integer contactosEfectivos;
    private Double tasaContacto;
    private Integer duracionPromedio; // en segundos
    private Integer llamadasHoy;
    private Integer llamadasSemana;
    private Integer llamadasMes;
    
    // Distribución de resultados
    private Map<String, Integer> distribucionResultados; // e.g., {"CONTACTADO": 45, "NO_CONTESTA": 30, ...}
    
    // Comparación temporal (para "Tú vs. Tú")
    private MetricasComparativaDTO periodoActual;
    private MetricasComparativaDTO periodoAnterior;
}
