package pe.unmsm.crm.marketing.campanas.gestor.domain.state;

import pe.unmsm.crm.marketing.campanas.gestor.domain.exception.EstadoIlegalException;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.Campana;

/**
 * Estado Borrador: Estado inicial de una campaña recién creada.
 * Permite: editar(), programar()
 */
public class EstadoBorrador implements EstadoCampana {

    @Override
    public void programar(Campana campana) {
        // Transición permitida: Borrador → Programada
        campana.setEstado(new EstadoProgramada());
    }

    @Override
    public void activar(Campana campana) {
        throw new EstadoIlegalException(
                "No se puede activar una campaña en estado Borrador. Debe programarse primero.");
    }

    @Override
    public void pausar(Campana campana) {
        throw new EstadoIlegalException("No se puede pausar una campaña en estado Borrador.");
    }

    @Override
    public void reanudar(Campana campana) {
        throw new EstadoIlegalException("No se puede reanudar una campaña en estado Borrador.");
    }

    @Override
    public void cancelar(Campana campana) {
        throw new EstadoIlegalException(
                "No se puede cancelar una campaña en estado Borrador. Use eliminar en su lugar.");
    }

    @Override
    public void finalizar(Campana campana) {
        throw new EstadoIlegalException("No se puede finalizar una campaña en estado Borrador.");
    }

    @Override
    public void editar(Campana campana) {
        // Permitido: las campanas en Borrador pueden editarse libremente
        // La lógica de actualización se maneja en el servicio
    }

    @Override
    public void reprogramar(Campana campana) {
        throw new EstadoIlegalException("No se puede reprogramar una campaña en estado Borrador. Use programar.");
    }

    @Override
    public void archivar(Campana campana) {
        throw new EstadoIlegalException(
                "No se puede archivar una campaña en estado Borrador. Debe finalizar o cancelar primero.");
    }

    @Override
    public String getNombre() {
        return "Borrador";
    }
}
