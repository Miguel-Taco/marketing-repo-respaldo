package pe.unmsm.crm.marketing.campanas.telefonicas.domain.telephony;

import pe.unmsm.crm.marketing.campanas.telefonicas.domain.model.CallContext;

/**
 * Puerto que abstrae la pasarela telefónica.
 */
public interface TelephonyGateway {
    void iniciarLlamada(CallContext context);
    void colgar(CallContext context);
    String consultarEstado(CallContext context);
    String enmascararNumero(String numeroDestino);
}

