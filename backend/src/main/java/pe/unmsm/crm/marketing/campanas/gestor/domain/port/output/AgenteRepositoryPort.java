package pe.unmsm.crm.marketing.campanas.gestor.domain.port.output;

import pe.unmsm.crm.marketing.campanas.gestor.domain.model.Agente;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para acceder a los datos de Agentes.
 */
public interface AgenteRepositoryPort {

    /**
     * Lista todos los agentes activos.
     */
    List<Agente> findAllActive();

    /**
     * Busca un agente por su ID.
     */
    Optional<Agente> findById(Integer id);

    /**
     * Verifica si un agente existe y está activo.
     */
    boolean existsAndActive(Integer id);
}
