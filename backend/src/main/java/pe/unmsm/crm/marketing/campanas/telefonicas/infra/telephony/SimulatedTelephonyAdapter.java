package pe.unmsm.crm.marketing.campanas.telefonicas.infra.telephony;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.model.CallContext;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.telephony.TelephonyGateway;

@Component
@Slf4j
public class SimulatedTelephonyAdapter implements TelephonyGateway {
    @Override
    public void iniciarLlamada(CallContext context) {
        log.info("[SIM] Iniciando llamada simulada para contacto {} campania {}", context.getContactoId(),
                context.getCampaniaId());
    }

    @Override
    public void colgar(CallContext context) {
        log.info("[SIM] Colgando llamada simulada {}", context.getLlamadaId());
    }

    @Override
    public String consultarEstado(CallContext context) {
        return "SIMULADO";
    }

    @Override
    public String enmascararNumero(String numeroDestino) {
        return "XXX-XXX-" + (numeroDestino != null && numeroDestino.length() >= 3
                ? numeroDestino.substring(numeroDestino.length() - 3)
                : "000");
    }
}
