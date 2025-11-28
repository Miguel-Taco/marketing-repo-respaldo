package pe.unmsm.crm.marketing.campanas.telefonicas.infra.telephony;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.model.CallContext;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.telephony.TelephonyGateway;

/**
 * Proxy para controlar acceso, cache y fallback.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TelephonyGatewayProxy implements TelephonyGateway {

    private final SimulatedTelephonyAdapter simulatedTelephonyAdapter;
    private final RestTelephonyAdapter restTelephonyAdapter;

    private TelephonyGateway current() {
        // Por ahora devolvemos simulación; luego se puede basar en feature
        // flag/propiedad
        return simulatedTelephonyAdapter;
    }

    @Override
    public void iniciarLlamada(CallContext context) {
        log.debug("Proxy iniciar llamada {}", context.getLlamadaId());
        current().iniciarLlamada(context);
    }

    @Override
    public void colgar(CallContext context) {
        log.debug("Proxy colgar llamada {}", context.getLlamadaId());
        current().colgar(context);
    }

    @Override
    @Cacheable(cacheNames = "estadoLlamada", key = "#context.llamadaId", unless = "#result == null")
    public String consultarEstado(CallContext context) {
        return current().consultarEstado(context);
    }

    @Override
    public String enmascararNumero(String numeroDestino) {
        return current().enmascararNumero(numeroDestino);
    }
}
