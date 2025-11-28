package pe.unmsm.crm.marketing.segmentacion.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface JpaCatalogoFiltroRepository extends JpaRepository<JpaCatalogoFiltroEntity, Long> {
    Optional<JpaCatalogoFiltroEntity> findByCampo(String campo);
}
