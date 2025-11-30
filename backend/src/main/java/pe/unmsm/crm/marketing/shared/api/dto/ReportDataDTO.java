package pe.unmsm.crm.marketing.shared.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

/**
 * DTO genérico para pasar datos a las plantillas de reportes PDF.
 * Permite configurar información básica del reporte y datos dinámicos.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDataDTO {

    /** Título principal del reporte */
    private String reportTitle;

    /** Descripción o subtítulo del reporte */
    private String reportDescription;

    /** Usuario que generó el reporte */
    private String generatedBy;

    /** Fecha de generación del reporte */
    private LocalDate generatedDate;

    /** Fecha de inicio del rango de datos */
    private LocalDate startDate;

    /** Fecha de fin del rango de datos */
    private LocalDate endDate;

    /**
     * Datos dinámicos del reporte (métricas, filtros, tablas, etc.)
     * Las claves del mapa corresponden a placeholders en la plantilla HTML
     * Ejemplo: {"totalLlamadas": 1234, "tasaExito": "45.2%"}
     */
    private Map<String, Object> data;
}
