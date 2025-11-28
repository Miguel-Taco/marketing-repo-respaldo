package pe.unmsm.crm.marketing.campanas.gestor.domain.state;

import pe.unmsm.crm.marketing.campanas.gestor.domain.exception.EstadoIlegalException;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.Campana;

/**
 * Estado Vigente: La campaña está en ejecución activa.
 * Permite: pausar(), cancelar(), finalizar()
 */
public class EstadoVigente implements EstadoCampana {

    @Override
    public void programar(Campana campana) {
        throw new EstadoIlegalException("La campaña ya está vigente.");
    }

    @Override
    public void activar(Campana campana) {
        throw new EstadoIlegalException("La campaña ya está activa/vigente.");
    }

    @Override
    public void pausar(Campana campana) {
        // Transición permitida: Vigente → Pausada
        campana.setEstado(new EstadoPausada());
    }

    @Override
    public void reanudar(Campana campana) {
        throw new EstadoIlegalException("La campaña ya está vigente.");
    }

    @Override
    public void cancelar(Campana campana) {
        // Transición permitida: Vigente → Cancelada
        campana.setEstado(new EstadoCancelada());
    }

    @Override
    public void finalizar(Campana campana) {
        // Transición permitida: Vigente → Finalizada
        campana.setEstado(new EstadoFinalizada());
    }

    @Override
    public void editar(Campana campana) {
        throw new EstadoIlegalException("No se puede editar una campaña vigente. Pausela primero.");
    }

    @Override
    public void reprogramar(Campana campana) {
        throw new EstadoIlegalException("No se puede reprogramar una campaña vigente. Pausela primero.");
    }

    @Override
    public void archivar(Campana campana) {
        throw new EstadoIlegalException(
                "No se puede archivar una campaña vigente. Debe finalizarla o cancelarla primero.");
    }

    @Override
    public String getNombre() {
        return "Vigente";
    }
}
