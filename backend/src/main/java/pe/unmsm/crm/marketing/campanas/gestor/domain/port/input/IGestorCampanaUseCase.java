package pe.unmsm.crm.marketing.campanas.gestor.domain.port.input;

import pe.unmsm.crm.marketing.campanas.gestor.domain.model.Campana;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Puerto de entrada (Use Case) para el gestor de campanas.
 * Define las operaciones del dominio que puede realizar el sistema.
 */
public interface IGestorCampanaUseCase {

    /**
     * Crea una nueva campaña en estado Borrador
     */
    Campana crear(Campana campana);

    /**
     * Obtiene una campaña por su ID
     */
    Campana obtenerPorId(Long idCampana);

    /**
     * Lista campanas con filtros opcionales
     */
    List<Campana> listar(String nombre, String estado, String prioridad, String canalEjecucion, Boolean esArchivado);

    /**
     * Edita una campaña (solo permitido en Borrador o Pausada)
     */
    Campana editar(Long idCampana, Campana datosActualizados);

    /**
     * Programa una campaña (Borrador → Programada)
     */
    Campana programar(Long idCampana, LocalDateTime fechaInicio, LocalDateTime fechaFin,
            Integer idAgente, Long idSegmento, Integer idEncuesta);

    /**
     * Activa una campaña (Programada → Vigente)
     */
    Campana activar(Long idCampana);

    /**
     * Pausa una campaña (Vigente → Pausada)
     */
    Campana pausar(Long idCampana, String motivo);

    /**
     * Reanuda una campaña (Pausada → Vigente)
     */
    Campana reanudar(Long idCampana);

    /**
     * Cancela una campaña (Programada/Vigente/Pausada → Cancelada)
     */
    Campana cancelar(Long idCampana, String motivo);

    /**
     * Finaliza una campaña (Vigente → Finalizada)
     */
    Campana finalizar(Long idCampana);

    /**
     * Reprograma una campaña (Programada mantiene o Pausada → Programada)
     */
    Campana reprogramar(Long idCampana, LocalDateTime nuevaFechaInicio, LocalDateTime nuevaFechaFin);

    /**
     * Archiva una campaña (solo Finalizada/Cancelada)
     */
    Campana archivar(Long idCampana);

    /**
     * Duplica una campaña existente (crea una copia en Borrador)
     */
    Campana duplicar(Long idCampana);

    /**
     * Elimina físicamente una campaña (solo Borrador)
     */
    void eliminar(Long idCampana);
}
