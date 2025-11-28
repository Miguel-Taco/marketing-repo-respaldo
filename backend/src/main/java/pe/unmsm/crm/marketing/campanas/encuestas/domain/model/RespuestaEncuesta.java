package pe.unmsm.crm.marketing.campanas.encuestas.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

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

    @Column(name = "id_cliente", nullable = false)
    private String idCliente;

    @Column(name = "fecha_respuesta", nullable = false)
    private LocalDateTime fechaRespuesta;
}
