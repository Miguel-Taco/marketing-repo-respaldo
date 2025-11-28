package pe.unmsm.crm.marketing.leads.domain.strategy;

import pe.unmsm.crm.marketing.leads.domain.model.Lead;
import java.util.Optional;

public interface DeduplicationStrategy {
    Optional<Lead> encontrarDuplicado(Lead leadEntrante);
}
