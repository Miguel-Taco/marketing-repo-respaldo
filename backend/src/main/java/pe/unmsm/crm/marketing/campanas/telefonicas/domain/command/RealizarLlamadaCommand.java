package pe.unmsm.crm.marketing.campanas.telefonicas.domain.command;

import pe.unmsm.crm.marketing.campanas.telefonicas.domain.model.CallContext;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.telephony.TelephonyGateway;

/**
 * Representa la acción de iniciar una llamada (simulada o real). Por ahora es un stub
 * que ejecuta la acción proporcionada por el cliente.
 */
public class RealizarLlamadaCommand extends BaseCallCommand {

    private final TelephonyGateway telephonyGateway;
    private final Runnable accion;

    public RealizarLlamadaCommand(CallContext context, TelephonyGateway telephonyGateway, Runnable accion) {
        super(context);
        this.telephonyGateway = telephonyGateway;
        this.accion = accion;
    }

    @Override
    public void execute() {
        telephonyGateway.iniciarLlamada(context());
        accion.run();
    }
}
