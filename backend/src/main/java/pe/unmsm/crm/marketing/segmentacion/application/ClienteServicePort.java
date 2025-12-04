package pe.unmsm.crm.marketing.segmentacion.application;

import pe.unmsm.crm.marketing.segmentacion.domain.model.Segmento;
import java.util.List;

/**
 * Port para acceder a datos de clientes desde API externa
 */
public interface ClienteServicePort {

    /**
     * Encuentra IDs de clientes que cumplen con las reglas del segmento
     */
    List<Long> findClientesBySegmento(Segmento segmento);

    /**
     * Cuenta clientes que cumplen con las reglas del segmento
     */
    long countClientesBySegmento(Segmento segmento);
}
