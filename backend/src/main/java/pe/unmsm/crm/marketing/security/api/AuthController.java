package pe.unmsm.crm.marketing.security.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.unmsm.crm.marketing.security.api.dto.LoginRequest;
import pe.unmsm.crm.marketing.security.api.dto.LoginResponse;
import pe.unmsm.crm.marketing.security.api.dto.UserInfoDTO;
import pe.unmsm.crm.marketing.security.service.AuthenticationService;

/**
 * Controlador REST para endpoints de autenticación
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j

public class AuthController {

    private final AuthenticationService authenticationService;

    /**
     * Endpoint de login
     *
     * @param loginRequest credenciales del usuario
     * @return token JWT y datos del usuario
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Solicitud de login recibida para usuario: {}", loginRequest.getUsername());
        LoginResponse response = authenticationService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para obtener información del usuario actual
     *
     * @return información del usuario autenticado
     */
    @GetMapping("/me")
    public ResponseEntity<UserInfoDTO> getCurrentUser() {
        UserInfoDTO userInfo = authenticationService.getCurrentUser();
        return ResponseEntity.ok(userInfo);
    }

    /**
     * Endpoint de logout (por ahora solo devuelve 200)
     * En el futuro se puede implementar blacklist de tokens
     *
     * @return confirmación de logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        log.info("Usuario cerró sesión");
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
