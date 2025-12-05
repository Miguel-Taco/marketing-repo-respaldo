package pe.unmsm.crm.marketing.campanas.mailing.infra.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import pe.unmsm.crm.marketing.campanas.mailing.api.dto.request.LeadVentasRequest;
import pe.unmsm.crm.marketing.campanas.mailing.domain.port.output.IVentasPort;

/**
 * Adapter para integración con el módulo de Ventas.
 * 
 * ENDPOINT DE VENTAS: POST /api/venta/lead/desde-marketing
 * 
 * Este adapter se encarga de:
 * 1. Construir el payload con el formato exacto que espera Ventas
 * 2. Enviar la petición HTTP POST
 * 3. Manejar errores de forma resiliente (no romper el flujo principal)
 * 4. Registrar logs para debugging
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class VentasAdapter implements IVentasPort {

    private final RestClient restClient;

    /**
     * URL base del módulo de Ventas.
     * 
     * IMPORTANTE: Actualiza esto en application.yml o como variable de entorno
     * cuando tengas la URL real del backend de Ventas.
     */
    @Value("${app.ventas.url:http://localhost:8080}")
    private String ventasBaseUrl;

    /**
     * Endpoint específico para recibir leads desde Marketing
     */
    private static final String ENDPOINT_LEAD_DESDE_MARKETING = "/api/venta/lead/desde-marketing";

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    /**
     * Deriva un lead interesado al módulo de Ventas.
     * 
     * Este método se llama cuando un destinatario hace clic en el CTA del correo,
     * indicando interés en la oferta de la campaña.
     * 
     * @param request DTO con toda la información requerida por Ventas
     * @return true si se derivó correctamente, false si hubo error
     */
    @Override
    public boolean derivarLeadInteresado(LeadVentasRequest request) {
        log.info("╔══════════════════════════════════════════════════════════════╗");
        log.info("║          DERIVANDO LEAD A VENTAS                             ║");
        log.info("╠══════════════════════════════════════════════════════════════╣");
        log.info("║  Lead ID: {}", request.getIdLeadMarketing());
        log.info("║  Email: {}", request.getCorreo());
        log.info("║  Campaña: {} (ID: {})", request.getNombreCampania(), request.getIdCampaniaMarketing());
        log.info("║  Canal: {}", request.getCanalOrigen());
        log.info("╚══════════════════════════════════════════════════════════════╝");

        // Validar request antes de enviar
        if (!request.esValido()) {
            log.error("  ✗ Request inválido: faltan campos obligatorios");
            logRequestDetallado(request);
            return false;
        }

        String url = ventasBaseUrl + ENDPOINT_LEAD_DESDE_MARKETING;
        log.info("  → URL: {}", url);

        try {
            // Log del payload para debugging
            logRequestDetallado(request);

            // Enviar petición a Ventas
            var response = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();

            HttpStatus status = (HttpStatus) response.getStatusCode();

            if (status.is2xxSuccessful()) {
                log.info("  ✓ Lead derivado exitosamente a Ventas");
                log.info("    Status: {}", status.value());
                return true;
            } else {
                log.warn("  ⚠ Respuesta inesperada de Ventas: {}", status.value());
                return false;
            }

        } catch (HttpClientErrorException e) {
            // Errores 4xx (bad request, not found, etc.)
            log.error("  ✗ Error de cliente al derivar a Ventas: {} - {}", 
                e.getStatusCode(), e.getResponseBodyAsString());
            logRequestDetallado(request);
            return false;

        } catch (HttpServerErrorException e) {
            // Errores 5xx (server error)
            log.error("  ✗ Error del servidor de Ventas: {} - {}", 
                e.getStatusCode(), e.getResponseBodyAsString());
            return false;

        } catch (RestClientException e) {
            // Errores de conexión, timeout, etc.
            log.error("  ✗ Error de conexión con Ventas: {}", e.getMessage());
            log.warn("    Verifica que la URL {} sea correcta y el servicio esté disponible", url);
            return false;

        } catch (Exception e) {
            // Cualquier otro error
            log.error("  ✗ Error inesperado al derivar a Ventas: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Método legacy mantenido por compatibilidad.
     * Se recomienda usar derivarLeadInteresado(LeadVentasRequest) en su lugar.
     */
    @Override
    @Deprecated
    public void derivarInteresado(Integer idCampanaMailingId, Integer idAgenteAsignado, 
                                   Long idLead, Long idSegmento, Long idCampanaGestion) {
        log.warn("⚠ Usando método deprecado derivarInteresado()");
        log.warn("  Se recomienda usar derivarLeadInteresado(LeadVentasRequest)");
        
        // Este método ya no debería usarse, pero lo mantenemos por si hay código legacy
        // No hace nada porque necesitamos más datos que los que recibe
    }

    /**
     * Log detallado del request para debugging
     */
    private void logRequestDetallado(LeadVentasRequest request) {
        try {
            log.debug("  Payload completo:");
            log.debug("  {");
            log.debug("    idLeadMarketing: {}", request.getIdLeadMarketing());
            log.debug("    nombres: '{}'", request.getNombres());
            log.debug("    apellidos: '{}'", request.getApellidos());
            log.debug("    correo: '{}'", request.getCorreo());
            log.debug("    telefono: '{}'", request.getTelefono());
            log.debug("    canalOrigen: '{}'", request.getCanalOrigen());
            log.debug("    idCampaniaMarketing: {}", request.getIdCampaniaMarketing());
            log.debug("    nombreCampania: '{}'", request.getNombreCampania());
            log.debug("    tematica: '{}'", request.getTematica());
            log.debug("    descripcion: '{}'", request.getDescripcion() != null ? 
                (request.getDescripcion().length() > 50 ? 
                    request.getDescripcion().substring(0, 50) + "..." : 
                    request.getDescripcion()) : "null");
            log.debug("    notasLlamada: '{}'", request.getNotasLlamada() != null ?
                (request.getNotasLlamada().length() > 50 ? 
                    request.getNotasLlamada().substring(0, 50) + "..." : 
                    request.getNotasLlamada()) : "null");
            log.debug("    fechaEnvio: {}", request.getFechaEnvio());
            log.debug("  }");
        } catch (Exception e) {
            log.debug("  Error al loguear request: {}", e.getMessage());
        }
    }
}