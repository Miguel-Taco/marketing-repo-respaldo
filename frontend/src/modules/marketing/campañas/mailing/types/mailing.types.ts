export interface CampanaMailing {
    id: number;
    idCampanaGestion: number;
    idSegmento: number;
    idEncuesta: number;
    idAgenteAsignado: number;
    idEstado: number;
    estadoNombre: string;
    prioridad: string;
    nombre: string;
    descripcion: string;
    tematica: string;
    fechaInicio: string;
    fechaFin: string;
    asunto: string | null;
    cuerpo: string | null;
    ctaTexto: string | null;
    ctaUrl: string;
    nombreEncuesta: string;
    fechaCreacion: string;
    fechaActualizacion: string;
}

export interface MetricasMailing {
    id: number;
    idCampanaMailingId: number;
    enviados: number;
    entregados: number;
    aperturas: number;
    clics: number;
    rebotes: number;
    bajas: number;
    tasaApertura: number;
    tasaClics: number;
    tasaBajas: number;
}

export interface ActualizarContenidoRequest {
    asunto: string;
    cuerpo: string;
    ctaTexto: string;
}

// Estados de campaña
export enum EstadoCampana {
    PENDIENTE = 1,
    LISTO = 2,
    ENVIADO = 3,
    VENCIDO = 4,
    FINALIZADO = 5,
    CANCELADO = 6
}

export const ESTADO_LABELS: Record<number, string> = {
    1: 'Pendiente',
    2: 'Listo',
    3: 'Enviado',
    4: 'Vencido',
    5: 'Finalizado',
    6: 'Cancelado'
};

export const ESTADO_COLORS: Record<number, 'default' | 'info' | 'success' | 'warning' | 'danger'> = {
    1: 'default',   // PENDIENTE - gris
    2: 'info',      // LISTO - azul
    3: 'success',   // ENVIADO - verde
    4: 'warning',   // VENCIDO - amarillo
    5: 'success',   // FINALIZADO - verde
    6: 'danger'     // CANCELADO - rojo
};

export const PRIORIDAD_COLORS: Record<string, string> = {
    'Alta': 'bg-red-100 text-red-700',
    'Media': 'bg-yellow-100 text-yellow-700',
    'Baja': 'bg-green-100 text-green-700'
};