package pe.unmsm.crm.marketing.campanas.telefonicas.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import pe.unmsm.crm.marketing.campanas.telefonicas.api.dto.ExternalLeadDTO;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.config.ExternalLeadNotificationProperties;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.entity.CampaniaTelefonicaEntity;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.entity.LlamadaEntity;
import pe.unmsm.crm.marketing.leads.domain.model.Lead;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Servicio para notificar leads interesados a sistema externo.
 * 
 * Envía datos de leads marcados como INTERESADO a un endpoint HTTP externo
 * de forma asíncrona para no afectar el flujo principal de registro de
 * llamadas.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalLeadNotificationService {

    private final ExternalLeadNotificationProperties properties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT);

    /**
     * Notifica un lead interesado al sistema externo de forma asíncrona.
     * 
     * @param llamada  Entidad de la llamada registrada
     * @param campania Campaña telefónica asociada
     * @param lead     Lead que fue marcado como interesado
     */
    @Async
    public void notificarLeadInteresado(
            LlamadaEntity llamada,
            CampaniaTelefonicaEntity campania,
            Lead lead) {

        log.info("----------------------------------------------------------------");
        log.info("       NOTIFICACION EXTERNA - LEAD INTERESADO                   ");
        log.info("----------------------------------------------------------------");
        log.info("  Lead ID: {}", lead.getId());
        log.info("  Campania: {} (ID: {})", campania.getNombre(), campania.getId());
        log.info("  Canal: CAMPANIA_TELEFONICA");
        log.info("----------------------------------------------------------------");

        try {
            // Construir payload
            ExternalLeadDTO payload = construirPayload(llamada, campania, lead);

            // Verificar configuración
            if (!properties.isEnabled()) {
                log.warn("  [WARN] Notificación HTTP DESHABILITADA");
                log.warn("    El payload se generó correctamente pero NO se enviará HTTP");
                log.warn("    Para habilitar: EXTERNAL_LEAD_NOTIFICATION_ENABLED=true");
                logPayloadDetallado(payload);
                return;
            }

            if (properties.getEndpointUrl() == null || properties.getEndpointUrl().trim().isEmpty()) {
                log.error("  [ERROR] URL de endpoint NO CONFIGURADA");
                log.error("    Configure VENTAS_URL en variables de entorno");
                logPayloadDetallado(payload);
                return;
            }

            // Log del endpoint y payload antes de enviar
            log.info("  -> Endpoint: {}", properties.getEndpointUrl());
            log.info("  -> Timeout: {} segundos", properties.getTimeoutSeconds());

            // Mostrar payload detallado
            logPayloadDetallado(payload);

            // Enviar HTTP
            enviarHttp(payload);

            log.info("  [SUCCESS] Notificación enviada exitosamente");
            log.info("    Lead ID: {}", lead.getId());
            log.info("    Endpoint: {}", properties.getEndpointUrl());

        } catch (Exception e) {
            log.error("  [ERROR] Error al procesar notificación externa");
            log.error("    Lead ID: {}", lead.getId());
            log.error("    Error: {}", e.getMessage());
            if (log.isDebugEnabled()) {
                log.error("    Stacktrace:", e);
            }
        } finally {
            log.info("----------------------------------------------------------------");
        }
    }

    /**
     * Guarda el payload JSON en un archivo de texto para debugging.
     */
    private void guardarPayloadEnArchivo(ExternalLeadDTO payload) {
        try {
            // Crear directorio si no existe
            Path logsDir = Paths.get("logs", "external-lead-notifications");
            Files.createDirectories(logsDir);

            // Generar nombre de archivo con timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("lead-%d-%s.txt", payload.getIdLeadMarketing(), timestamp);
            Path filePath = logsDir.resolve(fileName);

            // Escribir JSON al archivo
            String jsonContent = objectMapper.writeValueAsString(payload);
            Files.writeString(filePath, jsonContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            log.info("  [FILE] JSON guardado en: {}", filePath.toAbsolutePath());

        } catch (IOException e) {
            log.warn("  [WARN] No se pudo guardar JSON en archivo: {}", e.getMessage());
        }
    }

    /**
     * Log detallado del payload en formato estructurado
     */
    private void logPayloadDetallado(ExternalLeadDTO payload) {
        try {
            log.info("  ---------------------------------------------------------");
            log.info("  |  PAYLOAD JSON                                         |");
            log.info("  ---------------------------------------------------------");
            log.info("  |  idLeadMarketing: {}", payload.getIdLeadMarketing());
            log.info("  |  nombres: '{}'", payload.getNombres());
            log.info("  |  apellidos: '{}'", payload.getApellidos());
            log.info("  |  correo: '{}'", payload.getCorreo());
            log.info("  |  telefono: '{}'", payload.getTelefono());
            log.info("  |  dni: '{}'", payload.getDni());
            log.info("  |  canalOrigen: '{}'", payload.getCanalOrigen());
            log.info("  |  idCampaniaMarketing: {}", payload.getIdCampaniaMarketing());
            log.info("  |  nombreCampania: '{}'", payload.getNombreCampania());
            log.info("  |  tematica: '{}'", payload.getTematica());
            log.info("  |  descripcion: '{}'", truncate(payload.getDescripcion(), 40));
            log.info("  |  notas_llamada: '{}'", truncate(payload.getNotasLlamada(), 40));
            log.info("  |  fecha_envio: {}", payload.getFechaEnvio());
            log.info("  ---------------------------------------------------------");

            // JSON completo para copiar/pegar
            String jsonPayload = objectMapper.writeValueAsString(payload);
            log.info("  JSON Completo (Copy-Paste):\n{}", jsonPayload);

            // Guardar en archivo para debugging
            guardarPayloadEnArchivo(payload);

        } catch (Exception e) {
            log.warn("  Error al loguear payload: {}", e.getMessage());
        }
    }

    /**
     * Construye el payload de datos para enviar al sistema externo.
     */
    private ExternalLeadDTO construirPayload(
            LlamadaEntity llamada,
            CampaniaTelefonicaEntity campania,
            Lead lead) {

        // Limpiar y separar nombre completo en nombres y apellidos
        String[] nombreApellido = limpiarYSepararNombre(lead.getNombre());
        String nombres = nombreApellido[0];
        String apellidos = nombreApellido[1];

        return ExternalLeadDTO.builder()
                .idLeadMarketing(lead.getId())
                .nombres(nombres)
                .apellidos(apellidos)
                .correo(lead.getContacto() != null ? lead.getContacto().getEmail() : null)
                .telefono(lead.getContacto() != null ? lead.getContacto().getTelefono() : null)
                .dni(null)
                .canalOrigen("CAMPANIA_TELEFONICA")
                .idCampaniaMarketing(campania.getId().longValue())
                .nombreCampania(campania.getNombre())
                .tematica(campania.getNombre())
                .descripcion(campania.getNombre())
                .notasLlamada(llamada.getNotas())
                .fechaEnvio(LocalDateTime.now())
                .build();
    }

    /**
     * Limpia y separa el nombre completo en nombres y apellidos.
     * 
     * Reglas:
     * 1. Elimina sufijos como "- Import 9", "- import 24", números sueltos, etc.
     * 2. Después de limpiar:
     * - 4 palabras: 2 primeras = nombre, 2 últimas = apellido
     * - 3 palabras: 1 primera = nombre, 2 últimas = apellido
     * - 2 palabras: 1 = nombre, 1 = apellido
     * - 1 palabra o menos: nombre completo como nombre, "Sin Apellido" como
     * apellido
     * 
     * @param nombreCompleto Nombre completo del lead
     * @return Array [nombres, apellidos]
     */
    private String[] limpiarYSepararNombre(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) {
            return new String[] { "Sin Nombre", "Sin Apellido" };
        }

        // Limpiar el nombre
        String nombreLimpio = nombreCompleto
                // Eliminar sufijos como "- Import 9", "- import 24", etc.
                .replaceAll("(?i)\\s*-\\s*import\\s*\\d+", "")
                // Eliminar números sueltos al final
                .replaceAll("\\s+\\d+$", "")
                // Eliminar espacios múltiples
                .replaceAll("\\s+", " ")
                .trim();

        // Si después de limpiar está vacío
        if (nombreLimpio.isEmpty()) {
            return new String[] { "Sin Nombre", "Sin Apellido" };
        }

        // Dividir en palabras
        String[] palabras = nombreLimpio.split("\\s+");
        int numPalabras = palabras.length;

        String nombres;
        String apellidos;

        if (numPalabras >= 4) {
            // 4 o más palabras: 2 primeras = nombre, resto = apellido
            nombres = palabras[0] + " " + palabras[1];
            apellidos = String.join(" ", java.util.Arrays.copyOfRange(palabras, 2, numPalabras));
        } else if (numPalabras == 3) {
            // 3 palabras: 1 primera = nombre, 2 últimas = apellido
            nombres = palabras[0];
            apellidos = palabras[1] + " " + palabras[2];
        } else if (numPalabras == 2) {
            // 2 palabras: 1 = nombre, 1 = apellido
            nombres = palabras[0];
            apellidos = palabras[1];
        } else {
            // 1 palabra: usar como nombre y "Sin Apellido"
            nombres = palabras[0];
            apellidos = "Sin Apellido";
        }

        return new String[] { nombres, apellidos };
    }

    /**
     * Realiza el envío HTTP POST al endpoint externo.
     */
    private void enviarHttp(ExternalLeadDTO payload) {
        RestClient.RequestHeadersSpec<?> request = restClient.post()
                .uri(properties.getEndpointUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload);

        // Agregar API Key si está configurada
        if (properties.getApiKey() != null && !properties.getApiKey().trim().isEmpty()) {
            request = request.header("Authorization", "Bearer " + properties.getApiKey());
            log.info("  -> Usando API Key configurada");
        }

        // Ejecutar request y capturar respuesta
        try {
            org.springframework.http.ResponseEntity<String> response = request
                    .retrieve()
                    .toEntity(String.class);

            log.info("  ---------------------------------------------------------");
            log.info("  |  RESPUESTA DEL ENDPOINT EXTERNO                     |");
            log.info("  ---------------------------------------------------------");
            log.info("  |  Status Code: {}", response.getStatusCode());
            log.info("  |  Status: {}", response.getStatusCode().is2xxSuccessful() ? "✓ SUCCESS" : "✗ ERROR");

            if (response.hasBody() && response.getBody() != null) {
                String responseBody = response.getBody();
                log.info("  |  Response Body:");
                log.info("  |  {}", responseBody);
            } else {
                log.info("  |  Response Body: (vacío)");
            }

            log.info("  ---------------------------------------------------------");

        } catch (Exception e) {
            log.error("  ---------------------------------------------------------");
            log.error("  |  ERROR EN RESPUESTA DEL ENDPOINT                    |");
            log.error("  ---------------------------------------------------------");
            log.error("  |  Error: {}", e.getMessage());
            log.error("  ---------------------------------------------------------");
            throw e; // Re-lanzar para que se maneje en el try-catch superior
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null)
            return "null";
        if (text.length() <= maxLength)
            return text;
        return text.substring(0, maxLength) + "...";
    }

}