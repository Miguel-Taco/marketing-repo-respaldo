package pe.unmsm.crm.marketing.campanas.encuestas.domain.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "Respuesta_Detalle")
public class Respuesta_Detalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_respuesta_detalle")
    private Integer idRespuestaDetalle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_respuesta_encuesta", nullable = false)
    private RespuestaEncuesta respuestaEncuesta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pregunta", nullable = false)
    private Pregunta pregunta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_opcion")
    private Opcion opcion;

    @Column(name = "valor_respuesta")
    private Byte valorRespuesta;
}
