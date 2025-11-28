package pe.unmsm.crm.marketing.campanas.mailing.domain.model;

@Entity
@Table(name = "interacciones_log")
public class InteraccionLog {
    @Id
    @GeneratedValue
    private Integer id;
    private Integer id_campana_mailing;  // FK
    private Integer id_tipo_evento;      // FK a cat_tipos_interaccion
    private Integer id_contacto_crm;     // Lead ID
    private LocalDateTime fecha_evento = now();
}