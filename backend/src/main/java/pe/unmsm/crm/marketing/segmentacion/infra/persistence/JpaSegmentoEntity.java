package pe.unmsm.crm.marketing.segmentacion.infra.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "segmento")
@Getter
@Setter
public class JpaSegmentoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_segmento")
    private Long idSegmento;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "tipo_audiencia")
    private String tipoAudiencia;

    @Column(name = "estado")
    private String estado;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "segmento", fetch = FetchType.LAZY)
    private List<JpaSegmentoFiltroEntity> filtros = new ArrayList<>();
}
