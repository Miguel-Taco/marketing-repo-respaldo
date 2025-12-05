package pe.unmsm.crm.marketing.leads.domain.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.Embeddable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatosDemograficos {
    Integer edad;
    String genero;
    String nivelEducativo;
    String estadoCivil;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "distrito_id")
    private pe.unmsm.crm.marketing.shared.domain.model.Distrito distrito;

    // Constructor de conveniencia (3 parámetros)
    public DatosDemograficos(Integer edad, String genero,
            pe.unmsm.crm.marketing.shared.domain.model.Distrito distrito) {
        this.edad = edad;
        this.genero = genero;
        this.distrito = distrito;
        this.nivelEducativo = null;
        this.estadoCivil = null;
    }

    public String getDistritoId() {
        return distrito != null ? distrito.getId() : null;
    }
}