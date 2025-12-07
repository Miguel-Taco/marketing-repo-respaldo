package pe.unmsm.crm.marketing.segmentacion.application;

import pe.unmsm.crm.marketing.segmentacion.application.dto.MarketingClienteDTO;
import pe.unmsm.crm.marketing.segmentacion.domain.model.Segmento;
import java.util.List;

public interface ClienteServicePort {

    List<Long> findClientesBySegmento(Segmento segmento);

    long countClientesBySegmento(Segmento segmento);

    List<MarketingClienteDTO> getAllClientes();
}
