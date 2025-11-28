package pe.unmsm.crm.marketing.campanas.telefonicas.domain.event;

public interface TelemarketingEventPublisher {
    void publish(Object event);
}

