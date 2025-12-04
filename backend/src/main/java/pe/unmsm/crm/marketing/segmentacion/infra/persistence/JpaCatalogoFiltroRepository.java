package pe.unmsm.crm.marketing.segmentacion.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface JpaCatalogoFiltroRepository extends JpaRepository<JpaCatalogoFiltroEntity, Long> {
    /**
     * Encuentra filtros disponibles para un tipo de audiencia específico
     * 
     * @param tiposAudiencia Lista de tipos permitidos (ej: ["LEAD", "AMBOS"] o
     *                       ["CLIENTE", "AMBOS"])
     */
    List<JpaCatalogoFiltroEntity> findByTipoAudienciaIn(List<String> tiposAudiencia);

    // Cambiado a List para manejar duplicados en BD
    List<JpaCatalogoFiltroEntity> findByCampo(String campo);
}
