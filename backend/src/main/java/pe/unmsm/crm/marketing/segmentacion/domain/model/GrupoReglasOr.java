package pe.unmsm.crm.marketing.segmentacion.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.unmsm.crm.marketing.segmentacion.domain.visitor.ReglaVisitor;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GrupoReglasOr extends ReglaSegmento {
    private List<ReglaSegmento> reglas = new ArrayList<>();

    public void addRegla(ReglaSegmento regla) {
        this.reglas.add(regla);
    }

    @Override
    public <T> T accept(ReglaVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
