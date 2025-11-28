package pe.unmsm.crm.marketing.segmentacion.domain.visitor;

import pe.unmsm.crm.marketing.segmentacion.domain.model.GrupoReglasAnd;
import pe.unmsm.crm.marketing.segmentacion.domain.model.GrupoReglasOr;
import pe.unmsm.crm.marketing.segmentacion.domain.model.ReglaSimple;

public interface ReglaVisitor<T> {
    T visit(ReglaSimple regla);

    T visit(GrupoReglasAnd grupo);

    T visit(GrupoReglasOr grupo);
}
