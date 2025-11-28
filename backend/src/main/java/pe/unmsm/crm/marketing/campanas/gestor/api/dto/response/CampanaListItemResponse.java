package pe.unmsm.crm.marketing.campanas.gestor.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO ligero para listado de campañas.
 * Contiene solo los campos necesarios para la vista de tabla.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampanaListItemResponse {

    private Long idCampana;
    private String nombre;
    private String estado; // "Borrador", "Vigente", "Pausada", etc.
    private String prioridad; // "Alta", "Media", "Baja"
    private String canalEjecucion; // "Mailing" o "Llamadas"
    private LocalDateTime fechaProgramadaInicio; // Nullable para campañas en Borrador
    private LocalDateTime fechaProgramadaFin; // Nullable para campañas en Borrador
}
