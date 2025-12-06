package pe.unmsm.crm.marketing.shared.services;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.firebase.cloud.StorageClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pe.unmsm.crm.marketing.shared.config.FirebaseConfig;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * Servicio para gestionar archivos en Firebase Storage
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FirebaseStorageService {

    private final FirebaseConfig firebaseConfig;

    /**
     * Sube un archivo a Firebase Storage
     *
     * @param path Ruta donde se guardará el archivo
     * @param file Archivo a subir
     * @return URL del archivo subido
     */
    public String subirAudio(String path, MultipartFile file) throws IOException {
        return subirAudio(path, file.getBytes(), file.getContentType());
    }

    /**
     * Sube un archivo (bytes) a Firebase Storage
     *
     * @param path        Ruta donde se guardará el archivo
     * @param contenido   Contenido del archivo en bytes
     * @param contentType Tipo de contenido MIME
     * @return URL del archivo subido
     */
    public String subirAudio(String path, byte[] contenido, String contentType) {
        try {
            Storage storage = StorageClient.getInstance().bucket().getStorage();
            BlobId blobId = BlobId.of(firebaseConfig.getStorageBucket(), path);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(contentType != null ? contentType : "audio/mpeg")
                    .build();

            storage.create(blobInfo, contenido);

            log.info("Audio subido exitosamente a Firebase Storage: {}", path);
            return path;
        } catch (Exception e) {
            log.error("Error al subir audio a Firebase Storage: {}", e.getMessage());
            throw new RuntimeException("Error al subir archivo a Firebase Storage", e);
        }
    }

    /**
     * Obtiene una URL firmada temporal para acceder al archivo
     *
     * @param path          Ruta del archivo
     * @param duracionHoras Duración de validez de la URL en horas
     * @return URL firmada temporal
     */
    public String obtenerUrlFirmada(String path, long duracionHoras) {
        try {
            Storage storage = StorageClient.getInstance().bucket().getStorage();
            BlobId blobId = BlobId.of(firebaseConfig.getStorageBucket(), path);
            Blob blob = storage.get(blobId);

            if (blob == null) {
                throw new RuntimeException("Archivo no encontrado en Firebase Storage: " + path);
            }

            URL signedUrl = blob.signUrl(duracionHoras, TimeUnit.HOURS);
            return signedUrl.toString();
        } catch (Exception e) {
            log.error("Error al generar URL firmada: {}", e.getMessage());
            throw new RuntimeException("Error al generar URL de acceso al archivo", e);
        }
    }

    /**
     * Descarga un archivo desde Firebase Storage
     *
     * @param path Ruta del archivo
     * @return Contenido del archivo como byte array
     */
    public byte[] descargarAudio(String path) {
        try {
            Storage storage = StorageClient.getInstance().bucket().getStorage();
            BlobId blobId = BlobId.of(firebaseConfig.getStorageBucket(), path);
            Blob blob = storage.get(blobId);

            if (blob == null) {
                throw new RuntimeException("Archivo no encontrado en Firebase Storage: " + path);
            }

            return blob.getContent();
        } catch (Exception e) {
            log.error("Error al descargar audio de Firebase Storage: {}", e.getMessage());
            throw new RuntimeException("Error al descargar archivo de Firebase Storage", e);
        }
    }

    /**
     * Elimina un archivo de Firebase Storage
     *
     * @param path Ruta del archivo a eliminar
     */
    public void eliminarAudio(String path) {
        try {
            Storage storage = StorageClient.getInstance().bucket().getStorage();
            BlobId blobId = BlobId.of(firebaseConfig.getStorageBucket(), path);
            boolean deleted = storage.delete(blobId);

            if (deleted) {
                log.info("Audio eliminado exitosamente de Firebase Storage: {}", path);
            } else {
                log.warn("No se pudo eliminar el archivo o no existe: {}", path);
            }
        } catch (Exception e) {
            log.error("Error al eliminar audio de Firebase Storage: {}", e.getMessage());
            throw new RuntimeException("Error al eliminar archivo de Firebase Storage", e);
        }
    }

    /**
     * Verifica si un archivo existe en Firebase Storage
     *
     * @param path Ruta del archivo
     * @return true si existe, false en caso contrario
     */
    public boolean archivoExiste(String path) {
        try {
            Storage storage = StorageClient.getInstance().bucket().getStorage();
            BlobId blobId = BlobId.of(firebaseConfig.getStorageBucket(), path);
            Blob blob = storage.get(blobId);
            return blob != null && blob.exists();
        } catch (Exception e) {
            log.error("Error al verificar existencia de archivo: {}", e.getMessage());
            return false;
        }
    }
}
