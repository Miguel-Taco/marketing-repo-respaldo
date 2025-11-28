package pe.unmsm.crm.marketing.campanas.encuestas.domain.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "Pregunta", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "id_encuesta", "orden" })
})
public class Pregunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pregunta")
    private Integer idPregunta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_encuesta", nullable = false)
    @JsonBackReference
    @ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    private Encuesta encuesta;

    @Column(name = "texto_pregunta", nullable = false, columnDefinition = "TEXT")
    private String textoPregunta;

    @Column(name = "tipo_pregunta", nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoPregunta tipoPregunta;

    @Column(nullable = false)
    private Integer orden;

    @OneToMany(mappedBy = "pregunta", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Opcion> opciones = new ArrayList<>();

    public enum TipoPregunta {
        UNICA,
        MULTIPLE,
        ESCALA
    }
}
