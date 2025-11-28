package pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaniaAgentePK implements Serializable {

    private Integer idCampania;
    private Integer idAgente;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CampaniaAgentePK that = (CampaniaAgentePK) o;
        return Objects.equals(idCampania, that.idCampania) &&
                Objects.equals(idAgente, that.idAgente);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idCampania, idAgente);
    }
}
