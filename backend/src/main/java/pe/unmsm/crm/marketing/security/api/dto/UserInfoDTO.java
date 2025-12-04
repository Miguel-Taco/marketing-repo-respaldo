package pe.unmsm.crm.marketing.security.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO con información del usuario autenticado
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {

    private Long userId;
    private String username;
    private Boolean activo;
    private List<String> roles;
    private Long agentId;
    private String canalPrincipal;
    private Boolean puedeAccederMailing;
    private Boolean puedeAccederTelefonia;
    private List<Long> campaniasMailing;
    private List<Long> campaniasTelefonicas;
}
