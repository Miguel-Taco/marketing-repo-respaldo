package pe.unmsm.crm.marketing.campanas.gestor.domain.port.output;

import pe.unmsm.crm.marketing.campanas.gestor.domain.model.HistorialCampana;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Puerto de salida para operaciones de persistencia del historial de campanas.
 */
public interface HistorialRepositoryPort {

    /**
     * Guarda un registro de historial
     */
    HistorialCampana save(HistorialCampana historial);

    /**
     * Busca el historial de una campaña específica
     */
    List<HistorialCampana> findByIdCampana(Long idCampana);

    /**
     * Busca historial con filtros
     */
    Page<HistorialCampana> findByFiltros(Long idCampana, String tipoAccion,
            LocalDateTime fechaDesde, LocalDateTime fechaHasta, Pageable pageable);

    /**
     * Lista todo el historial
     */
    List<HistorialCampana> findAll();
}
