package pe.unmsm.crm.marketing.campanas.encuestas.domain.visitor;

import pe.unmsm.crm.marketing.campanas.encuestas.domain.model.Encuesta;

public interface EncuestaVisitor {
    void visit(Encuesta encuesta);
}
