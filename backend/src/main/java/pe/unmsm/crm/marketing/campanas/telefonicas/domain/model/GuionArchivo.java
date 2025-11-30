package pe.unmsm.crm.marketing.campanas.telefonicas.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que representa los metadatos de un archivo de guión almacenado en
 * Supabase.
 * Los archivos físicos se almacenan en Supabase Storage, esta entidad solo
 * guarda la metadata.
 */
@Entity
@Table(name = "guion")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuionArchivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_campania", nullable = false)
    private Long idCampania;

    /**
     * ID del agente que creó el guión.
     * Si es NULL, el guión es general de la campaña.
     */
    @Column(name = "id_agente")
    private Long idAgente;

    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "creado_por", nullable = false)
    private Long creadoPor;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion", nullable = false)
    private LocalDateTime fechaModificacion;

    /**
     * Ruta completa en Supabase Storage.
     * Ejemplo: campana/1/general/guion-ventas.md
     */
    @Column(name = "ruta_supabase", nullable = false, length = 500)
    private String rutaSupabase;

    /**
     * Tipo de archivo, siempre "md" para markdown.
     */
    @Column(name = "tipo_archivo", nullable = false, length = 10)
    private String tipoArchivo;

    @Column(name = "estado", nullable = false, length = 45)
    private String estado;

    @Column(name = "activo")
    private Boolean activo;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (fechaCreacion == null) {
            fechaCreacion = now;
        }
        if (fechaModificacion == null) {
            fechaModificacion = now;
        }
        if (tipoArchivo == null) {
            tipoArchivo = "md";
        }
        if (estado == null) {
            estado = "BORRADOR";
        }
        if (activo == null) {
            activo = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaModificacion = LocalDateTime.now();
    }

    /**
     * Verifica si el guión es general (no específico de un agente).
     */
    public boolean esGeneral() {
        return idAgente == null;
    }
}
