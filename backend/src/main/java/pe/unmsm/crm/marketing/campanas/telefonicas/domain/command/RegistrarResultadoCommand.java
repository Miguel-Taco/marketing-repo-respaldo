package pe.unmsm.crm.marketing.campanas.telefonicas.domain.command;

import lombok.Getter;
import pe.unmsm.crm.marketing.campanas.telefonicas.api.dto.LlamadaDTO;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.model.CallContext;

import java.util.function.Supplier;

/**
 * Comando que registra el resultado de una llamada y expone el resultado generado.
 */
@Getter
public class RegistrarResultadoCommand extends BaseCallCommand {

    private final Supplier<LlamadaDTO> action;
    private LlamadaDTO resultado;

    public RegistrarResultadoCommand(CallContext context, Supplier<LlamadaDTO> action) {
        super(context);
        this.action = action;
    }

    @Override
    public void execute() {
        this.resultado = action.get();
    }
}

