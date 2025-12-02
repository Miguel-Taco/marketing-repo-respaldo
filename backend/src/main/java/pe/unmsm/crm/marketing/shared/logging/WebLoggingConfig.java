package pe.unmsm.crm.marketing.shared.logging;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración web para registrar interceptores globales.
 */
@Configuration
public class WebLoggingConfig implements WebMvcConfigurer {

    private final PerformanceInterceptor performanceInterceptor;

    public WebLoggingConfig(PerformanceInterceptor performanceInterceptor) {
        this.performanceInterceptor = performanceInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Registrar el interceptor de performance para todas las rutas
        registry.addInterceptor(performanceInterceptor);
    }
}
