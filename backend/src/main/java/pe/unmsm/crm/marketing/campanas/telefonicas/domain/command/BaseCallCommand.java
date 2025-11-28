package pe.unmsm.crm.marketing.campanas.telefonicas.domain.command;

import lombok.Getter;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.model.CallContext;

import java.util.UUID;

/**
 * Implementación base que genera un ID y guarda el contexto.
 */
@Getter
public abstract class BaseCallCommand implements CallCommand {

    private final String id;
    private final CallContext context;

    protected BaseCallCommand(CallContext context) {
        this.id = UUID.randomUUID().toString();
        this.context = context;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public CallContext context() {
        return context;
    }
}
