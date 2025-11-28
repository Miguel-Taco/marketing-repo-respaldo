package pe.unmsm.crm.marketing.campanas.encuestas.domain.template;

public abstract class FlujoEncuestaTemplate {
    public final void ejecutarFlujo() {
        mostrar();
        recoger();
        calcular();
        notificar();
    }

    protected abstract void mostrar();

    protected abstract void recoger();

    protected abstract void calcular();

    protected abstract void notificar();
}
