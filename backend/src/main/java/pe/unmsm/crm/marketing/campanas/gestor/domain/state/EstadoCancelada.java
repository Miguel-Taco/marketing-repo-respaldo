package pe.unmsm.crm.marketing.campanas.gestor.domain.state;

import pe.unmsm.crm.marketing.campanas.gestor.domain.exception.EstadoIlegalException;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.Campana;

/**
 * Estado Cancelada: Estado terminal. La campaña fue cancelada antes de
 * completarse.
 * Solo permite: archivar()
 */
public class EstadoCancelada implements EstadoCampana {

    @Override
    public void programar(Campana campana) {
        throw new EstadoIlegalException("No se puede programar una campaña cancelada.");
    }

    @Override
    public void activar(Campana campana) {
        throw new EstadoIlegalException("No se puede activar una campaña cancelada.");
    }

    @Override
    public void pausar(Campana campana) {
        throw new EstadoIlegalException("No se puede pausar una campaña cancelada.");
    }

    @Override
    public void reanudar(Campana campana) {
        throw new EstadoIlegalException("No se puede reanudar una campaña cancelada.");
    }

    @Override
    public void cancelar(Campana campana) {
        throw new EstadoIlegalException("La campaña ya está cancelada.");
    }

    @Override
    public void finalizar(Campana campana) {
        throw new EstadoIlegalException("No se puede finalizar una campaña cancelada.");
    }

    @Override
    public void editar(Campana campana) {
        throw new EstadoIlegalException("No se puede editar una campaña cancelada.");
    }

    @Override
    public void reprogramar(Campana campana) {
        throw new EstadoIlegalException(
                "No se puede reprogramar una campaña cancelada. Duplique la campaña en su lugar.");
    }

    @Override
    public void archivar(Campana campana) {
        // Permitido: Cancelada puede archivarse
        campana.setEsArchivado(true);
    }

    @Override
    public String getNombre() {
        return "Cancelada";
    }
}
