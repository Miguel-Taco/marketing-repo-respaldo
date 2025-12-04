package pe.unmsm.crm.marketing.campanas.gestor.domain.port.output;

import pe.unmsm.crm.marketing.campanas.gestor.domain.model.Campana;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
    Page<Campana> findByFiltros(String nombre, String estado, String prioridad, String canalEjecucion,
            Boolean esArchivado, Pageable pageable);

    /**
     * Elimina una campaña por ID
     */
    void deleteById(Long idCampana);

    /**
     * Verifica si existe una campaña por ID
     */
    boolean existsById(Long idCampana);

    /**
     * Busca campañas programadas listas para activar
     */
    List<Campana> findProgramadasParaActivar(java.time.LocalDateTime ahora);

    /**
     * Busca todas las campañas en estado Programada
     */
    List<Campana> findProgramadasPendientes();
}
