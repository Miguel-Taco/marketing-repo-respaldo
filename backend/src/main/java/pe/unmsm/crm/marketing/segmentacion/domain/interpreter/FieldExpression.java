package pe.unmsm.crm.marketing.segmentacion.domain.interpreter;

import lombok.AllArgsConstructor;
import java.util.Map;

@AllArgsConstructor
public class FieldExpression implements SegmentExpression {
    private String field;
    private String operator;
    private Object value;

    @Override
    public boolean interpret(Map<String, Object> context) {
        Object contextValue = context.get(field);
        if (contextValue == null)
            return false;

        // Simplified logic for demonstration
        switch (operator) {
            case "EQUALS":
                return contextValue.equals(value);
            // Add more operators
            default:
                return false;
        }
    }
}
