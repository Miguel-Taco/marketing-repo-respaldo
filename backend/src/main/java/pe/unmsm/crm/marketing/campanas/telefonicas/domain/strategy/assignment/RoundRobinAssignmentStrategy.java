package pe.unmsm.crm.marketing.campanas.telefonicas.domain.strategy.assignment;

import org.springframework.stereotype.Component;
import pe.unmsm.crm.marketing.campanas.telefonicas.api.dto.CampaniaTelefonicaDTO;
import pe.unmsm.crm.marketing.campanas.telefonicas.api.dto.ContactoDTO;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Estrategia round-robin simple por campaña.
 */
@Component
public class RoundRobinAssignmentStrategy implements CallAssignmentStrategy {

    private final Map<Long, AtomicInteger> offsets = new ConcurrentHashMap<>();

    @Override
    public Long assign(CampaniaTelefonicaDTO campania, List<ContactoDTO> cola, Long agenteSolicitado) {
        if (campania == null || campania.getIdsAgentes() == null || campania.getIdsAgentes().isEmpty()) {
            return agenteSolicitado;
        }
        if (agenteSolicitado != null && campania.getIdsAgentes().contains(agenteSolicitado)) {
            return agenteSolicitado;
        }
        AtomicInteger counter = offsets.computeIfAbsent(campania.getId(), k -> new AtomicInteger(0));
        int index = Math.abs(counter.getAndIncrement()) % campania.getIdsAgentes().size();
        return campania.getIdsAgentes().get(index);
    }
}
