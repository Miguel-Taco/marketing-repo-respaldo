package pe.unmsm.crm.marketing.leads.domain.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.Embeddable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatosDemograficos {
    Integer edad;
    String genero;
    String nivelEducativo;
    String estadoCivil;
    String distrito; // Changed to String for DB compatibility

    // Constructor de conveniencia (3 parámetros)
    public DatosDemograficos(Integer edad, String genero, String distrito) {
        this.edad = edad;
        this.genero = genero;
        this.distrito = distrito;
        this.nivelEducativo = null;
        this.estadoCivil = null;
    }
}