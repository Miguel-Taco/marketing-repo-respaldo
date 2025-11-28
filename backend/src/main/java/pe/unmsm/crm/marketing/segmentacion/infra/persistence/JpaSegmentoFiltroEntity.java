package pe.unmsm.crm.marketing.segmentacion.infra.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "segmento_filtro")
@Getter
@Setter
public class JpaSegmentoFiltroEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_segmento_filtro")
    private Long idSegmentoFiltro;

    @ManyToOne
    @JoinColumn(name = "id_segmento")
    private JpaSegmentoEntity segmento;

    @Column(name = "id_filtro")
    private Long idFiltro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_filtro", insertable = false, updatable = false)
    private JpaCatalogoFiltroEntity catalogo;

    @Column(name = "operador")
    private String operador;

    @Column(name = "valor_texto")
    private String valorTexto;

    @Column(name = "valor_numero_desde")
    private BigDecimal valorNumeroDesde;

    @Column(name = "valor_numero_hasta")
    private BigDecimal valorNumeroHasta;

    @Column(name = "valor_fecha_desde")
    private LocalDate valorFechaDesde;

    @Column(name = "valor_fecha_hasta")
    private LocalDate valorFechaHasta;
}
