package pe.unmsm.crm.marketing.leads.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;

@Entity
@DiscriminatorValue("WEB")
public class LeadWeb extends Lead {
    public LeadWeb() {
        super();
        // fuenteTipo is handled by DiscriminatorColumn
    }

    @Override
    public String getResumenOrigen() {
        return "Web Ref#" + this.idReferenciaOrigen;
    }
}