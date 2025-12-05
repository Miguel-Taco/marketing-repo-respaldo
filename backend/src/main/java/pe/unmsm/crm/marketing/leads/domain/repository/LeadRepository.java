package pe.unmsm.crm.marketing.leads.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.unmsm.crm.marketing.leads.domain.enums.EstadoLead;
import pe.unmsm.crm.marketing.leads.domain.enums.TipoFuente;
import pe.unmsm.crm.marketing.leads.domain.model.Lead;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LeadRepository extends JpaRepository<Lead, Long> {
        Optional<Lead> findByContactoEmail(String email);

        Optional<Lead> findByContactoTelefono(String telefono);

        // Búsqueda flexible - solo por nombre (case-insensitive)
        @Query(value = "SELECT * FROM leads l WHERE " +
                        "(:estado IS NULL OR l.estado_lead_id = :#{#estado?.dbId}) AND " +
                        "(:fuenteTipo IS NULL OR l.fuente_tipo = :#{#fuenteTipo?.name()}) AND " +
                        "(:search IS NULL OR :search = '' OR LOWER(l.nombre_completo) LIKE LOWER(CONCAT('%', :search, '%')))", countQuery = "SELECT count(*) FROM leads l WHERE "
                                        +
                                        "(:estado IS NULL OR l.estado_lead_id = :#{#estado?.dbId}) AND " +
                                        "(:fuenteTipo IS NULL OR l.fuente_tipo = :#{#fuenteTipo?.name()}) AND " +
                                        "(:search IS NULL OR :search = '' OR LOWER(l.nombre_completo) LIKE LOWER(CONCAT('%', :search, '%')))", nativeQuery = true)
        Page<Lead> buscarLeads(
                        @Param("estado") EstadoLead estado,
                        @Param("fuenteTipo") TipoFuente fuenteTipo,
                        @Param("search") String search,
                        Pageable pageable);

        // NUEVO: Para limpieza automática de leads descartados antiguos
        List<Lead> findByEstadoAndUpdatedAtBefore(EstadoLead estado, Instant fechaCorte);

        // NUEVO: Para API de Integración (Segmentación)
        // Usa LEFT JOIN para incluir leads sin ubicación
        @Query("SELECT DISTINCT l FROM Lead l " +
                        "LEFT JOIN l.demograficos.distrito d " +
                        "LEFT JOIN d.provincia p " +
                        "LEFT JOIN p.departamento dep " +
                        "WHERE l.estado IN :estados AND " +
                        "l.fechaCreacion BETWEEN :fechaInicio AND :fechaFin AND " +
                        "(:edadMin IS NULL OR l.demograficos.edad >= :edadMin) AND " +
                        "(:edadMax IS NULL OR l.demograficos.edad <= :edadMax) AND " +
                        "(:genero IS NULL OR l.demograficos.genero = :genero) AND " +
                        "(:distritoId IS NULL OR d.id = :distritoId) AND " +
                        "(:provinciaId IS NULL OR p.id = :provinciaId) AND " +
                        "(:departamentoId IS NULL OR dep.id = :departamentoId) AND " +
                        "(:nivelEducativo IS NULL OR l.demograficos.nivelEducativo = :nivelEducativo) AND " +
                        "(:estadoCivil IS NULL OR l.demograficos.estadoCivil = :estadoCivil)")
        List<Lead> filtrarLeadsParaSegmentacion(
                        @Param("estados") List<EstadoLead> estados,
                        @Param("fechaInicio") LocalDateTime fechaInicio,
                        @Param("fechaFin") LocalDateTime fechaFin,
                        @Param("edadMin") Integer edadMin,
                        @Param("edadMax") Integer edadMax,
                        @Param("genero") String genero,
                        @Param("distritoId") String distritoId,
                        @Param("provinciaId") String provinciaId,
                        @Param("departamentoId") String departamentoId,
                        @Param("nivelEducativo") String nivelEducativo,
                        @Param("estadoCivil") String estadoCivil);

        // NUEVO: Para exportación de leads (sin paginación)
        @Query(value = "SELECT * FROM leads l WHERE " +
                        "(:estado IS NULL OR l.estado_lead_id = :#{#estado?.dbId}) AND " +
                        "(:fuenteTipo IS NULL OR l.fuente_tipo = :#{#fuenteTipo?.name()}) AND " +
                        "(:search IS NULL OR :search = '' OR LOWER(l.nombre_completo) LIKE LOWER(CONCAT('%', :search, '%')))", nativeQuery = true)
        List<Lead> buscarLeadsParaExportacion(
                        @Param("estado") EstadoLead estado,
                        @Param("fuenteTipo") TipoFuente fuenteTipo,
                        @Param("search") String search);

        // REPORTES
        long countByEstado(EstadoLead estado);

        long countByFuenteTipo(TipoFuente fuenteTipo);

        long countByFechaCreacionBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

        List<Lead> findByFechaCreacionBetweenOrderByFechaCreacionAsc(LocalDateTime fechaInicio, LocalDateTime fechaFin);

        @Query("SELECT l FROM Lead l " +
                        "LEFT JOIN FETCH l.demograficos.distrito d " +
                        "LEFT JOIN FETCH d.provincia p " +
                        "LEFT JOIN FETCH p.departamento dep " +
                        "WHERE l.fechaCreacion BETWEEN :fechaInicio AND :fechaFin " +
                        "ORDER BY l.fechaCreacion ASC")
        List<Lead> findByFechaCreacionBetweenWithLocation(@Param("fechaInicio") LocalDateTime fechaInicio,
                        @Param("fechaFin") LocalDateTime fechaFin);

        @Query("SELECT l.estado, COUNT(l) FROM Lead l WHERE l.fechaCreacion BETWEEN :fechaInicio AND :fechaFin GROUP BY l.estado")
        List<Object[]> countByEstadoBetween(@Param("fechaInicio") LocalDateTime fechaInicio,
                        @Param("fechaFin") LocalDateTime fechaFin);

        @Query("SELECT l.fuenteTipo, COUNT(l) FROM Lead l WHERE l.fechaCreacion BETWEEN :fechaInicio AND :fechaFin GROUP BY l.fuenteTipo")
        List<Object[]> countByFuenteTipoBetween(@Param("fechaInicio") LocalDateTime fechaInicio,
                        @Param("fechaFin") LocalDateTime fechaFin);

        // OPTIMIZACIÓN: Cargar leads por IDs CON ubicación (para
        // segmentación/exportación)
        @Query("SELECT l FROM Lead l " +
                        "LEFT JOIN FETCH l.demograficos.distrito d " +
                        "LEFT JOIN FETCH d.provincia p " +
                        "LEFT JOIN FETCH p.departamento " +
                        "WHERE l.id IN :ids")
        List<Lead> findAllByIdWithLocation(@Param("ids") List<Long> ids);
}