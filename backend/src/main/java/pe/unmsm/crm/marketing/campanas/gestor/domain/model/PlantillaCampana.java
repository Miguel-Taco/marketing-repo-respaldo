package pe.unmsm.crm.marketing.campanas.gestor.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que representa una plantilla reutilizable para crear campanas.
 * Tabla: plantilla_campana
 */
@Entity
@Table(name = "plantilla_campana")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlantillaCampana {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_plantilla")
    private Integer idPlantilla;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "tematica", nullable = false, length = 150)
    private String tematica;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal_ejecucion", length = 10)
    private CanalEjecucion canalEjecucion;

    @Column(name = "id_segmento")
    private Long idSegmento;

    @Column(name = "id_encuesta")
    private Integer idEncuesta;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion", nullable = false)
    private LocalDateTime fechaModificacion;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        fechaCreacion = now;
        fechaModificacion = now;
    }

    @PreUpdate
    protected void onUpdate() {
        fechaModificacion = LocalDateTime.now();
    }
}
