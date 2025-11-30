package pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "guion", schema = "railway")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_campania")
    private Long idCampania; // Nullable for standalone structured scripts

    @Column(name = "id_agente")
    private Long idAgente;

    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "objetivo", columnDefinition = "TEXT")
    private String objetivo;

    @Column(name = "tipo", length = 50)
    private String tipo;

    @Column(name = "notas_internas", columnDefinition = "TEXT")
    private String notasInternas;

    @Column(name = "ruta_supabase", nullable = false, length = 500)
    private String rutaSupabase = ""; // Empty for structured scripts

    @Column(name = "tipo_archivo", nullable = false, length = 10)
    private String tipoArchivo = "md";

    @Column(name = "estado", nullable = false, length = 45)
    private String estado = "BORRADOR";

    @Column(name = "creado_por", nullable = false)
    private Long creadoPor = 1L; // Default user ID

    @Column(name = "activo")
    private Boolean activo = true;

    @OneToMany(mappedBy = "guion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orden ASC")
    private List<GuionSeccionEntity> secciones = new ArrayList<>();
}
