package pe.unmsm.crm.marketing.shared.logging;

/**
 * Enum que define los módulos funcionales del sistema para propósitos de
 * auditoría.
 */
public enum ModuloLog {
    /** Módulo de gestión de Leads (Prospectos) */
    LEADS,

    /** Módulo de Segmentación de clientes */
    SEGMENTOS,

    /** Módulo de gestión de Campañas (configuración general) */
    CAMPANIAS_GESTOR,

    /** Módulo de ejecución de Campañas de Email Marketing */
    CAMPANIAS_MAILING,

    /** Módulo de ejecución de Campañas Telefónicas */
    CAMPANIAS_TELEFONICAS,

    /** Módulo de Encuestas y satisfacción */
    ENCUESTAS
}
