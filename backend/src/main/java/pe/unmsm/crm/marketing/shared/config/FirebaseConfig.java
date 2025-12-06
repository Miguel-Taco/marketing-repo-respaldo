package pe.unmsm.crm.marketing.shared.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Configuración de Firebase Admin SDK
 * Lee las credenciales desde variables de entorno (.env)
 */
@Configuration
@Slf4j
@Getter
public class FirebaseConfig {

    private final String credentialsPath;
    private final String credentialsJson;
    private final String storageBucket;

    public FirebaseConfig() {
        Dotenv dotenv = Dotenv.configure()
                .directory(".")
                .ignoreIfMissing()
                .load();

        this.credentialsPath = dotenv.get("FIREBASE_CREDENTIALS_PATH");
        this.credentialsJson = dotenv.get("FIREBASE_CREDENTIALS_JSON");
        this.storageBucket = dotenv.get("FIREBASE_STORAGE_BUCKET");

        boolean hasPath = credentialsPath != null && !credentialsPath.isEmpty();
        boolean hasJson = credentialsJson != null && !credentialsJson.isEmpty();

        log.info("Firebase config loaded - Bucket: {}, Credentials from File: {}, Credentials from JSON: {}",
                storageBucket, hasPath, hasJson);
    }

    @PostConstruct
    public void initialize() {
        try {
            if (storageBucket == null || storageBucket.isEmpty()) {
                log.warn("Firebase storage bucket not configured. Firebase services will not be available.");
                return;
            }

            GoogleCredentials credentials = null;

            // Prioridad 1: Leer desde variable de entorno JSON (para Render/Producción)
            if (credentialsJson != null && !credentialsJson.isEmpty()) {
                log.info("🔥 Loading Firebase credentials from environment variable (FIREBASE_CREDENTIALS_JSON)");
                try {
                    credentials = GoogleCredentials.fromStream(
                            new java.io.ByteArrayInputStream(credentialsJson.getBytes()));
                    log.info("✅ Firebase credentials loaded successfully from JSON environment variable");
                } catch (Exception e) {
                    log.error("❌ Failed to parse FIREBASE_CREDENTIALS_JSON: {}", e.getMessage());
                    throw e;
                }
            }
            // Prioridad 2: Leer desde archivo (para desarrollo local)
            else if (credentialsPath != null && !credentialsPath.isEmpty()) {
                log.info("🔥 Loading Firebase credentials from file: {}", credentialsPath);
                try {
                    credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsPath));
                    log.info("✅ Firebase credentials loaded successfully from file");
                } catch (Exception e) {
                    log.error("❌ Failed to read credentials file at: {}", credentialsPath);
                    log.error("   Error: {}", e.getMessage());
                    throw e;
                }
            }
            // Sin credenciales
            else {
                log.warn(
                        "⚠️ No Firebase credentials configured (neither FIREBASE_CREDENTIALS_JSON nor FIREBASE_CREDENTIALS_PATH)");
                log.warn("   Firebase services will not be available.");
                return;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .setStorageBucket(storageBucket)
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("🎉 Firebase Admin SDK initialized successfully with bucket: {}", storageBucket);
            } else {
                log.info("Firebase Admin SDK already initialized");
            }
        } catch (IOException e) {
            log.error("💥 Error initializing Firebase Admin SDK: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize Firebase", e);
        }
    }
}
