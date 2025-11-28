package pe.unmsm.crm.marketing.campanas.telefonicas.infra.telephony;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.model.CallContext;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.telephony.TelephonyGateway;

/**
 * Esqueleto para integración futura con API real de telefonía.
 */
@Component
@Slf4j
public class RestTelephonyAdapter implements TelephonyGateway {
    @Override
    public void iniciarLlamada(CallContext context) {
        log.info("[REST] Iniciar llamada real no implementado");
    }

    @Override
    public void colgar(CallContext context) {
        log.info("[REST] Colgar llamada real no implementado");
    }

    @Override
    public String consultarEstado(CallContext context) {
        return "NO_IMPLEMENTADO";
    }

    @Override
    public String enmascararNumero(String numeroDestino) {
        return numeroDestino;
    }
}
