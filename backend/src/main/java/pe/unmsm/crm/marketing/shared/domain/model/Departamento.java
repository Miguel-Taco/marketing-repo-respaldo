package pe.unmsm.crm.marketing.shared.domain.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "departamentos", indexes = {
        @Index(name = "idx_departamento_nombre", columnList = "nombre")
})
@Data
public class Departamento {
    @Id
    @Column(name = "id_departamento")
    private String id;
    private String nombre;
}
