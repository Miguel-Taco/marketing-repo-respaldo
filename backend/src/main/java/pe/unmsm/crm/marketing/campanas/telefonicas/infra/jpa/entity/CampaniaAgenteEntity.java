package pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "campania_agente", schema = "railway")
@IdClass(CampaniaAgentePK.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaniaAgenteEntity {

    @Id
    @Column(name = "id_campania")
    private Integer idCampania;

    @Id
    @Column(name = "id_agente")
    private Integer idAgente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_campania", insertable = false, updatable = false)
    private CampaniaTelefonicaEntity campania;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_agente", insertable = false, updatable = false)
    private AgenteMarketingEntity agente;
}
