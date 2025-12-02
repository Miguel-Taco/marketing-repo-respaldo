package pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "envio_encuesta", schema = "railway")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnvioEncuestaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_llamada", nullable = false)
    private Integer idLlamada;

    @Column(name = "id_encuesta", nullable = false)
    private Integer idEncuesta;

    @Column(name = "id_lead", nullable = false)
    private Long idLead;

    @Column(name = "telefono_destino", nullable = false, length = 20)
    private String telefonoDestino;

    @Column(name = "url_encuesta", nullable = false, length = 500)
    private String urlEncuesta;

    @Column(name = "fecha_envio", nullable = false)
    private LocalDateTime fechaEnvio;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoEnvio estado;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_comunicacion", nullable = false)
    private MetodoComunicacion metodoComunicacion;

    @Column(name = "mensaje_error", columnDefinition = "TEXT")
    private String mensajeError;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_llamada", insertable = false, updatable = false)
    private LlamadaEntity llamada;

    public enum EstadoEnvio {
        ENVIADA,
        ERROR,
        PENDIENTE
    }

    public enum MetodoComunicacion {
        SMS,
        WHATSAPP,
        EMAIL
    }
}
