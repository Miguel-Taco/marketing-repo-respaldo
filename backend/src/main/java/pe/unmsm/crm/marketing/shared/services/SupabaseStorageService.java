package pe.unmsm.crm.marketing.shared.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import pe.unmsm.crm.marketing.shared.config.SupabaseStorageConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Servicio genérico para operaciones con Supabase Storage.
 * Maneja upload, download, delete y listado de archivos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SupabaseStorageService {

    private final SupabaseStorageConfig config;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Sube un archivo al bucket de Supabase Storage.
     * Valida que el archivo sea de tipo markdown (.md).
     *
     * @param bucketName Nombre del bucket
     * @param path       Ruta completa donde se guardará el archivo
     * @param file       Archivo a subir
     * @throws IOException              Si hay error al leer el archivo
     * @throws IllegalArgumentException Si el archivo no es .md
     */
    public void uploadFile(String bucketName, String path, MultipartFile file) throws IOException {
        // Validar extensión .md
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".md")) {
            throw new IllegalArgumentException("Solo se permiten archivos Markdown (.md)");
        }

        uploadFile(bucketName, path, file.getBytes(), file.getContentType());
    }

    /**
     * Sube un archivo (bytes) al bucket de Supabase Storage.
     *
     * @param bucketName  Nombre del bucket
     * @param path        Ruta completa donde se guardará el archivo
     * @param content     Contenido del archivo en bytes
     * @param contentType Tipo de contenido MIME
     */
    public void uploadFile(String bucketName, String path, byte[] content, String contentType) {
        String url = String.format("%s/object/%s/%s", config.getStorageApiUrl(), bucketName, path);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + config.getSupabaseServiceKey());
        headers.setContentType(
                MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream"));

        HttpEntity<byte[]> requestEntity = new HttpEntity<>(content, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Error al subir archivo a Supabase: " + response.getBody());
            }

            log.info("Archivo subido exitosamente a Supabase: {}/{}", bucketName, path);
        } catch (Exception e) {
            log.error("Error al subir archivo a Supabase: {}", e.getMessage());
            throw new RuntimeException("Error al subir archivo a Supabase Storage", e);
        }
    }

    /**
     * Descarga un archivo desde Supabase Storage.
     *
     * @param bucketName Nombre del bucket
     * @param path       Ruta del archivo
     * @return Contenido del archivo como byte array
     */
    public byte[] downloadFile(String bucketName, String path) {
        String url = String.format("%s/object/%s/%s", config.getStorageApiUrl(), bucketName, path);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + config.getSupabaseServiceKey());

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    byte[].class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new RuntimeException("Error al descargar archivo de Supabase");
            }

            return response.getBody();
        } catch (Exception e) {
            log.error("Error al descargar archivo de Supabase: {}", e.getMessage());
            throw new RuntimeException("Error al descargar archivo de Supabase Storage", e);
        }
    }

    /**
     * Descarga un archivo markdown y lo retorna como String.
     *
     * @param bucketName Nombre del bucket
     * @param path       Ruta del archivo
     * @return Contenido del archivo como String
     */
    public String downloadFileAsString(String bucketName, String path) {
        byte[] fileBytes = downloadFile(bucketName, path);
        return new String(fileBytes, StandardCharsets.UTF_8);
    }

    /**
     * Elimina un archivo de Supabase Storage.
     *
     * @param bucketName Nombre del bucket
     * @param path       Ruta del archivo a eliminar
     */
    public void deleteFile(String bucketName, String path) {
        String url = String.format("%s/object/%s/%s", config.getStorageApiUrl(), bucketName, path);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + config.getSupabaseServiceKey());

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    requestEntity,
                    String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Error al eliminar archivo de Supabase: " + response.getBody());
            }

            log.info("Archivo eliminado exitosamente de Supabase: {}/{}", bucketName, path);
        } catch (Exception e) {
            log.error("Error al eliminar archivo de Supabase: {}", e.getMessage());
            throw new RuntimeException("Error al eliminar archivo de Supabase Storage", e);
        }
    }

    /**
     * Lista archivos en un directorio del bucket.
     *
     * @param bucketName Nombre del bucket
     * @param path       Ruta del directorio
     * @return Lista de archivos
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listFiles(String bucketName, String path) {
        String url = String.format("%s/object/list/%s", config.getStorageApiUrl(), bucketName);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + config.getSupabaseServiceKey());
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "prefix", path,
                "limit", 100,
                "offset", 0);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<List> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    List.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new RuntimeException("Error al listar archivos de Supabase");
            }

            return (List<Map<String, Object>>) response.getBody();
        } catch (Exception e) {
            log.error("Error al listar archivos de Supabase: {}", e.getMessage());
            throw new RuntimeException("Error al listar archivos de Supabase Storage", e);
        }
    }

    /**
     * Obtiene la URL pública de un archivo.
     *
     * @param bucketName Nombre del bucket
     * @param path       Ruta del archivo
     * @return URL pública del archivo
     */
    public String getPublicUrl(String bucketName, String path) {
        return String.format("%s/object/public/%s/%s", config.getStorageApiUrl(), bucketName, path);
    }
}
