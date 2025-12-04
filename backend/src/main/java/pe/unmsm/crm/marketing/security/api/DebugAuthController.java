package pe.unmsm.crm.marketing.security.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pe.unmsm.crm.marketing.security.domain.UsuarioEntity;
import pe.unmsm.crm.marketing.security.repository.UsuarioRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/debug/auth")
@RequiredArgsConstructor
public class DebugAuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/check")
    public ResponseEntity<Map<String, Object>> checkPassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String rawPassword = request.get("password");

        Map<String, Object> result = new HashMap<>();
        result.put("username", username);
        result.put("rawPassword", rawPassword);

        Optional<UsuarioEntity> userOpt = usuarioRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            UsuarioEntity user = userOpt.get();
            String storedHash = user.getPasswordHash();

            result.put("userFound", true);
            result.put("storedHash", storedHash);
            result.put("hashLength", storedHash != null ? storedHash.length() : 0);
            result.put("passwordMatches", passwordEncoder.matches(rawPassword, storedHash));

            // Generate a new hash for comparison
            String newHash = passwordEncoder.encode(rawPassword);
            result.put("newHashSample", newHash);
        } else {
            result.put("userFound", false);
        }

        return ResponseEntity.ok(result);
    }
}
