package pe.unmsm.crm.marketing.campanas.telefonicas.domain.command;

/**
 * Bus para encolar y procesar comandos de llamadas.
 */
public interface CallCommandBus {
    /**
     * Encola un comando y devuelve su ID de tracking.
     */
    String enqueue(CallCommand command);

    /**
     * Procesa en el hilo actual los comandos pendientes (útil para ejecución inmediata).
     */
    void processPending();

    /**
     * Cantidad de comandos en cola.
     */
    int pendingSize();
}

