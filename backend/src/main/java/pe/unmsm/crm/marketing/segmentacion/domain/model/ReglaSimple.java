package pe.unmsm.crm.marketing.segmentacion.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.unmsm.crm.marketing.segmentacion.domain.visitor.ReglaVisitor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReglaSimple extends ReglaSegmento {
    private Long idFiltro; // Reference to CatalogoFiltro
    private String campo;
    private String operador;

    // Values for different types
    private String valorTexto;
    private BigDecimal valorNumeroDesde;
    private BigDecimal valorNumeroHasta;
    private LocalDate valorFechaDesde;
    private LocalDate valorFechaHasta;

    @Override
    public <T> T accept(ReglaVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
