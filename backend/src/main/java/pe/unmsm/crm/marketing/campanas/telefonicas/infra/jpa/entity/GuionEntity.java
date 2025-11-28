package pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "guion", schema = "railway")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "contenido", columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "activo")
    private Boolean activo = true;
}
