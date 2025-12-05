package pe.unmsm.crm.marketing.campanas.telefonicas.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para filtros de reportes de campañas telefónicas.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelemarketingReportFilterDTO {

    /**
     * Fecha de inicio del periodo del reporte.
     */
    private LocalDate fechaInicio;

    /**
     * Fecha de fin del periodo del reporte.
     */
    private LocalDate fechaFin;

    /**
     * ID del agente para filtrar (opcional, solo para admins).
     * Si es null, se incluyen todas las llamadas de la campaña.
     */
    private Long idAgente;
}
