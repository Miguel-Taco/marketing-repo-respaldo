package pe.unmsm.crm.marketing.leads.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;

@Entity
@DiscriminatorValue("IMPORTACION")
public class LeadImportado extends Lead {
    public LeadImportado() {
        super();
        // fuenteTipo is handled by DiscriminatorColumn
    }

    @Override
    public String getResumenOrigen() {
        return "Importación Ref#" + this.idReferenciaOrigen;
    }
}