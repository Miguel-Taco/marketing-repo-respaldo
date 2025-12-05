package pe.unmsm.crm.marketing.campanas.mailing.infra.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

/**
 * Configuración de caché para el módulo de Mailing.
 * 
 * CACHÉS DISPONIBLES:
 * 
 * 1. mailing_campanias_lista
 *    - Almacena: Listados de campañas por estado
 *    - TTL: 2 minutos (se invalida al crear/actualizar campaña)
 *    - Uso: Pestañas del panel (Pendientes, Listos, Enviados, Finalizados)
 * 
 * 2. mailing_campania_detalle
 *    - Almacena: Detalle completo de una campaña
 *    - TTL: 5 minutos (se invalida al guardar borrador o cambiar estado)
 *    - Uso: Vista de edición de campaña
 * 
 * 3. mailing_metricas
 *    - Almacena: Métricas de una campaña
 *    - TTL: 30 segundos (se actualiza frecuentemente con webhooks)
 *    - Uso: Panel de métricas en campañas enviadas
 * 
 * 4. mailing_segmento_emails
 *    - Almacena: Lista de emails de un segmento
 *    - TTL: 10 minutos (los segmentos no cambian muy frecuentemente)
 *    - Uso: Al preparar envío de campaña
 * 
 * NOTA: El caché se invalida automáticamente en las operaciones de escritura
 * usando @CacheEvict en los métodos correspondientes.
 */
@Configuration
@EnableCaching
public class MailingCacheConfig {

    /**
     * Cache manager principal para el módulo de Mailing
     */
    @Bean("mailingCacheManager")
    @Primary
    public CacheManager mailingCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // Configuración por defecto
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats()); // Habilita estadísticas para monitoreo
        
        return cacheManager;
    }

    /**
     * Cache específico para listados de campañas (TTL corto)
     */
    @Bean("mailingListaCacheManager")
    public CacheManager mailingListaCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "mailing_campanias_lista"
        );
        
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(2, TimeUnit.MINUTES)
                .recordStats());
        
        return cacheManager;
    }

    /**
     * Cache específico para métricas (TTL muy corto por actualizaciones frecuentes)
     */
    @Bean("mailingMetricasCacheManager")
    public CacheManager mailingMetricasCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "mailing_metricas"
        );
        
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(200)
                .expireAfterWrite(30, TimeUnit.SECONDS)
                .recordStats());
        
        return cacheManager;
    }
}