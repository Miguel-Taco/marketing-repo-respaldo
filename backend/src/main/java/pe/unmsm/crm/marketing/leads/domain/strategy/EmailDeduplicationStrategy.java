package pe.unmsm.crm.marketing.leads.domain.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.unmsm.crm.marketing.leads.domain.model.Lead;
import pe.unmsm.crm.marketing.leads.domain.repository.LeadRepository;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EmailDeduplicationStrategy implements DeduplicationStrategy {

    private final LeadRepository leadRepository;

    @Override
    public Optional<Lead> encontrarDuplicado(Lead leadEntrante) {
        if (leadEntrante.getContacto() == null || leadEntrante.getContacto().getEmail() == null) {
            return Optional.empty();
        }
        return leadRepository.findByContactoEmail(leadEntrante.getContacto().getEmail());
    }
}
