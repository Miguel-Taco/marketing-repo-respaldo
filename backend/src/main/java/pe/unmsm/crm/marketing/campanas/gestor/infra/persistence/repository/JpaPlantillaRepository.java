
package pe.unmsm.crm.marketing.campanas.gestor.infra.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.PlantillaCampana;

import java.util.List;

/**
 * Repositorio Spring Data JPA para la entidad PlantillaCampana.
 */
@Repository
public interface JpaPlantillaRepository extends JpaRepository<PlantillaCampana, Integer> {

        /**
         * Busca plantillas por filtros opcionales
         */
        @Query("SELECT p FROM PlantillaCampana p WHERE " +
                        "(:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
                        "(:canalEjecucion IS NULL OR " +
                        " (:canalEjecucion = 'SIN_ASIGNAR' AND p.canalEjecucion IS NULL) OR " +
                        " (:canalEjecucion != 'SIN_ASIGNAR' AND str(p.canalEjecucion) = :canalEjecucion))")
        List<PlantillaCampana> findByFiltros(@Param("nombre") String nombre,
                        @Param("canalEjecucion") String canalEjecucion);

        /**
         * Busca plantillas por canal de ejecución
         */
        List<PlantillaCampana> findByCanalEjecucion(String canalEjecucion);
}
