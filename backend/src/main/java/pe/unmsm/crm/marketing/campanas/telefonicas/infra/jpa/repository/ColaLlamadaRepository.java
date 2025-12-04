package pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.entity.ColaLlamadaEntity;

import java.util.List;

@Repository
public interface ColaLlamadaRepository extends JpaRepository<ColaLlamadaEntity, Integer> {

        /**
         * Obtiene contactos de la cola por campaña y estados, ordenados por prioridad
         */
        @Query("SELECT c FROM ColaLlamadaEntity c " +
                        "WHERE c.idCampania = :idCampania " +
                        "AND c.estadoEnCola IN :estados " +
                        "ORDER BY CASE c.prioridadCola " +
                        "  WHEN 'ALTA' THEN 1 " +
                        "  WHEN 'MEDIA' THEN 2 " +
                        "  WHEN 'BAJA' THEN 3 END, c.id ASC")
        List<ColaLlamadaEntity> findByCampaniaAndEstadoOrderByPrioridad(
                        @Param("idCampania") Integer idCampania,
                        @Param("estados") List<String> estados);

        /**
         * Obtiene el siguiente contacto disponible con bloqueo pesimista para evitar
         * doble asignación.
         * FIXED: Changed return type from Optional to List to comply with Spring Data
         * JPA requirements
         * when using Pageable parameter. The service layer will get the first element.
         */
        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT c FROM ColaLlamadaEntity c " +
                        "WHERE c.idCampania = :idCampania " +
                        "AND c.estadoEnCola = 'PENDIENTE' " +
                        "AND (c.idAgenteActual IS NULL OR c.idAgenteActual = :idAgente) " +
                        "AND (c.fechaProgramada IS NULL OR c.fechaProgramada <= CURRENT_TIMESTAMP) " +
                        "ORDER BY CASE c.prioridadCola " +
                        "  WHEN 'ALTA' THEN 1 " +
                        "  WHEN 'MEDIA' THEN 2 " +
                        "  WHEN 'BAJA' THEN 3 END, c.id ASC")
        List<ColaLlamadaEntity> findNextAvailableContact(
                        @Param("idCampania") Integer idCampania,
                        @Param("idAgente") Integer idAgente,
                        Pageable pageable);

        /**
         * Obtiene contactos asignados a un agente específico con un estado determinado
         */
        List<ColaLlamadaEntity> findByIdAgenteActualAndEstadoEnCola(
                        Integer idAgenteActual, String estadoEnCola);

        /**
         * Cuenta los contactos pendientes de una campaña
         */
        @Query("SELECT COUNT(c) FROM ColaLlamadaEntity c " +
                        "WHERE c.idCampania = :idCampania AND c.estadoEnCola = 'PENDIENTE'")
        Long countPendingByCampaign(@Param("idCampania") Integer idCampania);

        /**
         * Asigna un contacto a un agente de forma condicional (solo si está pendiente)
         */
        @Modifying
        @Query("UPDATE ColaLlamadaEntity c SET c.estadoEnCola = 'EN_PROCESO', " +
                        "c.idAgenteActual = :idAgente " +
                        "WHERE c.id = :idContacto AND c.estadoEnCola = 'PENDIENTE'")
        int asignarContacto(@Param("idContacto") Integer idContacto,
                        @Param("idAgente") Integer idAgente);

        /**
         * Obtiene todos los contactos de una campaña
         */
        List<ColaLlamadaEntity> findByIdCampania(Integer idCampania);

        /**
         * Cuenta el total de leads en una campaña
         */
        @Query("SELECT COUNT(c) FROM ColaLlamadaEntity c WHERE c.idCampania = :idCampania")
        Long countTotalByCampaign(@Param("idCampania") Integer idCampania);

        /**
         * Cuenta los leads completados (contactados) de una campaña
         */
        @Query("SELECT COUNT(c) FROM ColaLlamadaEntity c " +
                        "WHERE c.idCampania = :idCampania AND c.estadoEnCola = 'COMPLETADO'")
        Long countCompletadosByCampaign(@Param("idCampania") Integer idCampania);

        /**
         * Cuenta los leads por estado en una campaña
         */
        @Query("SELECT COUNT(c) FROM ColaLlamadaEntity c " +
                        "WHERE c.idCampania = :idCampania AND c.estadoEnCola = :estado")
        Long countByEstadoAndCampaign(@Param("idCampania") Integer idCampania, @Param("estado") String estado);

        /**
         * Busca un lead específico en la cola de una campaña
         * Usado para verificar si un lead urgente ya existe en la cola
         */
        @Query("SELECT c FROM ColaLlamadaEntity c " +
                        "WHERE c.idCampania = :idCampania AND c.idLead = :idLead")
        java.util.Optional<ColaLlamadaEntity> findByIdCampaniaAndIdLead(
                        @Param("idCampania") Integer idCampania,
                        @Param("idLead") Long idLead);

        /**
         * Cuenta leads por prioridad en una campaña
         */
        @Query("SELECT c.prioridadCola, COUNT(c) FROM ColaLlamadaEntity c " +
                        "WHERE c.idCampania = :idCampania " +
                        "GROUP BY c.prioridadCola")
        List<Object[]> countByPrioridadAndCampaign(@Param("idCampania") Integer idCampania);
}
