package pe.unmsm.crm.marketing.segmentacion.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaSegmentoMiembroRepository extends JpaRepository<JpaSegmentoMiembroEntity, JpaSegmentoMiembroId> {
    void deleteByIdSegmento(Long idSegmento);

    long countByIdSegmento(Long idSegmento);

    List<JpaSegmentoMiembroEntity> findByIdSegmento(Long idSegmento);
}
