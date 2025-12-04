package pe.unmsm.crm.marketing.security.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO de respuesta para el login exitoso
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String type;
    private String username;
    private Long userId;
    private List<String> roles;
    private Long agentId;
    private String canalPrincipal;
    private Boolean puedeAccederMailing;
    private Boolean puedeAccederTelefonia;
    private List<Long> campaniasMailing;
    private List<Long> campaniasTelefonicas;
    private UserInfoDTO profile;
}
