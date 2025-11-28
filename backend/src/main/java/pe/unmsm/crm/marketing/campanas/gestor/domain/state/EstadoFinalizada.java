package pe.unmsm.crm.marketing.campanas.gestor.domain.state;

import pe.unmsm.crm.marketing.campanas.gestor.domain.exception.EstadoIlegalException;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.Campana;

/**
 * Estado Finalizada: Estado terminal. La campaña completó su ejecución
 * exitosamente.
 * Solo permite: archivar()
 */
public class EstadoFinalizada implements EstadoCampana {

    @Override
    public void programar(Campana campana) {
        throw new EstadoIlegalException("No se puede programar una campaña finalizada.");
    }

    @Override
    public void activar(Campana campana) {
        throw new EstadoIlegalException("No se puede activar una campaña finalizada.");
    }

    @Override
    public void pausar(Campana campana) {
        throw new EstadoIlegalException("No se puede pausar una campaña finalizada.");
    }

    @Override
    public void reanudar(Campana campana) {
        throw new EstadoIlegalException("No se puede reanudar una campaña finalizada.");
    }

    @Override
    public void cancelar(Campana campana) {
        throw new EstadoIlegalException("No se puede cancelar una campaña finalizada.");
    }

    @Override
    public void finalizar(Campana campana) {
        throw new EstadoIlegalException("La campaña ya está finalizada.");
    }

    @Override
    public void editar(Campana campana) {
        throw new EstadoIlegalException("No se puede editar una campaña finalizada.");
    }

    @Override
    public void reprogramar(Campana campana) {
        throw new EstadoIlegalException(
                "No se puede reprogramar una campaña finalizada. Duplique la campaña en su lugar.");
    }

    @Override
    public void archivar(Campana campana) {
        // Permitido: Finalizada puede archivarse
        campana.setEsArchivado(true);
    }

    @Override
    public String getNombre() {
        return "Finalizada";
    }
}
