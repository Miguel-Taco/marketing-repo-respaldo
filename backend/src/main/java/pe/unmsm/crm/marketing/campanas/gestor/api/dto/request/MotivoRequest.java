package pe.unmsm.crm.marketing.campanas.gestor.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para enviar motivo en operaciones de pausa y cancelación
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MotivoRequest {

    private String motivo;
}
