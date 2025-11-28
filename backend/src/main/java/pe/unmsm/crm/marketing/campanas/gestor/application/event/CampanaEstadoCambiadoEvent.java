package pe.unmsm.crm.marketing.campanas.gestor.application.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Evento de dominio que se publica cuando una campaña cambia de estado.
 * Utilizado para registrar auditoría en el historial.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampanaEstadoCambiadoEvent {

    private Long idCampana;
    private String estadoAnterior;
    private String estadoNuevo;
    private String tipoAccion; // ACTIVACION, PAUSA, etc.
    private LocalDateTime timestamp;
    private String motivo; // Opcional, ej. razón de cancelación
}
