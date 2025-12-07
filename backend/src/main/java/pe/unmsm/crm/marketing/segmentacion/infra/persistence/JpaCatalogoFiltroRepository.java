package pe.unmsm.crm.marketing.segmentacion.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JpaCatalogoFiltroRepository extends JpaRepository<JpaCatalogoFiltroEntity, Long> {
    List<JpaCatalogoFiltroEntity> findByTipoAudienciaIn(List<String> tiposAudiencia);

    List<JpaCatalogoFiltroEntity> findByCampo(String campo);
}
