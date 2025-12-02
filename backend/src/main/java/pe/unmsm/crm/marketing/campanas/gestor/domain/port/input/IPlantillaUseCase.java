package pe.unmsm.crm.marketing.campanas.gestor.domain.port.input;

import org.springframework.data.domain.Page;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.PlantillaCampana;

import java.util.List;

/**
 * Puerto de entrada (Use Case) para la gestión de plantillas de campanas.
 */
public interface IPlantillaUseCase {

    /**
     * Crea una nueva plantilla
     */
    PlantillaCampana crear(PlantillaCampana plantilla);

    /**
     * Obtiene una plantilla por su ID
     */
    PlantillaCampana obtenerPorId(Integer idPlantilla);

    /**
     * Lista plantillas con filtros opcionales y paginación
     */
    Page<PlantillaCampana> listar(String nombre, String canalEjecucion, int page, int size);

    /**
     * Edita una plantilla existente
     */
    PlantillaCampana editar(Integer idPlantilla, PlantillaCampana datosActualizados);

    /**
     * Elimina una plantilla
     */
    void eliminar(Integer idPlantilla);
}
