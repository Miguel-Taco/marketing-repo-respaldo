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
    private final ClienteServicePort clienteServicePort;

    public SegmentoPreviewService(SegmentoRepository segmentoRepository,
            LeadServicePort leadServicePort,
            ClienteServicePort clienteServicePort) {
        this.segmentoRepository = segmentoRepository;
        this.leadServicePort = leadServicePort;
        this.clienteServicePort = clienteServicePort;
    }

    public long previsualizarConteo(Long id) {
        Segmento segmento = segmentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Segmento no encontrado"));

        // Decidir qué servicio usar según el tipo de audiencia
        if ("CLIENTE".equals(segmento.getTipoAudiencia())) {
            return clienteServicePort.countClientesBySegmento(segmento);
        } else {
            return leadServicePort.countLeadsBySegmento(segmento);
        }
    }

    /**
     * Previsualiza un segmento sin guardarlo en la base de datos
     */
    public Map<String, Object> previsualizarSegmentoTemporal(Segmento segmento) {
        System.out.println("=== PREVIEW TEMPORAL ===");
        System.out.println("Tipo de audiencia: " + segmento.getTipoAudiencia());

        List<Long> memberIds;

        // Decidir qué servicio usar según el tipo de audiencia
        if ("CLIENTE".equals(segmento.getTipoAudiencia())) {
            System.out.println("Usando ClienteServicePort para filtrar clientes...");
            memberIds = clienteServicePort.findClientesBySegmento(segmento);
        } else {
            System.out.println("Usando LeadServicePort para filtrar leads...");
            memberIds = leadServicePort.findLeadsBySegmento(segmento);
        }

        long count = memberIds.size();
        System.out.println("Total encontrados: " + count);
        System.out.println("========================");

        return Map.of(
                "count", count,
                "leadIds", memberIds); // Nota: el nombre "leadIds" se mantiene por compatibilidad con frontend
    }
}
