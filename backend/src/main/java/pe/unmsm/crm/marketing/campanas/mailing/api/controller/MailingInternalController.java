package pe.unmsm.crm.marketing.campanas.mailing.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Controller para operaciones internas del módulo de Mailing.
 * 
 * ENDPOINTS:
 * - POST /api/v1/internal/mailing/cache/clear         → Limpia todo el caché
 * - POST /api/v1/internal/mailing/cache/clear/{name}  → Limpia un caché específico
 * - GET  /api/v1/internal/mailing/cache/stats         → Estadísticas del caché
 * - GET  /api/v1/internal/mailing/health              → Health check del módulo
 * 
 * USO:
 * - Solo accesible por ADMIN
 * - Útil para debugging y mantenimiento
 */
@RestController
@RequestMapping("/api/v1/internal/mailing")
@RequiredArgsConstructor
@Slf4j
public class MailingInternalController {

    private final CacheManager cacheManager;

    /**
     * Limpia todo el caché del módulo de mailing.
     * 
     * POST /api/v1/internal/mailing/cache/clear
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/cache/clear")
    public ResponseEntity<Map<String, Object>> clearAllCache() {
        log.warn("🗑️ Limpiando TODO el caché de mailing...");
        
        int cachesCleared = 0;
        
        for (String cacheName : cacheManager.getCacheNames()) {
            if (cacheName.startsWith("mailing_")) {
                var cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                    cachesCleared++;
                    log.info("  ✓ Caché '{}' limpiado", cacheName);
                }
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Caché de mailing limpiado");
        response.put("cachesCleared", cachesCleared);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Limpia un caché específico.
     * 
     * POST /api/v1/internal/mailing/cache/clear/{cacheName}
     * 
     * Nombres válidos:
     * - mailing_campanias_lista
     * - mailing_campania_detalle
     * - mailing_metricas
     * - mailing_segmento_emails
     * - mailing_segmento_count
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/cache/clear/{cacheName}")
    public ResponseEntity<Map<String, Object>> clearSpecificCache(
            @PathVariable String cacheName) {
        
        log.warn("🗑️ Limpiando caché específico: {}", cacheName);
        
        var cache = cacheManager.getCache(cacheName);
        
        Map<String, Object> response = new HashMap<>();
        
        if (cache == null) {
            response.put("success", false);
            response.put("message", "Caché no encontrado: " + cacheName);
            response.put("availableCaches", cacheManager.getCacheNames());
            return ResponseEntity.badRequest().body(response);
        }
        
        cache.clear();
        
        response.put("success", true);
        response.put("message", "Caché '" + cacheName + "' limpiado");
        
        log.info("✓ Caché '{}' limpiado exitosamente", cacheName);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene estadísticas del caché.
     * 
     * GET /api/v1/internal/mailing/cache/stats
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/cache/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        log.debug("📊 Obteniendo estadísticas de caché");
        
        Map<String, Object> stats = new HashMap<>();
        Map<String, Object> caches = new HashMap<>();
        
        for (String cacheName : cacheManager.getCacheNames()) {
            if (cacheName.startsWith("mailing_")) {
                var cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    Map<String, Object> cacheInfo = new HashMap<>();
                    cacheInfo.put("name", cacheName);
                    cacheInfo.put("type", cache.getClass().getSimpleName());
                    // Nota: Las estadísticas detalladas dependen de la implementación de Caffeine
                    caches.put(cacheName, cacheInfo);
                }
            }
        }
        
        stats.put("totalMailingCaches", caches.size());
        stats.put("caches", caches);
        stats.put("allCacheNames", cacheManager.getCacheNames());
        
        return ResponseEntity.ok(stats);
    }

    /**
     * Health check del módulo de mailing.
     * 
     * GET /api/v1/internal/mailing/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        
        health.put("status", "UP");
        health.put("module", "mailing");
        health.put("timestamp", java.time.Instant.now().toString());
        
        // Verificar caché
        boolean cacheHealthy = cacheManager.getCacheNames() != null;
        health.put("cacheStatus", cacheHealthy ? "AVAILABLE" : "UNAVAILABLE");
        
        return ResponseEntity.ok(health);
    }

    /**
     * Invalida el caché de métricas de una campaña específica.
     * Útil cuando se hacen correcciones manuales.
     * 
     * POST /api/v1/internal/mailing/cache/metricas/{idCampana}/invalidate
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/cache/metricas/{idCampana}/invalidate")
    public ResponseEntity<Map<String, Object>> invalidateMetricasCache(
            @PathVariable Integer idCampana) {
        
        log.warn("🗑️ Invalidando caché de métricas para campaña {}", idCampana);
        
        var cache = cacheManager.getCache("mailing_metricas");
        
        Map<String, Object> response = new HashMap<>();
        
        if (cache != null) {
            cache.evict(idCampana);
            response.put("success", true);
            response.put("message", "Caché de métricas invalidado para campaña " + idCampana);
            log.info("✓ Caché de métricas invalidado para campaña {}", idCampana);
        } else {
            response.put("success", false);
            response.put("message", "Caché 'mailing_metricas' no encontrado");
        }
        
        return ResponseEntity.ok(response);
    }
}