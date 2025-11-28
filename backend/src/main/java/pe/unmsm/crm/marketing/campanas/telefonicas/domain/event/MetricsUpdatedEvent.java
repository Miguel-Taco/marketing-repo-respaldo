package pe.unmsm.crm.marketing.campanas.telefonicas.domain.event;

import lombok.Value;
import pe.unmsm.crm.marketing.campanas.telefonicas.api.dto.MetricasAgenteDTO;

@Value
public class MetricsUpdatedEvent {
    Long campaniaId;
    Long agenteId;
    MetricasAgenteDTO metricas;
}

