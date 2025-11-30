package pe.unmsm.crm.marketing.campanas.gestor.infra.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "agente_marketing", schema = "railway")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgenteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_agente")
    private Integer idAgente;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "activo")
    private Boolean activo = true;
}
