package pe.unmsm.crm.marketing.campanas.mailing.infra.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.unmsm.crm.marketing.campanas.mailing.domain.model.CampanaMailing;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JpaCampanaMailingRepository extends JpaRepository<CampanaMailing, Integer> {

    // ========================================================================
    // QUERIES PARA EL PANEL (Por Agente)
    // ========================================================================

    @Query("SELECT c FROM CampanaMailing c WHERE c.idAgenteAsignado = :idAgente AND c.idEstado = :idEstado")
    List<CampanaMailing> findByAgenteAndEstado(@Param("idAgente") Integer idAgente, @Param("idEstado") Integer idEstado);

    @Query("SELECT c FROM CampanaMailing c WHERE c.idAgenteAsignado = :idAgente ORDER BY c.fechaInicio DESC")
    List<CampanaMailing> findByAgenteOrderByFechaInicio(@Param("idAgente") Integer idAgente);

    @Query("SELECT c FROM CampanaMailing c WHERE c.idAgenteAsignado = :idAgente AND c.idEstado = :idEstado " +
           "AND (:nombre IS NULL OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) " +
           "ORDER BY c.fechaInicio DESC")
    List<CampanaMailing> findByAgenteEstadoAndNombre(@Param("idAgente") Integer idAgente,
                                                      @Param("idEstado") Integer idEstado,
                                                      @Param("nombre") String nombre);

    // ========================================================================
    // QUERIES PARA EL PANEL (Por lista de IDs - Control de acceso)
    // ========================================================================

    List<CampanaMailing> findByIdInOrderByFechaInicio(List<Integer> ids);

    List<CampanaMailing> findByIdInAndIdEstado(List<Integer> ids, Integer idEstado);

    // ========================================================================
    // QUERIES PARA EL SCHEDULER
    // ========================================================================

    /**
     * Busca campañas en estado LISTO (2) cuya fecha_inicio ya llegó
     * Usada por el scheduler para enviar automáticamente
     */
    @Query("SELECT c FROM CampanaMailing c WHERE c.idEstado = :idEstado AND c.fechaInicio <= :ahora")
    List<CampanaMailing> findListosParaEnviar(@Param("idEstado") Integer idEstado, @Param("ahora") LocalDateTime ahora);

    /**
     * Busca campañas en estado PENDIENTE (1) cuya fecha_inicio ya pasó
     * Usada por el scheduler para marcar como VENCIDAS
     */
    @Query("SELECT c FROM CampanaMailing c WHERE c.idEstado = 1 AND c.fechaInicio < :ahora")
    List<CampanaMailing> findVencidas(@Param("ahora") LocalDateTime ahora);

    /**
     * Busca campañas por estado y cuya fecha_fin ya pasó
     * Usada por el scheduler para marcar como FINALIZADAS
     * 
     * @param idEstado Estado actual (típicamente 3 = ENVIADO)
     * @param ahora Fecha/hora actual
     */
    List<CampanaMailing> findByIdEstadoAndFechaFinBefore(Integer idEstado, LocalDateTime ahora);

    // ========================================================================
    // QUERIES PARA INTEGRACIÓN CON GESTOR
    // ========================================================================

    /**
     * Busca campaña por ID de campaña del Gestor
     * Usada para operaciones de pausa/cancelación desde el Gestor
     */
    Optional<CampanaMailing> findByIdCampanaGestion(Long idCampanaGestion);

    // ========================================================================
    // QUERIES PARA CONTROL DE ACCESO
    // ========================================================================

    /**
     * Obtiene solo los IDs de campañas de un agente
     * Usada para verificación de acceso
     */
    @Query("SELECT c.id FROM CampanaMailing c WHERE c.idAgenteAsignado = :idAgente")
    List<Integer> findIdsByIdAgenteAsignado(@Param("idAgente") Integer idAgente);

    /**
     * Verifica si una campaña pertenece a un agente
     */
    boolean existsByIdAndIdAgenteAsignado(Integer id, Integer idAgente);
}
