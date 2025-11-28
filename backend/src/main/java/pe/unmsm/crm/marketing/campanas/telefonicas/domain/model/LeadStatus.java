package pe.unmsm.crm.marketing.campanas.telefonicas.domain.model;

/**
 * Estado de un lead dentro de la campaña telefónica.
 */
public enum LeadStatus {
    PENDIENTE,
    EN_LLAMADA,
    REAGENDADO,
    CERRADO_EXITOSO,
    DESCARTADO
}

