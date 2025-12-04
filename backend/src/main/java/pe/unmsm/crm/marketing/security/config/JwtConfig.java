package pe.unmsm.crm.marketing.security.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración centralizada para JWT
 */
@Configuration
@Getter
public class JwtConfig {

    @Value("${jwt.secret:MySecretKeyForJWTTokenGenerationAndValidationShouldBeAtLeast256BitsLong}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 24 horas en milisegundos
    private long expiration;

    @Value("${jwt.header:Authorization}")
    private String header;

    @Value("${jwt.prefix:Bearer }")
    private String prefix;
}
