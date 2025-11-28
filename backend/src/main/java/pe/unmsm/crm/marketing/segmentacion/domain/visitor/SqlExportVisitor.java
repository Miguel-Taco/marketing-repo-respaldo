package pe.unmsm.crm.marketing.segmentacion.domain.visitor;

import pe.unmsm.crm.marketing.segmentacion.domain.model.GrupoReglasAnd;
import pe.unmsm.crm.marketing.segmentacion.domain.model.GrupoReglasOr;

import pe.unmsm.crm.marketing.segmentacion.domain.model.ReglaSimple;

import java.util.stream.Collectors;

public class SqlExportVisitor implements ReglaVisitor<String> {

    @Override
    public String visit(ReglaSimple regla) {
        String campo = regla.getCampo();
        String operador = regla.getOperador();
        String valor = regla.getValorTexto();

        // Manejo de valores nulos
        if (valor == null) {
            valor = "";
        }

        // Determinar si el campo es numérico
        boolean esNumerico = isNumericField(campo);

        // Formatear valor según tipo y operador
        String valorSql;
        String operadorSql = mapOperator(operador);

        if ("CONTIENE".equalsIgnoreCase(operador)) {
            valorSql = "'%" + valor + "%'";
        } else if ("EMPIEZA_CON".equalsIgnoreCase(operador)) {
            valorSql = "'" + valor + "%'";
        } else if ("TERMINA_CON".equalsIgnoreCase(operador)) {
            valorSql = "'%" + valor + "'";
        } else {
            if (esNumerico) {
                // Si es numérico y está vacío, usar 0 o manejar error
                if (valor.trim().isEmpty()) {
                    valor = "0";
                }
                valorSql = valor;
            } else {
                valorSql = "'" + valor + "'";
            }
        }

        return String.format("%s %s %s", campo, operadorSql, valorSql);
    }

    @Override
    public String visit(GrupoReglasAnd grupo) {
        if (grupo.getReglas().isEmpty())
            return "1=1";
        return "(" + grupo.getReglas().stream()
                .map(r -> r.accept(this))
                .collect(Collectors.joining(" AND ")) + ")";
    }

    @Override
    public String visit(GrupoReglasOr grupo) {
        if (grupo.getReglas().isEmpty())
            return "1=0";
        return "(" + grupo.getReglas().stream()
                .map(r -> r.accept(this))
                .collect(Collectors.joining(" OR ")) + ")";
    }

    private boolean isNumericField(String campo) {
        return "edad".equalsIgnoreCase(campo) ||
                "ingresos".equalsIgnoreCase(campo) ||
                "score".equalsIgnoreCase(campo);
    }

    private String mapOperator(String operator) {
        if (operator == null)
            return "=";

        switch (operator.toUpperCase()) {
            case "IGUAL":
                return "=";
            case "DIFERENTE":
                return "<>";
            case "MAYOR_QUE":
                return ">";
            case "MENOR_QUE":
                return "<";
            case "MAYOR_IGUAL":
                return ">=";
            case "MENOR_IGUAL":
                return "<=";
            case "CONTIENE":
            case "EMPIEZA_CON":
            case "TERMINA_CON":
                return "LIKE";
            default:
                return "=";
        }
    }
}
