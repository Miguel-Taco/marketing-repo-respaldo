package pe.unmsm.crm.marketing.segmentacion.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JpaSegmentoFiltroRepository extends JpaRepository<JpaSegmentoFiltroEntity, Long> {
    List<JpaSegmentoFiltroEntity> findBySegmentoIdSegmento(Long idSegmento);

    void deleteBySegmentoIdSegmento(Long idSegmento);
}
