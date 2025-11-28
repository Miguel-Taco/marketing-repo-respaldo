package pe.unmsm.crm.marketing.campanas.encuestas.domain.prototype;

import pe.unmsm.crm.marketing.campanas.encuestas.domain.model.Encuesta;

public class EncuestaPrototype implements Cloneable {
    private Encuesta encuesta;

    public EncuestaPrototype(Encuesta encuesta) {
        this.encuesta = encuesta;
    }

    public Encuesta getEncuesta() {
        return encuesta;
    }

    @Override
    public EncuestaPrototype clone() {
        try {
            return (EncuestaPrototype) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
