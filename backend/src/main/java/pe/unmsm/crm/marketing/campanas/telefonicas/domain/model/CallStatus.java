package pe.unmsm.crm.marketing.campanas.telefonicas.domain.model;

/**
 * Ciclo de vida de una llamada dentro de la campaña telefónica.
 */
public enum CallStatus {
    PENDIENTE,
    EN_LLAMADA,
    REAGENDADO,
    CERRADO,
    CANCELADO
}

