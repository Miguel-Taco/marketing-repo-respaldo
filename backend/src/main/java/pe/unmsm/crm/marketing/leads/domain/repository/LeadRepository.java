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

        // OPTIMIZACIÓN: Búsqueda Full-Text nativa (MySQL)
        @Query(value = "SELECT * FROM leads l WHERE " +
                        "(:estado IS NULL OR l.estado_lead_id = :#{#estado?.dbId}) AND " +
                        "(:fuenteTipo IS NULL OR l.fuente_tipo = :#{#fuenteTipo?.name()}) AND " +
                        "(:search IS NULL OR MATCH(l.nombre_completo, l.email, l.telefono) AGAINST(:search IN BOOLEAN MODE))", countQuery = "SELECT count(*) FROM leads l WHERE "
                                        +
                                        "(:estado IS NULL OR l.estado_lead_id = :#{#estado?.dbId}) AND " +
                                        "(:fuenteTipo IS NULL OR l.fuente_tipo = :#{#fuenteTipo?.name()}) AND " +
                                        "(:search IS NULL OR MATCH(l.nombre_completo, l.email, l.telefono) AGAINST(:search IN BOOLEAN MODE))", nativeQuery = true)
        Page<Lead> buscarLeads(
                        @Param("estado") EstadoLead estado,
                        @Param("fuenteTipo") TipoFuente fuenteTipo,
                        @Param("search") String search,
                        Pageable pageable);

        // NUEVO: Para limpieza automática de leads descartados antiguos
        List<Lead> findByEstadoAndUpdatedAtBefore(EstadoLead estado, Instant fechaCorte);

        // NUEVO: Para API de Integración (Segmentación)
        @Query("SELECT l FROM Lead l WHERE " +
                        "l.estado IN :estados AND " +
                        "l.fechaCreacion BETWEEN :fechaInicio AND :fechaFin AND " +
                        "(:edadMin IS NULL OR l.demograficos.edad >= :edadMin) AND " +
                        "(:edadMax IS NULL OR l.demograficos.edad <= :edadMax) AND " +
                        "(:genero IS NULL OR l.demograficos.genero = :genero) AND " +
                        "(:distritoId IS NULL OR LOWER(l.demograficos.distrito) = LOWER(:distritoId)) AND " +
                        "(:provinciaId IS NULL OR LOWER(l.demograficos.distrito) LIKE LOWER(CONCAT(:provinciaId, '%'))) AND "
                        +
                        "(:departamentoId IS NULL OR LOWER(l.demograficos.distrito) LIKE LOWER(CONCAT(:departamentoId, '%'))) AND "
                        +
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
                        "(:search IS NULL OR MATCH(l.nombre_completo, l.email, l.telefono) AGAINST(:search IN BOOLEAN MODE))", nativeQuery = true)
        List<Lead> buscarLeadsParaExportacion(
                        @Param("estado") EstadoLead estado,
                        @Param("fuenteTipo") TipoFuente fuenteTipo,
                        @Param("search") String search);
}