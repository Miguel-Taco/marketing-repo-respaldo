package pe.unmsm.crm.marketing.segmentacion.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaSegmentoRepository extends JpaRepository<JpaSegmentoEntity, Long> {

    /**
     * Optimized query to fetch all segments with their filters and catalog in one
     * go
     * This prevents N+1 query problem
     */
    @Query("SELECT DISTINCT s FROM JpaSegmentoEntity s " +
            "LEFT JOIN FETCH s.filtros f " +
            "LEFT JOIN FETCH f.catalogo")
    List<JpaSegmentoEntity> findAllWithFiltersAndCatalog();
}
