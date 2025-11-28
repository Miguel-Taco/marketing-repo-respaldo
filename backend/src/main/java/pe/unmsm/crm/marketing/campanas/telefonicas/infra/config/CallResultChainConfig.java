package pe.unmsm.crm.marketing.campanas.telefonicas.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.handler.*;

/**
 * Configura la cadena de handlers de resultado de llamada.
 */
@Configuration
public class CallResultChainConfig {

    @Bean
    public CallResultHandler callResultHandlerChain() {
        CallResultHandler noContesta = new NoContestaHandler();
        CallResultHandler buzon = new BuzonVozHandler();
        CallResultHandler exitoso = new ContactoExitosoHandler();
        CallResultHandler ventas = new DerivarAVentasHandler();
        CallResultHandler fallback = new FallbackLoggingHandler();

        noContesta.setNext(buzon);
        buzon.setNext(exitoso);
        exitoso.setNext(ventas);
        ventas.setNext(fallback);
        return noContesta;
    }
}
