package pe.unmsm.crm.marketing.campanas.gestor.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que representa el historial de auditoría de acciones sobre campanas.
 * Tabla: historial_campana
 */
@Entity
@Table(name = "historial_campana")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistorialCampana {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historial")
    private Long idHistorial;

    @Column(name = "id_campana", nullable = false)
    private Long idCampana;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_campana", insertable = false, updatable = false)
    private Campana campana;

    @Column(name = "fecha_accion", nullable = false, updatable = false)
    private LocalDateTime fechaAccion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_accion", nullable = false, length = 100)
    private TipoAccion tipoAccion;

    @Column(name = "descripcion_detalle", columnDefinition = "TEXT")
    private String descripcionDetalle;

    @PrePersist
    protected void onCreate() {
        if (fechaAccion == null) {
            fechaAccion = LocalDateTime.now();
        }
    }
}
