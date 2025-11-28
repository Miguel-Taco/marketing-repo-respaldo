package pe.unmsm.crm.marketing.campanas.gestor.domain.model;

/**
 * Resultado de la ejecución de una campaña reportado por los módulos
 * de Mailing o Llamadas vía webhook.
 */
public enum ResultadoEjecucion {
    EXITO, // La campaña se ejecutó completamente sin problemas
    ERROR, // Hubo un error crítico que impidió la ejecución
    PARCIAL // Se ejecutó parcialmente (ej: algunos correos fallaron)
}
