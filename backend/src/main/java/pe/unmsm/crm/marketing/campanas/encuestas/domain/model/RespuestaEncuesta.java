package pe.unmsm.crm.marketing.campanas.encuestas.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import pe.unmsm.crm.marketing.leads.domain.model.Lead;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "Respuesta_Encuesta")
public class RespuestaEncuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_respuesta_encuesta")
    private Integer idRespuestaEncuesta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_encuesta", nullable = false)
    private Encuesta encuesta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id", nullable = false)
    private Lead lead;

    @Column(name = "fecha_respuesta", nullable = false)
    private LocalDateTime fechaRespuesta;

    @OneToMany(mappedBy = "respuestaEncuesta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Respuesta_Detalle> detalles = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        fechaRespuesta = LocalDateTime.now();
    }
}
