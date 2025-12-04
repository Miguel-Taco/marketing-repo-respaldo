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
         * Obtiene el historial completo de una campa??a
         */
        List<LlamadaEntity> findByIdCampaniaOrderByInicioDesc(Integer idCampania);

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

        @Query("SELECT new map(" +
                        "COUNT(l) as totalLlamadas, " +
                        "AVG(TIMESTAMPDIFF(SECOND, l.inicio, l.fin)) as duracionPromedio) " +
                        "FROM LlamadaEntity l " +
                        "WHERE l.idAgente = :idAgente " +
                        "AND l.inicio >= :desde")
        Map<String, Object> getMetricasByAgente(
                        @Param("idAgente") Integer idAgente,
                        @Param("desde") LocalDateTime desde);

        /**
         * Obtiene métricas por campaña desde una fecha específica (GLOBAL)
         */
        @Query("SELECT new map(" +
                        "COUNT(l) as totalLlamadas, " +
                        "AVG(TIMESTAMPDIFF(SECOND, l.inicio, l.fin)) as duracionPromedio) " +
                        "FROM LlamadaEntity l " +
                        "WHERE l.idCampania = :idCampania " +
                        "AND l.inicio >= :desde")
        Map<String, Object> getMetricasByCampaniaAndDate(
                        @Param("idCampania") Integer idCampania,
                        @Param("desde") LocalDateTime desde);

        /**
         * Cuenta llamadas por resultado en una campaña
         * Retorna: [resultado (código), nombre (display), count]
         */
        @Query("SELECT r.resultado, r.nombre, COUNT(l) FROM LlamadaEntity l " +
                        "LEFT JOIN l.resultado r WHERE l.idCampania = :idCampania " +
                        "GROUP BY r.id, r.resultado, r.nombre")
        List<Object[]> countByResultadoAndCampania(@Param("idCampania") Integer idCampania);

        /**
         * Obtiene todas las llamadas de una campaña
         */
        List<LlamadaEntity> findByIdCampania(Integer idCampania);

        /**
         * Cuenta llamadas realizadas hoy por campaña y agente
         * Usa rango de fechas calculado en Java para evitar problemas de timezone
         */
        @Query("SELECT COUNT(l) FROM LlamadaEntity l " +
                        "WHERE l.idCampania = :idCampania " +
                        "AND l.idAgente = :idAgente " +
                        "AND l.inicio BETWEEN :inicioDia AND :finDia")
        Long countLlamadasHoy(
                        @Param("idCampania") Integer idCampania,
                        @Param("idAgente") Integer idAgente,
                        @Param("inicioDia") LocalDateTime inicioDia,
                        @Param("finDia") LocalDateTime finDia);

        /**
         * Cuenta llamadas efectivas (con resultado positivo) realizadas hoy
         * Consideramos efectivas: CONTACTADO, INTERESADO
         * Usa rango de fechas calculado en Java para evitar problemas de timezone
         */
        @Query("SELECT COUNT(l) FROM LlamadaEntity l " +
                        "JOIN l.resultado r " +
                        "WHERE l.idCampania = :idCampania " +
                        "AND l.idAgente = :idAgente " +
                        "AND l.inicio BETWEEN :inicioDia AND :finDia " +
                        "AND r.resultado IN ('CONTACTADO', 'INTERESADO')")
        Long countLlamadasEfectivasHoy(
                        @Param("idCampania") Integer idCampania,
                        @Param("idAgente") Integer idAgente,
                        @Param("inicioDia") LocalDateTime inicioDia,
                        @Param("finDia") LocalDateTime finDia);

        /**
         * Cuenta llamadas realizadas hoy por campaña (GLOBAL)
         */
        @Query("SELECT COUNT(l) FROM LlamadaEntity l " +
                        "WHERE l.idCampania = :idCampania " +
                        "AND l.inicio BETWEEN :inicioDia AND :finDia")
        Long countLlamadasHoyPorCampania(
                        @Param("idCampania") Integer idCampania,
                        @Param("inicioDia") LocalDateTime inicioDia,
                        @Param("finDia") LocalDateTime finDia);

        /**
         * Cuenta llamadas efectivas hoy por campaña (GLOBAL)
         */
        @Query("SELECT COUNT(l) FROM LlamadaEntity l " +
                        "JOIN l.resultado r " +
                        "WHERE l.idCampania = :idCampania " +
                        "AND l.inicio BETWEEN :inicioDia AND :finDia " +
                        "AND r.resultado IN ('CONTACTADO', 'INTERESADO')")
        Long countLlamadasEfectivasHoyPorCampania(
                        @Param("idCampania") Integer idCampania,
                        @Param("inicioDia") LocalDateTime inicioDia,
                        @Param("finDia") LocalDateTime finDia);

        /**
         * Llamadas por día en un rango de fechas
         */
        @Query("SELECT new map(" +
                        "DATE(l.inicio) as fecha, " +
                        "COUNT(l) as total, " +
                        "SUM(CASE WHEN r.resultado IN ('CONTACTADO', 'INTERESADO') THEN 1 ELSE 0 END) as efectivas) " +
                        "FROM LlamadaEntity l " +
                        "LEFT JOIN l.resultado r " +
                        "WHERE l.idCampania = :idCampania " +
                        "AND l.inicio BETWEEN :inicio AND :fin " +
                        "GROUP BY DATE(l.inicio) " +
                        "ORDER BY DATE(l.inicio)")
        List<Map<String, Object>> countLlamadasPorDia(
                        @Param("idCampania") Integer idCampania,
                        @Param("inicio") LocalDateTime inicio,
                        @Param("fin") LocalDateTime fin);

        /**
         * Llamadas por hora del día
         */
        @Query("SELECT HOUR(l.inicio) as hora, COUNT(l) as total " +
                        "FROM LlamadaEntity l " +
                        "WHERE l.idCampania = :idCampania " +
                        "GROUP BY HOUR(l.inicio) " +
                        "ORDER BY HOUR(l.inicio)")
        List<Object[]> countLlamadasPorHora(@Param("idCampania") Integer idCampania);

        /**
         * Duración promedio por tipo de resultado (efectivas vs no efectivas)
         */
        @Query("SELECT new map(" +
                        "AVG(CASE WHEN r.resultado IN ('CONTACTADO', 'INTERESADO') " +
                        "    THEN TIMESTAMPDIFF(SECOND, l.inicio, l.fin) ELSE NULL END) as duracionEfectivas, " +
                        "AVG(CASE WHEN r.resultado NOT IN ('CONTACTADO', 'INTERESADO') " +
                        "    THEN TIMESTAMPDIFF(SECOND, l.inicio, l.fin) ELSE NULL END) as duracionNoEfectivas) " +
                        "FROM LlamadaEntity l " +
                        "JOIN l.resultado r " +
                        "WHERE l.idCampania = :idCampania")
        Map<String, Object> getDuracionPromedioByEfectividad(@Param("idCampania") Integer idCampania);

        /**
         * Rendimiento por agente en una campaña
         */
        @Query("SELECT new map(" +
                        "l.idAgente as idAgente, " +
                        "a.nombre as nombreAgente, " +
                        "COUNT(l) as llamadasRealizadas, " +
                        "SUM(CASE WHEN r.resultado IN ('CONTACTADO', 'INTERESADO') THEN 1 ELSE 0 END) as contactosEfectivos, "
                        +
                        "AVG(TIMESTAMPDIFF(SECOND, l.inicio, l.fin)) as duracionPromedio) " +
                        "FROM LlamadaEntity l " +
                        "LEFT JOIN l.agente a " +
                        "LEFT JOIN l.resultado r " +
                        "WHERE l.idCampania = :idCampania " +
                        "GROUP BY l.idAgente, a.nombre")
        List<Map<String, Object>> getRendimientoPorAgente(@Param("idCampania") Integer idCampania);
}
