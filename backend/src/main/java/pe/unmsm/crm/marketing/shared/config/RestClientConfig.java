package pe.unmsm.crm.marketing.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Configuración global de RestClient para comunicación HTTP entre módulos.
 * Usado por los adaptadores HTTP del módulo de campanas.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        return builder.build();
    }
}
