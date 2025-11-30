package pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "guion_seccion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuionSeccionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_guion", nullable = false)
    private GuionEntity guion;

    @Column(name = "tipo_seccion", nullable = false, length = 50)
    private String tipoSeccion;

    @Column(name = "contenido", columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "orden", nullable = false)
    private Integer orden;
}
