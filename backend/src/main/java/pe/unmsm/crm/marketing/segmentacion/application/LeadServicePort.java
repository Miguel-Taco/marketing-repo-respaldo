package pe.unmsm.crm.marketing.segmentacion.application;

import pe.unmsm.crm.marketing.segmentacion.domain.model.Segmento;
import java.util.List;

public interface LeadServicePort {
    List<Long> findLeadsBySegmento(Segmento segmento);

    long countLeadsBySegmento(Segmento segmento);
}
