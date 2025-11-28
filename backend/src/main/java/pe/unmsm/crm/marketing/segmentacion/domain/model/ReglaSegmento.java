package pe.unmsm.crm.marketing.segmentacion.domain.model;

import pe.unmsm.crm.marketing.segmentacion.domain.visitor.ReglaVisitor;

public abstract class ReglaSegmento {
    public abstract <T> T accept(ReglaVisitor<T> visitor);
}
