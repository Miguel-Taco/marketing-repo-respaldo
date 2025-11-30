package pe.unmsm.crm.marketing.campanas.telefonicas.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO para agregar un contacto urgente a la cola de llamadas.
 * Usado por el gestor de encuestas para marcar leads que requieren atención
 * inmediata.
 */
@Data
public class AddUrgentContactRequest {

    @NotNull(message = "El idLead es obligatorio")
    private Long idLead;

    @NotNull(message = "El idEncuesta es obligatorio")
    private Integer idEncuesta;
}
