package pe.unmsm.crm.marketing.campanas.telefonicas.domain.model;

/**
 * Resultado final o provisional de una llamada.
 */
public enum CallOutcome {
    CONTACTADO,
    NO_CONTESTA,
    BUZON_VOZ,
    OCUPADO,
    INTERESADO,
    VENTA,
    DERIVADO_VENTAS,
    REAGENDADO,
    NUMERO_INCORRECTO,
    RECHAZADO,
    OTRO
}

