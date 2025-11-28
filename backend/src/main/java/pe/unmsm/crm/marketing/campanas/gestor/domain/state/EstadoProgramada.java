package pe.unmsm.crm.marketing.campanas.gestor.domain.state;

import pe.unmsm.crm.marketing.campanas.gestor.domain.exception.EstadoIlegalException;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.Campana;

/**
 * Estado Programada: La campaña tiene fechas asignadas y está esperando su
 * activación.
 * Permite: activar(), cancelar(), reprogramar()
 * Bloquea: editar()
 */
public class EstadoProgramada implements EstadoCampana {

    @Override
    public void programar(Campana campana) {
        throw new EstadoIlegalException("La campaña ya está programada.");
    }

    @Override
    public void activar(Campana campana) {
        // Transición permitida: Programada → Vigente
        campana.setEstado(new EstadoVigente());
    }

    @Override
    public void pausar(Campana campana) {
        throw new EstadoIlegalException(
                "No se puede pausar una campaña que aún no ha iniciado. Use cancelar o reprogramar.");
    }

    @Override
    public void reanudar(Campana campana) {
        throw new EstadoIlegalException("No se puede reanudar una campaña programada.");
    }

    @Override
    public void cancelar(Campana campana) {
        // Transición permitida: Programada → Cancelada
        campana.setEstado(new EstadoCancelada());
    }

    @Override
    public void finalizar(Campana campana) {
        throw new EstadoIlegalException("No se puede finalizar una campaña que aún no ha iniciado.");
    }

    @Override
    public void editar(Campana campana) {
        throw new EstadoIlegalException(
                "No se puede editar una campaña programada. Use reprogramar para cambiar fechas.");
    }

    @Override
    public void reprogramar(Campana campana) {
        // Permitido: Mantiene el estado en Programada
        // Solo actualiza las fechas (lógica en el servicio)
    }

    @Override
    public void archivar(Campana campana) {
        throw new EstadoIlegalException("No se puede archivar una campaña programada. Cancele primero.");
    }

    @Override
    public String getNombre() {
        return "Programada";
    }
}
