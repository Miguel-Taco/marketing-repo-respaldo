package pe.unmsm.crm.marketing.segmentacion.infra.adapter;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import pe.unmsm.crm.marketing.segmentacion.application.LeadServicePort;
import pe.unmsm.crm.marketing.segmentacion.domain.model.Segmento;

import java.util.Collections;
import java.util.List;

@Component
@Profile("console")
public class ConsoleLeadAdapter implements LeadServicePort {

    @Override
    public List<Long> findLeadsBySegmento(Segmento segmento) {
        return Collections.emptyList();
    }

    @Override
    public long countLeadsBySegmento(Segmento segmento) {
        return 0;
    }
}
