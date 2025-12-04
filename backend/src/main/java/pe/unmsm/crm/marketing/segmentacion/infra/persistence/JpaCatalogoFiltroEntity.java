package pe.unmsm.crm.marketing.segmentacion.infra.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "catalogo_filtro")
@Getter
@Setter
public class JpaCatalogoFiltroEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_filtro")
    private Long idFiltro;

    @Column(name = "categoria")
    private String categoria;

    @Column(name = "campo")
    private String campo;

    @Column(name = "tipo_dato")
    private String tipoDato;

    @Column(name = "operadores_permitidos")
    private String operadoresPermitidos;

    @Column(name = "tipo_audiencia")
    private String tipoAudiencia; // LEAD, CLIENTE, AMBOS
}
