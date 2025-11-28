package pe.unmsm.crm.marketing.segmentacion.domain.interpreter;

import lombok.AllArgsConstructor;
import java.util.Map;

@AllArgsConstructor
public class AndExpression implements SegmentExpression {
    private SegmentExpression expr1;
    private SegmentExpression expr2;

    @Override
    public boolean interpret(Map<String, Object> context) {
        return expr1.interpret(context) && expr2.interpret(context);
    }
}
