package pe.unmsm.crm.marketing.shared.logging;

/**
 * Enum que define las acciones de negocio importantes para propósitos de
 * auditoría.
 */
public enum AccionLog {
    /** Creación de una nueva entidad */
    CREAR,

    /** Actualización de datos de una entidad existente */
    ACTUALIZAR,

    /** Eliminación (lógica o física) de una entidad */
    ELIMINAR,

    /** Cambio de estado en el flujo de vida de una entidad */
    CAMBIAR_ESTADO,

    /** Derivación de un lead o cliente al área de ventas */
    DERIVAR_A_VENTAS,

    /** Registro de una interacción telefónica */
    REGISTRAR_LLAMADA,

    /** Envío de una campaña de correo */
    ENVIAR_MAILING,

    /** Envío de una encuesta a un cliente */
    ENVIAR_ENCUESTA
}
