package pe.unmsm.crm.marketing.campanas.gestor.domain.port.output;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.PlantillaCampana;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para operaciones de persistencia de plantillas.
 */
public interface PlantillaRepositoryPort {

    /**
     * Guarda una plantilla (crear o actualizar)
     */
    PlantillaCampana save(PlantillaCampana plantilla);

    /**
     * Busca una plantilla por ID
     */
    Optional<PlantillaCampana> findById(Integer idPlantilla);

    /**
     * Lista todas las plantillas
     */
    List<PlantillaCampana> findAll();

    /**
     * Busca plantillas por filtros con paginación
     */
    Page<PlantillaCampana> findByFiltros(String nombre, String canalEjecucion, Pageable pageable);

    /**
     * Elimina una plantilla por ID
     */
    void deleteById(Integer idPlantilla);

    /**
     * Verifica si existe una plantilla por ID
     */
    boolean existsById(Integer idPlantilla);
}
