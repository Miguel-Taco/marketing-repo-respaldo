import { ApiResponse, PaginatedResponse } from "../../../../shared/types/api.types";

// Enums (Espejo de Java)
export type LeadState = 'NUEVO' | 'CALIFICADO' | 'DESCARTADO';
export type LeadSourceType = 'WEB' | 'IMPORTACION';

// Value Objects
export interface DatosContacto {
    email: string;
    telefono: string;
    distritoId?: string;
}

export interface DatosDemograficos {
    edad?: number;
    genero?: string;
    nivelEducativo?: string;
    estadoCivil?: string;
    distrito?: string;
    distritoNombre?: string;
    provinciaNombre?: string;
    departamentoNombre?: string;
}

export interface TrackingUTM {
    source?: string;
    medium?: string;
    campaign?: string;
    term?: string;
    content?: string;
}

export interface HistorialEstado {
    fecha: string;
    estado: string;
    motivo?: string;
}

export interface HistorialEntry {
    fecha: string;
    estado: LeadState;
    motivo: string;
    usuario?: string;
}

// Entidad Principal (Lo que devuelve el Backend en GET)
export interface Lead {
    id: number;
    nombreCompleto: string;
    estado: LeadState;
    fechaCreacion: string; // Viene como String ISO desde el backend
    contacto: DatosContacto;
    demograficos?: DatosDemograficos;
    tracking?: TrackingUTM;
    fuenteTipo?: LeadSourceType;
    envioFormularioId?: number;
    registroImportadoId?: number;
    historial?: HistorialEntry[];
}

// DTO para Crear (Lo que enviamos en POST)
export interface CreateLeadDTO {
    nombreCompleto: string;
    origen: string;
    contacto: {
        email: string;
        telefono: string;
        distritoId: string;
    };
    demograficos?: {
        edad?: number;
        genero?: string;
    };
    tracking?: TrackingUTM;
}

// DTO para Cambiar Estado (PATCH)
export interface ChangeStatusDTO {
    nuevoEstado: LeadState;
    motivo?: string;
}

// Respuestas de API específicas
export type LeadListResponse = ApiResponse<PaginatedResponse<Lead>>;
export type LeadDetailResponse = ApiResponse<Lead>;

export interface LoteImportacion {
    id: number;
    nombreArchivo: string;
    totalRegistros: number;
    exitosos: number;
    rechazados: number;
    duplicados?: number;
    conErrores?: number;
    createdAt: string;
    estadoCalculado: 'COMPLETADO' | 'CON_ERRORES' | 'EN_PROCESO' | 'VACIO';
}

export interface LeadReportFilterDTO {
    fechaInicio?: string;
    fechaFin?: string;
    estado?: LeadState;
    fuenteTipo?: LeadSourceType;
    search?: string;
    edadMin?: number;
    edadMax?: number;
    genero?: string;
    distrito?: string;
}

