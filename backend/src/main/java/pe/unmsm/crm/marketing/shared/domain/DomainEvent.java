package pe.unmsm.crm.marketing.shared.domain;

import lombok.Getter;

import java.time.Instant;

@Getter
public abstract class DomainEvent {

    private final Instant occurredAt = Instant.now();

    public abstract String getType();
}
