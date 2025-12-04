package pe.unmsm.crm.marketing.segmentacion.infra.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import pe.unmsm.crm.marketing.segmentacion.application.LeadServicePort;
import pe.unmsm.crm.marketing.segmentacion.domain.model.*;
import pe.unmsm.crm.marketing.segmentacion.infra.cache.LeadCacheService;

import java.util.List;

/**
 * Adaptador optimizado que usa caché en memoria para filtrar leads
 * En vez de llamar a la API cada vez, filtra desde el caché precargado
 */
@Component
@Primary
@RequiredArgsConstructor
@org.springframework.context.annotation.Profile("!console")
public class RestLeadAdapter implements LeadServicePort {

    private final LeadCacheService cacheService;

    @Override
    public List<Long> findLeadsBySegmento(Segmento segmento) {
        System.out.println("=== RestLeadAdapter: Filtrando desde CACHÉ (sin HTTP call) ===");

        // Filtrar en memoria desde el caché (0 queries SQL)
        List<Long> filteredIds = cacheService.filterLeadsBySegment(segmento);

        System.out.println("✓ Filtrado desde caché: " + filteredIds.size() + " leads encontrados");
        return filteredIds;
    }

    @Override
    public long countLeadsBySegmento(Segmento segmento) {
        System.out.println("=== RestLeadAdapter: Contando desde CACHÉ ===");

        // Contar en memoria desde el caché (0 queries SQL)
        long count = cacheService.countLeadsBySegment(segmento);

        System.out.println("✓ Count desde caché: " + count + " leads");
        return count;
    }
}
