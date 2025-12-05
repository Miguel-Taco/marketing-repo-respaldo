package pe.unmsm.crm.marketing.shared.domain.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "distritos", indexes = {
        @Index(name = "idx_distrito_nombre", columnList = "nombre")
})
@Data
public class Distrito {
    @Id
    @Column(name = "id_distrito")
    private String id;

    private String nombre;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_provincia")
    private Provincia provincia;

    public String getProvinciaId() {
        return provincia != null ? provincia.getId() : null;
    }
}
