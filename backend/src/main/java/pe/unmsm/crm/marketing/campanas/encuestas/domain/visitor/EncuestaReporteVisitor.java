package pe.unmsm.crm.marketing.campanas.encuestas.domain.visitor;

import pe.unmsm.crm.marketing.campanas.encuestas.domain.model.Encuesta;

public class EncuestaReporteVisitor implements EncuestaVisitor {
    @Override
    public void visit(Encuesta encuesta) {
        // Generate report logic
    }
}
