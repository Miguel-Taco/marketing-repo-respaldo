package pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "llamada", schema = "railway")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LlamadaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "inicio", nullable = false)
    private LocalDateTime inicio;

    @Column(name = "fin", nullable = false)
    private LocalDateTime fin;

    @Column(name = "id_agente", nullable = false)
    private Integer idAgente;

    @Column(name = "id_lead", nullable = false)
    private Long idLead;

    @Column(name = "id_resultado")
    private Integer idResultado;

    @Column(name = "id_campania", nullable = false)
    private Integer idCampania;

    @Column(name = "notas", columnDefinition = "MEDIUMTEXT")
    private String notas;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_agente", insertable = false, updatable = false)
    private AgenteMarketingEntity agente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_resultado", insertable = false, updatable = false)
    private ResultadoLlamadaEntity resultado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_campania", insertable = false, updatable = false)
    private CampaniaTelefonicaEntity campania;
}
