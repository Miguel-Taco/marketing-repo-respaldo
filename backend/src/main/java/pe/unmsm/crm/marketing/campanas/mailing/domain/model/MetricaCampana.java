package pe.unmsm.crm.marketing.campanas.mailing.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "metricas_campana")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricaCampana {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "id_campana_mailing", nullable = false, unique = true)
    private CampanaMailing campanaMailing;

    @Column(name = "enviados", nullable = false)
    @Builder.Default
    private Integer enviados = 0;

    @Column(name = "entregados", nullable = false)
    @Builder.Default
    private Integer entregados = 0;

    @Column(name = "aperturas", nullable = false)
    @Builder.Default
    private Integer aperturas = 0;

    @Column(name = "clics", nullable = false)
    @Builder.Default
    private Integer clics = 0;

    @Column(name = "rebotes", nullable = false)
    @Builder.Default
    private Integer rebotes = 0;

    @Column(name = "bajas", nullable = false)
    @Builder.Default
    private Integer bajas = 0;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    @PreUpdate
    protected void onUpdate() {
        actualizadoEn = LocalDateTime.now();
    }
}