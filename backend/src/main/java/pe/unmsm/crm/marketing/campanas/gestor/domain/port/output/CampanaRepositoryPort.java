package pe.unmsm.crm.marketing.campanas.gestor.domain.port.output;

import pe.unmsm.crm.marketing.campanas.gestor.domain.model.Campana;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para operaciones de persistencia de campanas.
 */
public interface CampanaRepositoryPort {

    /**
     * Guarda una campaña (crear o actualizar)
     */
    Campana save(Campana campana);

    /**
     * Busca una campaña por ID
     */
    Optional<Campana> findById(Long idCampana);

    /**
     * Lista todas las campanas
     */
    List<Campana> findAll();

    /**
     * Busca campanas por filtros
     */
    List<Campana> findByFiltros(String nombre, String estado, String prioridad, String canalEjecucion,
            Boolean esArchivado);

    /**
     * Elimina una campaña por ID
     */
    void deleteById(Long idCampana);

    /**
     * Verifica si existe una campaña por ID
     */
    boolean existsById(Long idCampana);
}
