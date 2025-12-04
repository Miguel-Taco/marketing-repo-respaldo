package pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cola_llamada", schema = "railway")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColaLlamadaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_campania", nullable = false)
    private Integer idCampania;

    @Column(name = "id_lead", nullable = false)
    private Long idLead;

    @Column(name = "prioridad_cola", nullable = false, length = 45)
    private String prioridadCola = "MEDIA";

    @Column(name = "estado_en_cola", nullable = false, length = 45)
    private String estadoEnCola = "PENDIENTE";

    @Column(name = "id_agente_actual")
    private Integer idAgenteActual;

    @Column(name = "fecha_programada")
    private java.time.LocalDateTime fechaProgramada;

    @Column(name = "fecha_ultima_llamada")
    private java.time.LocalDateTime fechaUltimaLlamada;

    @Column(name = "resultado_ultima_llamada")
    private String resultadoUltimaLlamada;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_campania", insertable = false, updatable = false)
    private CampaniaTelefonicaEntity campania;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_agente_actual", insertable = false, updatable = false)
    private AgenteMarketingEntity agenteActual;
}
