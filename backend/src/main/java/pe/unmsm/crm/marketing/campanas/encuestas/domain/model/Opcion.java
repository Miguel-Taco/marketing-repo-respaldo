package pe.unmsm.crm.marketing.campanas.encuestas.domain.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "Opcion", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "id_pregunta", "orden" })
})
public class Opcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_opcion")
    private Integer idOpcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pregunta", nullable = false)
    @JsonBackReference
    @ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    private Pregunta pregunta;

    @Column(name = "texto_opcion", nullable = false)
    private String textoOpcion;

    @Column(nullable = false)
    private Integer orden;

    @Column(name = "es_alerta_urgente", nullable = false)
    private Boolean esAlertaUrgente = false;
}
