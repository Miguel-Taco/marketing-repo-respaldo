package pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.model.GuionArchivo;

import java.util.List;

/**
 * Repository para gestionar los metadatos de archivos de guiones.
 */
@Repository
public interface GuionArchivoRepository extends JpaRepository<GuionArchivo, Integer> {

    /**
     * Obtiene los guiones generales de una campaña (idAgente es NULL).
     */
    List<GuionArchivo> findByIdCampaniaAndIdAgenteIsNull(Long idCampania);

    /**
     * Obtiene los guiones de un agente específico en una campaña.
     */
    List<GuionArchivo> findByIdCampaniaAndIdAgente(Long idCampania, Long idAgente);

    /**
     * Obtiene todos los guiones de una campaña (generales y de agentes).
     */
    List<GuionArchivo> findByIdCampania(Long idCampania);
}
