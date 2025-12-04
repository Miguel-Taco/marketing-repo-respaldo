package pe.unmsm.crm.marketing.campanas.gestor.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para items del historial de auditoría
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistorialItemResponse {

    private Long idHistorial;
    private Long idCampana;
    private String nombreCampana;
    private LocalDateTime fechaAccion;
    private String tipoAccion; // CREACION, ACTIVACION, etc.
    private String usuarioResponsable;
    private String descripcionDetalle; // Descripción legible del cambio
}
