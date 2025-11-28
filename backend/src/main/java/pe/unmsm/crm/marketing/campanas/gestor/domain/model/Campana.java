package pe.unmsm.crm.marketing.campanas.gestor.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.unmsm.crm.marketing.campanas.gestor.domain.state.EstadoBorrador;
import pe.unmsm.crm.marketing.campanas.gestor.domain.state.EstadoCampana;
import pe.unmsm.crm.marketing.campanas.gestor.domain.state.converter.EstadoCampanaConverter;

import java.time.LocalDateTime;

/**
 * Entidad Campana (Agregado Raíz).
 * Representa una campaña de marketing que puede ejecutarse vía Mailing o
 * Llamadas.
 * Usa el Patrón State para gestionar su ciclo de vida.
 * Tabla: campana
 */
@Entity
@Table(name = "campana")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Campana {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_campana")
    private Long idCampana;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "tematica", nullable = false, length = 150)
    private String tematica;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Convert(converter = EstadoCampanaConverter.class)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoCampana estado;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridad", nullable = false, length = 10)
    @Builder.Default
    private Prioridad prioridad = Prioridad.Media;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal_ejecucion", nullable = false, length = 10)
    private CanalEjecucion canalEjecucion;

    @Column(name = "fecha_programada_inicio")
    private LocalDateTime fechaProgramadaInicio;

    @Column(name = "fecha_programada_fin")
    private LocalDateTime fechaProgramadaFin;

    @Column(name = "id_plantilla")
    private Integer idPlantilla;

    @Column(name = "id_agente")
    private Integer idAgente;

    @Column(name = "id_segmento")
    private Long idSegmento;

    @Column(name = "id_encuesta")
    private Integer idEncuesta;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion", nullable = false)
    private LocalDateTime fechaModificacion;

    @Column(name = "es_archivado", nullable = false)
    @Builder.Default
    private Boolean esArchivado = false;

    // ========== Métodos de Negocio (Delegación al Estado) ==========

    /**
     * Transición: Borrador → Programada
     */
    public void programar() {
        this.estado.programar(this);
    }

    /**
     * Transición: Programada → Vigente
     */
    public void activar() {
        this.estado.activar(this);
    }

    /**
     * Transición: Vigente → Pausada
     */
    public void pausar() {
        this.estado.pausar(this);
    }

    /**
     * Transición: Pausada → Vigente
     */
    public void reanudar() {
        this.estado.reanudar(this);
    }

    /**
     * Transición: Programada/Vigente/Pausada → Cancelada
     */
    public void cancelar() {
        this.estado.cancelar(this);
    }

    /**
     * Transición: Vigente → Finalizada
     */
    public void finalizar() {
        this.estado.finalizar(this);
    }

    /**
     * Permite editar la campaña (solo en Borrador o Pausada)
     */
    public void editar() {
        this.estado.editar(this);
    }

    /**
     * Transición: Programada (mantiene) o Pausada → Programada
     */
    public void reprogramar() {
        this.estado.reprogramar(this);
    }

    /**
     * Marca la campaña como archivada (solo Finalizada/Cancelada)
     */
    public void archivar() {
        this.estado.archivar(this);
    }

    // ========== Lifecycle Callbacks ==========

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        fechaCreacion = now;
        fechaModificacion = now;

        // Estado inicial: Borrador
        if (estado == null) {
            estado = new EstadoBorrador();
        }

        // Valor por defecto
        if (esArchivado == null) {
            esArchivado = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaModificacion = LocalDateTime.now();
    }
}
