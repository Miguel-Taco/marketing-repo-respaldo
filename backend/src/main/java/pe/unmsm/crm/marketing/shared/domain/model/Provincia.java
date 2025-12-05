package pe.unmsm.crm.marketing.shared.domain.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "provincias", indexes = {
        @Index(name = "idx_provincia_nombre", columnList = "nombre")
})
@Data
public class Provincia {
    @Id
    @Column(name = "id_provincia")
    private String id;

    private String nombre;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_departamento")
    private Departamento departamento;

    public String getDepartamentoId() {
        return departamento != null ? departamento.getId() : null;
    }
}
