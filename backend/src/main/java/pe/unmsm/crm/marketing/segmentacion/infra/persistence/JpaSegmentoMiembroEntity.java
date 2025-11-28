package pe.unmsm.crm.marketing.segmentacion.infra.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "segmento_miembro")
@Getter
@Setter
@IdClass(JpaSegmentoMiembroId.class)
public class JpaSegmentoMiembroEntity {
    @Id
    @Column(name = "id_segmento")
    private Long idSegmento;

    @Id
    @Column(name = "tipo_miembro")
    private String tipoMiembro;

    @Id
    @Column(name = "id_miembro")
    private Long idMiembro;

    @Column(name = "fecha_agregado")
    private LocalDateTime fechaAgregado;
}
