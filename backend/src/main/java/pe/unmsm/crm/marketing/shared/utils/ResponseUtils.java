package pe.unmsm.crm.marketing.shared.utils;

import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@UtilityClass
public class ResponseUtils {

    // Respuesta estándar para éxito (200 OK)
    public static <T> ResponseEntity<Map<String, Object>> success(T data, String message) {
        return buildResponse(HttpStatus.OK, "OK", message, data);
    }

    // Respuesta para procesos asíncronos/staging (202 Accepted)
    public static <T> ResponseEntity<Map<String, Object>> accepted(T data, String message) {
        return buildResponse(HttpStatus.ACCEPTED, "ACCEPTED", message, data);
    }

    // Método privado para construir el Map
    private static <T> ResponseEntity<Map<String, Object>> buildResponse(@NonNull HttpStatus status, String statusText,
            String message, T data) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", statusText);
        body.put("message", message);
        body.put("timestamp", Instant.now());
        if (data != null) {
            body.put("data", data);
        }
        return ResponseEntity.status(Objects.requireNonNull(status)).body(body);
    }

    // Mantiene tu método existente para errores
    public ProblemDetail fromException(String code, String message, @NonNull HttpStatus status,
            HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(Objects.requireNonNull(status), message);
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("code", code);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }
}
