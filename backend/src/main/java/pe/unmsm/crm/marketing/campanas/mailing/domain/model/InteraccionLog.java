package pe.unmsm.crm.marketing.campanas.mailing.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "interacciones_log", indexes = {
    @Index(name = "idx_logs_campana_id", columnList = "id_campana_mailing"),
    @Index(name = "idx_logs_contacto_evento", columnList = "id_contacto_crm, id_tipo_evento")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InteraccionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_campana_mailing", nullable = false)
    private Integer idCampanaMailingId;

    @Column(name = "id_tipo_evento", nullable = false)
    private Integer idTipoEvento;

    @Column(name = "id_contacto_crm", nullable = false)
    private Long idContactoCrm;

    @Column(name = "fecha_evento", nullable = false, updatable = false)
    private LocalDateTime fechaEvento;

    @PrePersist
    protected void onCreate() {
        if (fechaEvento == null) {
            fechaEvento = LocalDateTime.now();
        }
    }
}