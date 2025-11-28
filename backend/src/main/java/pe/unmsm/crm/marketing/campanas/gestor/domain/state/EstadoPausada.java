package pe.unmsm.crm.marketing.campanas.gestor.domain.state;

import pe.unmsm.crm.marketing.campanas.gestor.domain.exception.EstadoIlegalException;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.Campana;

/**
 * Estado Pausada: La campaña fue detenida temporalmente.
 * Permite: reanudar(), cancelar(), reprogramar()
 */
public class EstadoPausada implements EstadoCampana {

    @Override
    public void programar(Campana campana) {
        throw new EstadoIlegalException("La campaña está pausada. Use reanudar o reprogramar.");
    }

    @Override
    public void activar(Campana campana) {
        throw new EstadoIlegalException("La campaña está pausada. Use reanudar.");
    }

    @Override
    public void pausar(Campana campana) {
        throw new EstadoIlegalException("La campaña ya está pausada.");
    }

    @Override
    public void reanudar(Campana campana) {
        // Transición permitida: Pausada → Vigente
        campana.setEstado(new EstadoVigente());
    }

    @Override
    public void cancelar(Campana campana) {
        // Transición permitida: Pausada → Cancelada
        campana.setEstado(new EstadoCancelada());
    }

    @Override
    public void finalizar(Campana campana) {
        throw new EstadoIlegalException("No se puede finalizar una campaña pausada. Reanude primero.");
    }

    @Override
    public void editar(Campana campana) {
        // Permitido: editar algunos campos mientras está pausada
        // La lógica específica se maneja en el servicio
    }

    @Override
    public void reprogramar(Campana campana) {
        // Transición permitida: Pausada → Programada
        // Esta es la clave: permite reiniciar el ciclo
        campana.setEstado(new EstadoProgramada());
    }

    @Override
    public void archivar(Campana campana) {
        throw new EstadoIlegalException(
                "No se puede archivar una campaña pausada. Debe finalizarla o cancelarla primero.");
    }

    @Override
    public String getNombre() {
        return "Pausada";
    }
}
