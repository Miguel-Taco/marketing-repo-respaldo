package pe.unmsm.crm.marketing.segmentacion.domain.repository;

import pe.unmsm.crm.marketing.segmentacion.domain.model.Segmento;
import java.util.Optional;
import java.util.List;

public interface SegmentoRepository {
    Segmento save(Segmento segmento);

    Optional<Segmento> findById(Long id);

    List<Segmento> findAll();

    void deleteById(Long id);
}
