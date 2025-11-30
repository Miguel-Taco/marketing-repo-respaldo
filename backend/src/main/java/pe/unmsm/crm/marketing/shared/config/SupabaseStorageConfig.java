package pe.unmsm.crm.marketing.shared.config;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.Getter;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración para Supabase Storage.
 * Lee las credenciales desde variables de entorno.
 */
@Configuration
@Getter
public class SupabaseStorageConfig {

    private final String supabaseUrl;
    private final String supabaseServiceKey;

    public SupabaseStorageConfig() {
        Dotenv dotenv = Dotenv.configure()
                .directory(".")
                .ignoreIfMissing()
                .load();

        this.supabaseUrl = dotenv.get("SUPABASE_URL");
        this.supabaseServiceKey = dotenv.get("SUPABASE_SERVICE_KEY");

        if (supabaseUrl == null || supabaseServiceKey == null) {
            throw new IllegalStateException(
                    "Las variables de entorno SUPABASE_URL y SUPABASE_SERVICE_KEY son requeridas");
        }
    }

    /**
     * Obtiene la URL base para la API de Storage de Supabase
     */
    public String getStorageApiUrl() {
        return supabaseUrl + "/storage/v1";
    }
}
