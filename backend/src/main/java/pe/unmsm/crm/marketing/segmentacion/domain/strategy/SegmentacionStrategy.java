package pe.unmsm.crm.marketing.segmentacion.domain.strategy;

import pe.unmsm.crm.marketing.segmentacion.domain.model.Segmento;
import java.util.List;

public interface SegmentacionStrategy {
    List<Long> ejecutarSegmentacion(Segmento segmento);
}
