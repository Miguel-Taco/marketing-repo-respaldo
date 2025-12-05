package pe.unmsm.crm.marketing.campanas.telefonicas.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO para métricas completas de una campaña telefónica
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricasCampaniaDTO {
    // Resumen general
    private Long totalLeads;
    private Long leadsContactados;
    private Long leadsPendientes;
    private Double porcentajeAvance;
    private Long totalLlamadas;
    private Integer duracionPromedio; // segundos

    // Distribución de resultados
    private Map<String, ResultadoDistribucionDTO> distribucionResultados;

    // Análisis temporal
    private List<LlamadasPorDiaDTO> llamadasPorDia;
    private Map<Integer, Long> llamadasPorHora; // hora (0-23) -> count

    // Rendimiento por agente
    private List<RendimientoAgenteDTO> rendimientoPorAgente;

    // Métricas de calidad
    private Double tasaContactoGlobal;
    private Double tasaEfectividad;
    private Integer duracionPromedioEfectivas;
    private Integer duracionPromedioNoEfectivas;
    private Long llamadasEfectivas;
    private Long llamadasNoEfectivas;
    private Long llamadasPendientes;
    private Double promedioLlamadasDiarias;

    // Estado de cola
    private Map<String, Long> leadsPorPrioridad; // ALTA, MEDIA, BAJA
    private Map<String, Long> leadsPorEstado; // PENDIENTE, EN_PROCESO, COMPLETADO
}
