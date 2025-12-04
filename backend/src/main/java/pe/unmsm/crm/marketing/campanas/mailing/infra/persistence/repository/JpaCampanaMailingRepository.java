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

    @Query("SELECT c FROM CampanaMailing c WHERE c.idAgenteAsignado = :idAgente AND c.idEstado = :idEstado")
    List<CampanaMailing> findByAgenteAndEstado(@Param("idAgente") Integer idAgente, @Param("idEstado") Integer idEstado);

    @Query("SELECT c FROM CampanaMailing c WHERE c.idEstado = :idEstado AND c.fechaInicio <= :ahora")
    List<CampanaMailing> findListosParaEnviar(@Param("idEstado") Integer idEstado, @Param("ahora") LocalDateTime ahora);

    @Query("SELECT c FROM CampanaMailing c WHERE c.idEstado = 1 AND c.fechaInicio < :ahora")
    List<CampanaMailing> findVencidas(@Param("ahora") LocalDateTime ahora);

    @Query("SELECT c FROM CampanaMailing c WHERE c.idAgenteAsignado = :idAgente ORDER BY c.fechaInicio DESC")
    List<CampanaMailing> findByAgenteOrderByFechaInicio(@Param("idAgente") Integer idAgente);

    @Query("SELECT c FROM CampanaMailing c WHERE c.idAgenteAsignado = :idAgente AND c.idEstado = :idEstado " +
           "AND (:nombre IS NULL OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) " +
           "ORDER BY c.fechaInicio DESC")
    List<CampanaMailing> findByAgenteEstadoAndNombre(@Param("idAgente") Integer idAgente,
                                                      @Param("idEstado") Integer idEstado,
                                                      @Param("nombre") String nombre);

    List<CampanaMailing> findByIdInOrderByFechaInicio(List<Integer> ids);

    List<CampanaMailing> findByIdInAndIdEstado(List<Integer> ids, Integer idEstado);

    Optional<CampanaMailing> findByIdCampanaGestion(Long idCampanaGestion);

    @Query("SELECT c.id FROM CampanaMailing c WHERE c.idAgenteAsignado = :idAgente")
    List<Integer> findIdsByIdAgenteAsignado(@Param("idAgente") Integer idAgente);

    boolean existsByIdAndIdAgenteAsignado(Integer id, Integer idAgente);
}
