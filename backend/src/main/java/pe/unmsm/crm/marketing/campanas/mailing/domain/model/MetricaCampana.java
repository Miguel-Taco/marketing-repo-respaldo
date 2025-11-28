package pe.unmsm.crm.marketing.campanas.mailing.domain.model;

@Entity
@Table(name = "metricas_campana")
public class MetricaCampana {
    @Id
    private Integer id;
    @Column(unique = true)
    private Integer id_campana_mailing;  // FK
    private Integer enviados = 0;
    private Integer entregados = 0;
    private Integer aperturas = 0;
    private Integer clics = 0;
    private Integer rebotes = 0;
    private Integer bajas = 0;
    private LocalDateTime actualizado_en;
}
