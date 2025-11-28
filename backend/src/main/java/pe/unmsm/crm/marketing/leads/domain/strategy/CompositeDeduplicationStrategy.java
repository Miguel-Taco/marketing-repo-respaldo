package pe.unmsm.crm.marketing.leads.domain.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import pe.unmsm.crm.marketing.leads.domain.model.Lead;
import java.util.Optional;

/**
 * Estrategia compuesta que verifica duplicados tanto por email como por
 * teléfono.
 * Marcada como @Primary para que sea la estrategia por defecto inyectada.
 */
@Component
@Primary
@RequiredArgsConstructor
public class CompositeDeduplicationStrategy implements DeduplicationStrategy {

    private final EmailDeduplicationStrategy emailStrategy;
    private final PhoneDeduplicationStrategy phoneStrategy;

    @Override
    public Optional<Lead> encontrarDuplicado(Lead leadEntrante) {
        // Primero buscar por email (más confiable)
        Optional<Lead> porEmail = emailStrategy.encontrarDuplicado(leadEntrante);
        if (porEmail.isPresent()) {
            return porEmail;
        }

        // Si no hay duplicado por email, buscar por teléfono
        return phoneStrategy.encontrarDuplicado(leadEntrante);
    }
}
