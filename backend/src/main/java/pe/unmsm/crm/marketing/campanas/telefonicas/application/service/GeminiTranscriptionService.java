package pe.unmsm.crm.marketing.campanas.telefonicas.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;

/**
 * Servicio para transcribir audio usando Gemini 2.5 Flash Lite
 * Lee la configuración desde variables de entorno (.env)
 */
@Service
@Slf4j
public class GeminiTranscriptionService {

    private final String geminiApiKey;
    private final String geminiModel;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(180, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public GeminiTranscriptionService() {
        Dotenv dotenv = Dotenv.configure()
                .directory(".")
                .ignoreIfMissing()
                .load();

        this.geminiApiKey = dotenv.get("GEMINI_API_KEY");
        this.geminiModel = dotenv.get("GEMINI_MODEL", "gemini-2.5-flash-lite");

        if (geminiApiKey == null || geminiApiKey.isEmpty()) {
            log.warn("GEMINI_API_KEY not configured. Transcription service will not be available.");
        } else {
            log.info("Gemini transcription service initialized with model: {}", geminiModel);
        }
    }

    /**
     * Transcribe un archivo de audio usando Gemini API
     *
     * @param audioBytes Contenido del audio en bytes
     * @param metadata   Metadata de la llamada (nombre agente, lead, campaña,
     *                   fecha)
     * @return Transcripción en formato Markdown
     */
    public String transcribirAudio(byte[] audioBytes, Map<String, String> metadata) throws IOException {
        String base64Audio = Base64.getEncoder().encodeToString(audioBytes);

        String prompt = construirPrompt(metadata);

        String requestBody = String.format("""
                {
                    "contents": [{
                        "parts": [
                            {
                                "text": "%s"
                            },
                            {
                                "inline_data": {
                                    "mime_type": "audio/mpeg",
                                    "data": "%s"
                                }
                            }
                        ]
                    }]
                }
                """, prompt.replace("\n", "\\n").replace("\"", "\\\""), base64Audio);

        String url = String.format(
                "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                geminiModel,
                geminiApiKey);

        // Log detallado para debugging
        log.info("=== GEMINI API REQUEST ===");
        log.info("URL: {}", url.replace(geminiApiKey, "***API_KEY***"));
        log.info("Model: {}", geminiModel);
        log.info("Request body length: {} bytes", requestBody.length());

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(
                        requestBody,
                        MediaType.parse("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            log.info("=== GEMINI API RESPONSE ===");
            log.info("Status Code: {}", response.code());
            log.info("Status Message: {}", response.message());

            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Sin detalles";
                log.error("=== GEMINI API ERROR ===");
                log.error("HTTP Status: {}", response.code());
                log.error("Error Response Body:");
                log.error("{}", errorBody);
                log.error("========================");
                throw new IOException("Error al transcribir audio: " + response.code());
            }

            String responseBody = response.body().string();
            log.info("Response body length: {} bytes", responseBody.length());
            log.info("Response preview: {}...", responseBody.substring(0, Math.min(200, responseBody.length())));
            return procesarRespuesta(responseBody, metadata);
        }
    }

    /**
     * Construye el prompt para Gemini basado en los requisitos
     */
    private String construirPrompt(Map<String, String> metadata) {
        return """
                Analiza el siguiente audio de una llamada telefónica de telemarketing y proporciona:

                1. TRANSCRIPCIÓN COMPLETA con marcas de tiempo (formato MM:SS)
                2. IDENTIFICACIÓN DE HABLANTES (Agente y Cliente)
                3. FORMATO DE SALIDA en Markdown

                Estructura requerida:

                # Transcripción de Llamada

                **Campaña:** """ + metadata.getOrDefault("nombreCampania", "N/A") + """

                **Agente:** """ + metadata.getOrDefault("nombreAgente", "N/A") + """

                **Lead:** """ + metadata.getOrDefault("nombreLead", "N/A") + """

                **Fecha:** """ + metadata.getOrDefault("fecha", "N/A") + """

                **Duración:** [duración total en formato MM:SS]

                ## Conversación

                ### [00:00] Agente
                [Texto transcrito del agente]

                ### [00:15] Cliente
                [Texto transcrito del cliente]

                ### [00:30] Agente
                [Texto transcrito del agente]

                [continuar con el patrón...]

                ## Resumen
                - **Resultado:** [breve resumen del resultado de la llamada]
                - **Puntos clave:** [lista de puntos importantes mencionados]
                - **Sentimiento:** [positivo/neutral/negativo]

                Instrucciones:
                - Identifica claramente quién habla en cada momento (Agente vs Cliente)
                - Incluye marcas de tiempo cada vez que cambia el hablante
                - Mantén la precisión en la transcripción
                - El resumen debe ser conciso y objetivo
                - NO incluyas notas, advertencias ni texto fuera del formato especificado
                - SOLO devuelve el markdown formateado
                """;
    }

    /**
     * Procesa la respuesta de Gemini y extrae la transcripción
     */
    private String procesarRespuesta(String responseBody, Map<String, String> metadata) throws IOException {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode candidates = root.path("candidates");

            if (candidates.isEmpty()) {
                throw new IOException("No se recibió transcripción de Gemini");
            }

            JsonNode content = candidates.get(0).path("content");
            JsonNode parts = content.path("parts");

            if (parts.isEmpty()) {
                throw new IOException("Transcripción vacía");
            }

            String transcription = parts.get(0).path("text").asText();

            if (transcription == null || transcription.isBlank()) {
                throw new IOException("Transcripción vacía o nula");
            }

            log.info("Transcripción completada exitosamente. Longitud: {} caracteres", transcription.length());
            return transcription;

        } catch (Exception e) {
            log.error("Error al procesar respuesta de Gemini: {}", e.getMessage());
            throw new IOException("Error al procesar transcripción", e);
        }
    }
}
