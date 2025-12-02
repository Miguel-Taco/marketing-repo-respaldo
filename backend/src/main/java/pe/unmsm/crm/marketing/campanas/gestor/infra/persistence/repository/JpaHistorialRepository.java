package pe.unmsm.crm.marketing.campanas.gestor.infra.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.HistorialCampana;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.TipoAccion;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Repositorio Spring Data JPA para la entidad HistorialCampana.
 */
@Repository
public interface JpaHistorialRepository extends JpaRepository<HistorialCampana, Long> {

        /**
         * Busca el historial de una campaña específica ordenado por fecha descendente
         */
        List<HistorialCampana> findByIdCampanaOrderByFechaAccionDesc(Long idCampana);

        /**
         * Busca historial con filtros opcionales
         */
        @Query("SELECT h FROM HistorialCampana h WHERE " +
                        "(:idCampana IS NULL OR h.idCampana = :idCampana) AND " +
                        "(:tipoAccion IS NULL OR h.tipoAccion = :tipoAccion) AND " +
                        "(:fechaDesde IS NULL OR h.fechaAccion >= :fechaDesde) AND " +
                        "(:fechaHasta IS NULL OR h.fechaAccion <= :fechaHasta) " +
                        "ORDER BY h.fechaAccion DESC")
        Page<HistorialCampana> findByFiltros(@Param("idCampana") Long idCampana,
                        @Param("tipoAccion") TipoAccion tipoAccion,
                        @Param("fechaDesde") LocalDateTime fechaDesde,
                        @Param("fechaHasta") LocalDateTime fechaHasta,
                        Pageable pageable);

        /**
         * Busca historial por tipo de acción
         */
        List<HistorialCampana> findByTipoAccionOrderByFechaAccionDesc(TipoAccion tipoAccion);
}
