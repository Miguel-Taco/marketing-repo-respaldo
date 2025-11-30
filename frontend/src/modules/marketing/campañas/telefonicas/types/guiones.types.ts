export interface SeccionGuionDTO {
    id?: number;
    tipoSeccion: 'INTRO' | 'DIAGNOSTICO' | 'OBJECIONES' | 'CIERRE' | 'POST_LLAMADA';
    contenido: string;
    orden: number;
}

export interface GuionDTO {
    id?: number;
    nombre: string;
    objetivo: string;
    tipo: string; // RENOVACION, VENTA_NUEVA, RECUPERO, etc.
    notasInternas?: string;
    estado?: string;
    secciones: SeccionGuionDTO[];
}

export interface CreateGuionRequest {
    idCampania?: number; // Optional: campaign context
    nombre: string;
    objetivo: string;
    tipo: string;
    notasInternas?: string;
    secciones: SeccionGuionDTO[];
}

export const TIPOS_SECCION = {
    INTRO: { value: 'INTRO', label: 'Introducción / Saludo', orden: 1 },
    DIAGNOSTICO: { value: 'DIAGNOSTICO', label: 'Preguntas de Diagnóstico', orden: 2 },
    OBJECIONES: { value: 'OBJECIONES', label: 'Manejo de Objeciones', orden: 3 },
    CIERRE: { value: 'CIERRE', label: 'Cierre / Call to Action', orden: 4 },
    POST_LLAMADA: { value: 'POST_LLAMADA', label: 'Pasos Post-Llamada', orden: 5 },
} as const;

export const TIPOS_LLAMADA = [
    { value: 'RENOVACION', label: 'Renovación' },
    { value: 'VENTA_NUEVA', label: 'Venta Nueva' },
    { value: 'RECUPERO', label: 'Recupero' },
    { value: 'RETENCION', label: 'Retención' },
    { value: 'ENCUESTA', label: 'Encuesta' },
    { value: 'SEGUIMIENTO', label: 'Seguimiento' },
] as const;
