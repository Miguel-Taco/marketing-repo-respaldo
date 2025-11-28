package pe.unmsm.crm.marketing.segmentacion.application;

import org.springframework.stereotype.Service;
import pe.unmsm.crm.marketing.segmentacion.domain.model.Segmento;
import pe.unmsm.crm.marketing.segmentacion.domain.repository.SegmentoRepository;
import java.util.List;
import java.util.Map;

@Service
public class SegmentoPreviewService {

    private final SegmentoRepository segmentoRepository;
    private final LeadServicePort leadServicePort;

    public SegmentoPreviewService(SegmentoRepository segmentoRepository,
            LeadServicePort leadServicePort) {
        this.segmentoRepository = segmentoRepository;
        this.leadServicePort = leadServicePort;
    }

    public long previsualizarConteo(Long id) {
        Segmento segmento = segmentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Segmento no encontrado"));

        return leadServicePort.countLeadsBySegmento(segmento);
    }

    /**
     * Previsualiza un segmento sin guardarlo en la base de datos
     */
    public Map<String, Object> previsualizarSegmentoTemporal(Segmento segmento) {
        List<Long> leadIds = leadServicePort.findLeadsBySegmento(segmento);
        long count = leadIds.size();

        return Map.of(
                "count", count,
                "leadIds", leadIds);
    }
}
