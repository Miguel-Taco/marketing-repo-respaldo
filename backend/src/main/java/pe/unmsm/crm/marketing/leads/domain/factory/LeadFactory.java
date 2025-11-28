package pe.unmsm.crm.marketing.leads.domain.factory;

import pe.unmsm.crm.marketing.leads.domain.model.Lead;
import pe.unmsm.crm.marketing.leads.domain.enums.TipoFuente;

public interface LeadFactory {
    Lead convertirALead(Object origenStaging);

    boolean soporta(TipoFuente tipo);
}