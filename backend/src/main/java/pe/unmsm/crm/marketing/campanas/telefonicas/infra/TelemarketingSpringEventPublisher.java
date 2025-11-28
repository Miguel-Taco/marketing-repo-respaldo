package pe.unmsm.crm.marketing.campanas.telefonicas.infra;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.event.TelemarketingEventPublisher;

@Component
@RequiredArgsConstructor
public class TelemarketingSpringEventPublisher implements TelemarketingEventPublisher {

    private final ApplicationEventPublisher publisher;

    @Override
    public void publish(Object event) {
        publisher.publishEvent(event);
    }
}

