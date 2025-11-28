package pe.unmsm.crm.marketing.segmentacion.domain.interpreter;

import java.util.Map;

public interface SegmentExpression {
    boolean interpret(Map<String, Object> context);
}
