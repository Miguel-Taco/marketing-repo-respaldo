package pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "campania_telefonica", schema = "railway")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaniaTelefonicaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_campana_gestion", nullable = false)
    private Long idCampanaGestion;

    @Column(name = "id_segmento", nullable = false)
    private Long idSegmento;

    @Column(name = "id_encuesta")
    private Integer idEncuesta;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    // REMOVED: id_guion field - column doesn't exist in database table
    // If you need to associate scripts with campaigns, add the column to the
    // database first

    @Column(name = "estado", nullable = false, length = 45)
    private String estado = "BORRADOR";

    @Column(name = "id_estado", nullable = false)
    private Integer idEstado = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridad", nullable = false)
    private PrioridadEnum prioridad = PrioridadEnum.Media;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion", nullable = false)
    private LocalDateTime fechaModificacion;

    @Column(name = "es_archivado", nullable = false)
    private Boolean esArchivado = false;

    // Relaciones
    // REMOVED: GuionEntity relationship - id_guion column doesn't exist in database

    @OneToMany(mappedBy = "campania", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CampaniaAgenteEntity> agentes = new ArrayList<>();

    @OneToOne(mappedBy = "campania", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private CampaniaTelefonicaConfigEntity config;

    @OneToMany(mappedBy = "campania")
    private List<ColaLlamadaEntity> cola = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaModificacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaModificacion = LocalDateTime.now();
    }

    public enum PrioridadEnum {
        Alta, Media, Baja
    }
}
