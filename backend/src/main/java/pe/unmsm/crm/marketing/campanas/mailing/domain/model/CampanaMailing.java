package pe.unmsm.crm.marketing.campanas.mailing.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "campanas_mailing", indexes = {
    @Index(name = "idx_mailing_estado", columnList = "id_estado"),
    @Index(name = "idx_mailing_agente_estado", columnList = "id_agente_asignado, id_estado"),
    @Index(name = "idx_mailing_fecha_inicio", columnList = "fecha_inicio")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampanaMailing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Referencias a otros módulos
    @Column(name = "id_campana_gestion", nullable = false)
    private Long idCampanaGestion;

    @Column(name = "id_segmento", nullable = false)
    private Long idSegmento;

    @Column(name = "id_encuesta", nullable = false)
    private Integer idEncuesta;

    @Column(name = "id_agente_asignado", nullable = false)
    private Integer idAgenteAsignado;

    // Estado y Prioridad
    @Column(name = "id_estado", nullable = false)
    @Builder.Default  // ✅ AGREGADO
    private Integer idEstado = 1; // PENDIENTE por defecto

    @Column(name = "prioridad", length = 20, nullable = false)
    @Builder.Default  // ✅ AGREGADO
    private String prioridad = "Media";

    // Datos informativos (del Gestor, read-only)
    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "tematica", length = 255)
    private String tematica;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin;

    // Contenido editable
    @Column(name = "asunto", length = 255)
    private String asunto;

    @Column(name = "cuerpo", columnDefinition = "LONGTEXT")
    private String cuerpo;

    @Column(name = "cta_texto", length = 100)
    private String ctaTexto;

    // CTA URL (del Gestor, read-only, NO editable)
    @Column(name = "cta_url", length = 2048)
    private String ctaUrl;

    // Auditoría
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    // Relación OneToOne con Métricas
    @OneToOne(mappedBy = "campanaMailing", cascade = CascadeType.ALL, orphanRemoval = true)
    private MetricaCampana metricas;

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        fechaCreacion = now;
        fechaActualizacion = now;
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
