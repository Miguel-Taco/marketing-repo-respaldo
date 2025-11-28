package pe.unmsm.crm.marketing.segmentacion.domain.visitor;

import pe.unmsm.crm.marketing.segmentacion.domain.model.GrupoReglasAnd;
import pe.unmsm.crm.marketing.segmentacion.domain.model.GrupoReglasOr;
import pe.unmsm.crm.marketing.segmentacion.domain.model.ReglaSimple;

import java.util.stream.Collectors;

public class ExplanationVisitor implements ReglaVisitor<String> {

    @Override
    public String visit(ReglaSimple regla) {
        return String.format("%s es %s %s", regla.getCampo(), regla.getOperador(), regla.getValorTexto());
    }

    @Override
    public String visit(GrupoReglasAnd grupo) {
        return "(" + grupo.getReglas().stream()
                .map(r -> r.accept(this))
                .collect(Collectors.joining(" Y ")) + ")";
    }

    @Override
    public String visit(GrupoReglasOr grupo) {
        return "(" + grupo.getReglas().stream()
                .map(r -> r.accept(this))
                .collect(Collectors.joining(" O ")) + ")";
    }
}
