package pe.unmsm.crm.marketing.segmentacion.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * Update only cantidadMiembros without touching filters (optimized)
     */
    @Modifying
    @Query("UPDATE JpaSegmentoEntity s SET s.cantidadMiembros = :cantidad, s.fechaActualizacion = CURRENT_TIMESTAMP WHERE s.idSegmento = :id")
    void updateCantidadMiembros(@Param("id") Long id, @Param("cantidad") Integer cantidad);
}
