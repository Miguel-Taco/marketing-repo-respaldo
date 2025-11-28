package pe.unmsm.crm.marketing.campanas.gestor.domain.state;

import pe.unmsm.crm.marketing.campanas.gestor.domain.model.Campana;

/**
 * Interfaz del Patrón State que define todas las transiciones posibles
 * en el ciclo de vida de una campaña.
 */
public interface EstadoCampana {

    /**
     * Transición: Borrador → Programada
     */
    void programar(Campana campana);

    /**
     * Transición: Programada → Vigente
     */
    void activar(Campana campana);

    /**
     * Transición: Vigente → Pausada
     */
    void pausar(Campana campana);

    /**
     * Transición: Pausada → Vigente
     */
    void reanudar(Campana campana);

    /**
     * Transición: Programada/Vigente/Pausada → Cancelada
     */
    void cancelar(Campana campana);

    /**
     * Transición: Vigente → Finalizada
     */
    void finalizar(Campana campana);

    /**
     * Permite editar la campaña (solo en Borrador)
     */
    void editar(Campana campana);

    /**
     * Transición: Programada (mantiene) o Pausada → Programada
     */
    void reprogramar(Campana campana);

    /**
     * Marca la campaña como archivada (solo Finalizada/Cancelada)
     */
    void archivar(Campana campana);

    /**
     * Retorna el nombre del estado para persistencia en BD
     */
    String getNombre();
}
