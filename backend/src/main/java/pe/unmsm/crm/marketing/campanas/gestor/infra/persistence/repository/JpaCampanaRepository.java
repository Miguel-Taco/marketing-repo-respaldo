package pe.unmsm.crm.marketing.campanas.gestor.infra.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.Campana;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.CanalEjecucion;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.Prioridad;
import pe.unmsm.crm.marketing.campanas.gestor.domain.state.EstadoCampana;

import java.util.List;

/**
 * Repositorio Spring Data JPA para la entidad Campana.
 */
@Repository
public interface JpaCampanaRepository extends JpaRepository<Campana, Long> {

        /**
         * Busca campanas por filtros opcionales
         */
        @Query("SELECT c FROM Campana c WHERE " +
                        "(:nombre IS NULL OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
                        "(:estado IS NULL OR c.estado = :estado) AND " +
                        "(:prioridad IS NULL OR c.prioridad = :prioridad) AND " +
                        "(:canalEjecucion IS NULL OR c.canalEjecucion = :canalEjecucion) AND " +
                        "(:esArchivado IS NULL OR c.esArchivado = :esArchivado)")
        List<Campana> findByFiltros(@Param("nombre") String nombre,
                        @Param("estado") EstadoCampana estado,
                        @Param("prioridad") Prioridad prioridad,
                        @Param("canalEjecucion") CanalEjecucion canalEjecucion,
                        @Param("esArchivado") Boolean esArchivado);

        /**
         * Busca campanas no archivadas
         */
        List<Campana> findByEsArchivadoFalse();
}
