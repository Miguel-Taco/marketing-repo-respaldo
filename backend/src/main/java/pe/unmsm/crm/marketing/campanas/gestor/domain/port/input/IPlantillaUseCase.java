package pe.unmsm.crm.marketing.campanas.gestor.domain.port.input;

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
     * Lista plantillas con filtros opcionales
     */
    List<PlantillaCampana> listar(String nombre, String canalEjecucion);

    /**
     * Edita una plantilla existente
     */
    PlantillaCampana editar(Integer idPlantilla, PlantillaCampana datosActualizados);

    /**
     * Elimina una plantilla
     */
    void eliminar(Integer idPlantilla);
}
