package pe.unmsm.crm.marketing.segmentacion.infra.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import pe.unmsm.crm.marketing.segmentacion.infra.persistence.JpaCatalogoFiltroEntity;
import pe.unmsm.crm.marketing.segmentacion.infra.persistence.JpaCatalogoFiltroRepository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Caché en memoria del catálogo de filtros para evitar queries lentas
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogoFiltroCache {

    private final JpaCatalogoFiltroRepository repository;

    // Mapa: campo (normalizado) -> Lista de filtros
    private final Map<String, List<JpaCatalogoFiltroEntity>> cache = new ConcurrentHashMap<>();

    /**
     * Carga el catálogo al iniciar la aplicación
     */
    @EventListener(ApplicationReadyEvent.class)
    public void loadCache() {
        log.info("=== Cargando catálogo de filtros en caché ===");
        List<JpaCatalogoFiltroEntity> allFiltros = repository.findAll();

        // Agrupar por campo normalizado
        Map<String, List<JpaCatalogoFiltroEntity>> grouped = allFiltros.stream()
                .collect(Collectors.groupingBy(
                        f -> f.getCampo().toLowerCase().trim()));

        cache.putAll(grouped);
        log.info("✓ Catálogo cargado: {} campos únicos, {} filtros totales",
                cache.size(), allFiltros.size());
    }

    /**
     * Busca filtros por campo (desde caché, instantáneo)
     */
    public List<JpaCatalogoFiltroEntity> findByCampo(String campo) {
        String campoNormalizado = campo.toLowerCase().trim();
        return cache.getOrDefault(campoNormalizado, List.of());
    }

    /**
     * Refresca el caché (llamar si se modifican filtros en BD)
     */
    public void refresh() {
        cache.clear();
        loadCache();
    }
}
