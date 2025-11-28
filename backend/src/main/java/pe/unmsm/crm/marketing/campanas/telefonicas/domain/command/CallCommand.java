package pe.unmsm.crm.marketing.campanas.telefonicas.domain.command;

import pe.unmsm.crm.marketing.campanas.telefonicas.domain.model.CallContext;

/**
 * Comando que representa una operación de telemarketing que puede encolarse.
 */
public interface CallCommand {
    /**
     * Identificador único del comando (tracking).
     */
    String id();

    /**
     * Contexto asociado a la llamada/operación.
     */
    CallContext context();

    /**
     * Lógica de ejecución del comando.
     */
    void execute();
}

