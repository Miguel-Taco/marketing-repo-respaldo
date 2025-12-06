package pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.entity.GrabacionLlamadaEntity;

import java.time.LocalDateTime;

@Repository
public interface GrabacionLlamadaRepository extends JpaRepository<GrabacionLlamadaEntity, Long> {

        /**
         * Busca grabaciones por agente con paginación
         */
        Page<GrabacionLlamadaEntity> findByIdAgenteOrderByFechaHoraDesc(
                        Integer idAgente,
                        Pageable pageable);

        /**
         * Busca grabaciones por agente y campaña
         */
        Page<GrabacionLlamadaEntity> findByIdAgenteAndIdCampaniaOrderByFechaHoraDesc(
                        Integer idAgente,
                        Integer idCampania,
                        Pageable pageable);

        /**
         * Busca grabaciones por agente y resultado
         */
        Page<GrabacionLlamadaEntity> findByIdAgenteAndResultadoOrderByFechaHoraDesc(
                        Integer idAgente,
                        String resultado,
                        Pageable pageable);

        /**
         * Busca grabaciones por agente en un rango de fechas
         */
        Page<GrabacionLlamadaEntity> findByIdAgenteAndFechaHoraBetweenOrderByFechaHoraDesc(
                        Integer idAgente,
                        LocalDateTime desde,
                        LocalDateTime hasta,
                        Pageable pageable);

        /**
         * Búsqueda compleja con múltiples filtros
         * Permite idAgente null para que admins vean todas las grabaciones
         */
        @Query("SELECT g FROM GrabacionLlamadaEntity g " +
                        "LEFT JOIN FETCH g.agente a " +
                        "LEFT JOIN FETCH g.campania c " +
                        "WHERE (:idAgente IS NULL OR g.idAgente = :idAgente) " +
                        "AND (:idCampania IS NULL OR g.idCampania = :idCampania) " +
                        "AND (:resultado IS NULL OR g.resultado = :resultado) " +
                        "AND (:desde IS NULL OR g.fechaHora >= :desde) " +
                        "AND (:hasta IS NULL OR g.fechaHora <= :hasta) " +
                        "ORDER BY g.fechaHora DESC")
        Page<GrabacionLlamadaEntity> findByMultipleFilters(
                        @Param("idAgente") Integer idAgente,
                        @Param("idCampania") Integer idCampania,
                        @Param("resultado") String resultado,
                        @Param("desde") LocalDateTime desde,
                        @Param("hasta") LocalDateTime hasta,
                        Pageable pageable);

        /**
         * Busca grabaciones que coincidan con nombre o teléfono del lead
         * Permite idAgente null para que admins vean todas las grabaciones
         */
        @Query("SELECT g FROM GrabacionLlamadaEntity g " +
                        "JOIN pe.unmsm.crm.marketing.leads.domain.model.Lead l ON g.idLead = l.id " +
                        "WHERE (:idAgente IS NULL OR g.idAgente = :idAgente) " +
                        "AND (LOWER(l.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
                        "OR LOWER(l.contacto.telefono) LIKE LOWER(CONCAT('%', :busqueda, '%'))) " +
                        "ORDER BY g.fechaHora DESC")
        Page<GrabacionLlamadaEntity> searchByLeadInfo(
                        @Param("idAgente") Integer idAgente,
                        @Param("busqueda") String busqueda,
                        Pageable pageable);

        /**
         * Obtiene grabaciones pendientes de procesar
         */
        @Query("SELECT g FROM GrabacionLlamadaEntity g " +
                        "WHERE g.estadoProcesamiento = 'PENDIENTE' " +
                        "AND g.intentosProcesamiento < 3 " +
                        "ORDER BY g.createdAt ASC")
        Page<GrabacionLlamadaEntity> findPendingProcessing(Pageable pageable);

        /**
         * Cuenta grabaciones por campaña
         */
        Long countByIdCampania(Integer idCampania);

        /**
         * Cuenta grabaciones por agente
         */
        Long countByIdAgente(Integer idAgente);
}
