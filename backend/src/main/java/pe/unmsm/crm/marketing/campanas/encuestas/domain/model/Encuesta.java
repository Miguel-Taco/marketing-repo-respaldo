package pe.unmsm.crm.marketing.campanas.encuestas.domain.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "Encuesta")
public class Encuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_encuesta")
    private Integer idEncuesta;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoEncuesta estado = EstadoEncuesta.BORRADOR;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion", nullable = false)
    private LocalDateTime fechaModificacion;

    @OneToMany(mappedBy = "encuesta", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Pregunta> preguntas = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaModificacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaModificacion = LocalDateTime.now();
    }

    public enum EstadoEncuesta {
        BORRADOR,
        ACTIVA,
        ARCHIVADA
    }
}
