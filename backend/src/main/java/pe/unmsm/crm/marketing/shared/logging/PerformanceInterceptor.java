package pe.unmsm.crm.marketing.shared.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor para medir el rendimiento de las peticiones HTTP.
 * Registra el tiempo de ejecución de cada request en el logger "PERFORMANCE".
 */
@Component
public class PerformanceInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger("PERFORMANCE");
    private static final String START_TIME_ATTRIBUTE = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // Guardar tiempo de inicio
        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME_ATTRIBUTE, startTime);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        // Calcular duración
        Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;

            // Loguear métricas: method, uri, status, durationMs
            logger.info("method={}, uri={}, status={}, durationMs={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    duration);
        }
    }
}
