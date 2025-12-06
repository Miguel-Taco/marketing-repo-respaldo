package pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "grabacion_llamada", schema = "railway")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrabacionLlamadaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_campania", nullable = false)
    private Integer idCampania;

    @Column(name = "id_agente", nullable = false)
    private Integer idAgente;

    @Column(name = "id_lead", nullable = false)
    private Long idLead;

    @Column(name = "id_llamada")
    private Integer idLlamada;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "duracion_segundos", nullable = false)
    private Integer duracionSegundos;

    @Column(name = "ruta_audio_firebase", nullable = false, length = 500)
    private String rutaAudioFirebase;

    @Column(name = "ruta_transcripcion_supabase", length = 500)
    private String rutaTranscripcionSupabase;

    @Column(name = "estado_procesamiento", nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoProcesamiento estadoProcesamiento = EstadoProcesamiento.PENDIENTE;

    @Column(name = "resultado", length = 100)
    private String resultado;

    @Column(name = "mensaje_error", columnDefinition = "TEXT")
    private String mensajeError;

    @Column(name = "intentos_procesamiento", nullable = false)
    private Integer intentosProcesamiento = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_campania", insertable = false, updatable = false)
    private CampaniaTelefonicaEntity campania;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_agente", insertable = false, updatable = false)
    private AgenteMarketingEntity agente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_llamada", insertable = false, updatable = false)
    private LlamadaEntity llamada;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (fechaHora == null) {
            fechaHora = LocalDateTime.now();
        }
        if (estadoProcesamiento == null) {
            estadoProcesamiento = EstadoProcesamiento.PENDIENTE;
        }
        if (intentosProcesamiento == null) {
            intentosProcesamiento = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum EstadoProcesamiento {
        PENDIENTE,
        PROCESANDO,
        COMPLETADO,
        ERROR
    }
}
