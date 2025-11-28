package pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.entity.LlamadaEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface LlamadaRepository extends JpaRepository<LlamadaEntity, Integer> {

    /**
     * Obtiene el historial de llamadas por campaña y agente
     */
    List<LlamadaEntity> findByIdCampaniaAndIdAgenteOrderByInicioDesc(
            Integer idCampania, Integer idAgente);

    /**
     * Obtiene el historial de llamadas por lead
     */
    List<LlamadaEntity> findByIdLeadOrderByInicioDesc(Long idLead);

    /**
     * Obtiene métricas agregadas por campaña
     */
    @Query("SELECT new map(" +
            "COUNT(l) as totalLlamadas, " +
            "AVG(TIMESTAMPDIFF(SECOND, l.inicio, l.fin)) as duracionPromedio, " +
            "SUM(CASE WHEN l.idResultado IS NOT NULL THEN 1 ELSE 0 END) as conResultado) " +
            "FROM LlamadaEntity l WHERE l.idCampania = :idCampania")
    Map<String, Object> getMetricasByCampania(@Param("idCampania") Integer idCampania);

    /**
     * Obtiene métricas por agente y campaña desde una fecha específica
     */
    @Query("SELECT new map(" +
            "COUNT(l) as totalLlamadas, " +
            "AVG(TIMESTAMPDIFF(SECOND, l.inicio, l.fin)) as duracionPromedio) " +
            "FROM LlamadaEntity l " +
            "WHERE l.idAgente = :idAgente AND l.idCampania = :idCampania " +
            "AND l.inicio >= :desde")
    Map<String, Object> getMetricasByAgenteAndCampania(
            @Param("idAgente") Integer idAgente,
            @Param("idCampania") Integer idCampania,
            @Param("desde") LocalDateTime desde);

    /**
     * Cuenta llamadas por resultado en una campaña
     */
    @Query("SELECT r.nombre, COUNT(l) FROM LlamadaEntity l " +
            "JOIN l.resultado r WHERE l.idCampania = :idCampania " +
            "GROUP BY r.id, r.nombre")
    List<Object[]> countByResultadoAndCampania(@Param("idCampania") Integer idCampania);

    /**
     * Obtiene todas las llamadas de una campaña
     */
    List<LlamadaEntity> findByIdCampania(Integer idCampania);
}
