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

        try {
            log.info("Preparando notificación de lead interesado [leadId={}, campaniaId={}]",
                    lead.getId(), campania.getId());

            // Construir payload SIEMPRE (independiente de configuración)
            ExternalLeadDTO payload = construirPayload(llamada, campania, lead);

            // TESTING: Guardar archivo SIEMPRE para facilitar debugging
            guardarPayloadEnArchivo(payload);

            // TESTING: Mostrar payload completo en consola SIEMPRE
            log.info("==================== PAYLOAD PARA SISTEMA EXTERNO ====================");
            log.info("Endpoint: {}",
                    properties.getEndpointUrl() != null ? properties.getEndpointUrl() : "NO CONFIGURADO");
            log.info("Payload JSON:");
            log.info("  idLeadMarketing: {}", payload.getIdLeadMarketing());
            log.info("  nombres: {}", payload.getNombres());
            log.info("  apellidos: {}", payload.getApellidos());
            log.info("  correo: {}", payload.getCorreo());
            log.info("  telefono: {}", payload.getTelefono());
            log.info("  dni: {}", payload.getDni());
            log.info("  canalOrigen: {}", payload.getCanalOrigen());
            log.info("  idCampaniaMarketing: {}", payload.getIdCampaniaMarketing());
            log.info("  nombreCampania: {}", payload.getNombreCampania());
            log.info("  tematica: {}", payload.getTematica());
            log.info("  descripcion: {}", payload.getDescripcion());
            log.info("  notas_llamada: {}", payload.getNotasLlamada());
            log.info("  fecha_envio: {}", payload.getFechaEnvio());

            // Mostrar JSON completo formateado (para copiar y pegar en tests)
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                mapper.enable(SerializationFeature.INDENT_OUTPUT);
                String jsonPayload = mapper.writeValueAsString(payload);
                log.info("JSON Completo (Copy-Paste):\n{}", jsonPayload);
            } catch (Exception e) {
                log.warn("No se pudo serializar payload a JSON: {}", e.getMessage());
            }

            log.info("======================================================================");

            // Verificar si la notificación HTTP está habilitada
            if (!properties.isEnabled()) {
                log.info(
                        "Notificación HTTP externa DESHABILITADA. Payload guardado en archivo pero no se enviará HTTP.");
                log.info("Para habilitar, configurar: EXTERNAL_LEAD_NOTIFICATION_ENABLED=true");
                return;
            }

            // Validar que exista URL configurada
            if (properties.getEndpointUrl() == null || properties.getEndpointUrl().trim().isEmpty()) {
                log.warn(
                        "URL de endpoint externo NO CONFIGURADA. Payload guardado en archivo pero no se enviará HTTP.");
                log.warn("Para configurar, agregar: EXTERNAL_LEAD_NOTIFICATION_URL=https://tu-endpoint.com/api");
                return;
            }

            // Enviar al sistema externo
            enviarHttp(payload);

            log.info("Notificación enviada exitosamente al sistema externo [leadId={}, endpoint={}]",
                    lead.getId(), properties.getEndpointUrl());

        } catch (Exception e) {
            // No lanzar excepción para no afectar el flujo principal
            log.error("Error al procesar notificación externa [leadId={}]: {}",
                    lead.getId(), e.getMessage(), e);
        }
    }

    /**
     * Construye el payload de datos para enviar al sistema externo.
     */
    private ExternalLeadDTO construirPayload(
            LlamadaEntity llamada,
            CampaniaTelefonicaEntity campania,
            Lead lead) {

        // Parsear nombre completo a nombres y apellidos
        String[] nombreParseado = parsearNombreCompleto(lead.getNombre());
        String nombres = nombreParseado[0];
        String apellidos = nombreParseado[1];

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
     * Parsea un nombre completo y lo divide en nombres y apellidos.
     * 
     * Lógica:
     * 1. Elimina números, guiones "-" y la palabra "import" (case insensitive)
     * 2. Divide las palabras restantes según cantidad:
     * - 4+ palabras: primeras 2 = nombres, últimas 2 = apellidos
     * - 3 palabras: primera = nombre, últimas 2 = apellidos
     * - 2 palabras: primera = nombre, segunda = apellido
     * - 1 palabra: nombre sin apellido
     * 
     * Ejemplos:
     * - "Ana Soto 9" -> nombres="Ana", apellidos="Soto"
     * - "Luis Pacheco - Import 10" -> nombres="Luis", apellidos="Pacheco"
     * - "Maria Elena Garcia Torres" -> nombres="Maria Elena", apellidos="Garcia
     * Torres"
     * - "Juan Carlos Lopez" -> nombres="Juan", apellidos="Carlos Lopez"
     * 
     * @param nombreCompleto Nombre completo a parsear
     * @return Array de 2 elementos: [nombres, apellidos]
     */
    private String[] parsearNombreCompleto(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) {
            return new String[] { "", "" };
        }

        // Limpiar el nombre:
        // 1. Eliminar números
        // 2. Eliminar guiones
        // 3. Eliminar la palabra "import" (case insensitive)
        String nombreLimpio = nombreCompleto
                .replaceAll("\\d+", "") // Eliminar números
                .replaceAll("-", " ") // Reemplazar guiones por espacios
                .replaceAll("(?i)\\bimport\\b", "") // Eliminar "import" (case insensitive)
                .replaceAll("\\s+", " ") // Normalizar espacios múltiples
                .trim();

        // Dividir en palabras
        String[] palabras = nombreLimpio.split("\\s+");

        // Filtrar palabras vacías
        palabras = java.util.Arrays.stream(palabras)
                .filter(p -> !p.isEmpty())
                .toArray(String[]::new);

        if (palabras.length == 0) {
            return new String[] { "", "" };
        }

        String nombres;
        String apellidos;

        switch (palabras.length) {
            case 1:
                // Solo 1 palabra: es el nombre, sin apellido
                nombres = palabras[0];
                apellidos = "";
                break;
            case 2:
                // 2 palabras: primera es nombre, segunda es apellido
                nombres = palabras[0];
                apellidos = palabras[1];
                break;
            case 3:
                // 3 palabras: primera es nombre, últimas 2 son apellidos
                nombres = palabras[0];
                apellidos = palabras[1] + " " + palabras[2];
                break;
            default:
                // 4+ palabras: primeras 2 son nombres, últimas son apellidos
                nombres = palabras[0] + " " + palabras[1];
                // Unir el resto como apellidos (puede ser más de 2)
                StringBuilder apellidosBuilder = new StringBuilder();
                for (int i = 2; i < palabras.length; i++) {
                    if (i > 2)
                        apellidosBuilder.append(" ");
                    apellidosBuilder.append(palabras[i]);
                }
                apellidos = apellidosBuilder.toString();
                break;
        }

        log.debug("Nombre parseado: '{}' -> nombres='{}', apellidos='{}'",
                nombreCompleto, nombres, apellidos);

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
        }

        // Ejecutar request con timeout
        request.retrieve()
                .toBodilessEntity();

        log.debug("POST enviado a {} con payload: {}", properties.getEndpointUrl(), payload);
    }

    /**
     * Guarda el payload en un archivo de texto para testing.
     * El archivo se crea en:
     * C:\Users\marec\Desktop\Wankas_v2\external-leads-payload.txt
     */
    private void guardarPayloadEnArchivo(ExternalLeadDTO payload) {
        try {
            // Ruta del archivo
            Path filePath = Paths.get("C:\\Users\\marec\\Desktop\\Wankas_v2\\external-leads-payload.txt");

            // Crear archivo si no existe
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
                log.info("Archivo de payloads creado en: {}", filePath.toAbsolutePath());
            }

            // Preparar contenido
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            StringBuilder content = new StringBuilder();

            content.append("\n");
            content.append("================================================================================\n");
            content.append("TIMESTAMP: ").append(LocalDateTime.now().format(formatter)).append("\n");
            content.append("LEAD ID: ").append(payload.getIdLeadMarketing()).append("\n");
            content.append("CAMPAÑA: ").append(payload.getNombreCampania()).append("\n");
            content.append("================================================================================\n");

            // JSON formateado
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            String jsonPayload = mapper.writeValueAsString(payload);

            content.append("PAYLOAD JSON:\n");
            content.append(jsonPayload);
            content.append("\n\n");

            // Escribir al archivo (append)
            Files.writeString(filePath, content.toString(), StandardOpenOption.APPEND);

            log.info("Payload guardado en archivo: {}", filePath.toAbsolutePath());

        } catch (IOException e) {
            log.error("Error al guardar payload en archivo: {}", e.getMessage());
        }
    }
}
