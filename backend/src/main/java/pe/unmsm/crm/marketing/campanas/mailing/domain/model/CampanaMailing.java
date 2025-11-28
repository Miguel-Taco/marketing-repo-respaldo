package pe.unmsm.crm.marketing.campanas.mailing.domain.model;

@Entity
@Table(name = "campanas_mailing")
public class CampanaMailing {
    @Id
    private Integer id;
    private Long id_campana_gestion;
    private Long id_segmento;
    private Integer id_encuesta;
    private Integer id_agente_asignado;
    private Integer id_estado;  // FK a cat_estados_campana
    private String prioridad;
    private String nombre;
    private String descripcion;
    private String tematica;
    private LocalDateTime fecha_inicio;
    private LocalDateTime fecha_fin;
    private String asunto;      // Editable
    private String cuerpo;      // Editable
    private String cta_texto;   // Editable
    private String cta_url;     // Del Gestor, NO editable
    private LocalDateTime fecha_creacion;
    private LocalDateTime fecha_actualizacion;
    
    @OneToOne(mappedBy = "campana_mailing")
    private MetricaCampana metricas;
}
