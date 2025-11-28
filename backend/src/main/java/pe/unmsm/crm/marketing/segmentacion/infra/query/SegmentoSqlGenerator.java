package pe.unmsm.crm.marketing.segmentacion.infra.query;

import org.springframework.stereotype.Component;
import pe.unmsm.crm.marketing.segmentacion.domain.model.Segmento;
import pe.unmsm.crm.marketing.segmentacion.domain.visitor.SqlExportVisitor;

@Component
public class SegmentoSqlGenerator {

    public String generarSql(Segmento segmento) {
        if (segmento.getReglaPrincipal() == null) {
            return "SELECT id FROM leads";
        }
        SqlExportVisitor visitor = new SqlExportVisitor();
        String whereClause = segmento.getReglaPrincipal().accept(visitor);

        if (whereClause == null || whereClause.trim().isEmpty()) {
            return "SELECT id FROM leads";
        }

        return "SELECT id FROM leads WHERE " + whereClause;
    }
}
