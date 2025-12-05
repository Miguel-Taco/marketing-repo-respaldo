// Types for Campañas Module

export type CampanaEstado =
    | 'Borrador'
    | 'Programada'
    | 'Vigente'
    | 'Pausada'
    | 'Finalizada'
    | 'Cancelada';

export type CanalEjecucion = 'Mailing' | 'Llamadas';

export type Prioridad = 'Baja' | 'Media' | 'Alta';

// Main entity (list view)
export interface CampanaListItem {
    idCampana: number;
    nombre: string;
    estado: CampanaEstado;
    prioridad: Prioridad;
    canalEjecucion: CanalEjecucion;
    fechaProgramadaInicio: string | null; // ISO 8601 or null for Borrador
    fechaProgramadaFin: string | null; // ISO 8601 or null for Borrador
}

// Generic Page Response (matches backend PageResponse<T>)
export interface PageResponse<T> {
    content: T[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
    first: boolean;
    last: boolean;
}

// API Response type
export type CampanaListResponse = PageResponse<CampanaListItem>;

// DTO for creating campaign
export interface CreateCampanaDTO {
    nombre: string;
    tematica: string;
    descripcion?: string;
    prioridad: Prioridad;
    canalEjecucion: CanalEjecucion;
    idAgente?: number;
    idSegmento?: number;
    idEncuesta?: number;
    esArchivado?: boolean;
}

// DTO for updating campaign (editable fields only)
export interface UpdateCampanaDTO {
    nombre: string;
    tematica: string;
    descripcion?: string;
    prioridad: Prioridad;
    canalEjecucion: CanalEjecucion;
    idAgente?: number;
    idSegmento?: number;
    idEncuesta?: number;
}

// ============================================
// Tipos para Modal de Detalles
// ============================================

/**
 * Enum de estados de campaña (mapea a EstadoCampanaEnum.java)
 */
export enum EstadoCampanaEnum {
    BORRADOR = 'BORRADOR',
    PROGRAMADA = 'PROGRAMADA',
    VIGENTE = 'VIGENTE',
    PAUSADA = 'PAUSADA',
    FINALIZADA = 'FINALIZADA',
    CANCELADA = 'CANCELADA'
}

/**
 * Detalle completo de campaña (mapea a CampanaDetalleResponse.java)
 */
export interface CampanaDetalle {
    idCampana: number;
    nombre: string;
    tematica: string;
    descripcion: string;
    estado: EstadoCampanaEnum;
    prioridad: Prioridad;
    canalEjecucion: CanalEjecucion;
    fechaProgramadaInicio: string | null;
    fechaProgramadaFin: string | null;
    idPlantilla: number | null;
    idAgente: number | null;
    nombreAgente?: string;
    idSegmento: number | null;
    idEncuesta: number | null;
    fechaCreacion: string;
    fechaModificacion: string;
    esArchivado: boolean;
}

/**
 * Tipos de acción en historial (mapea a TipoAccion.java)
 */
export enum TipoAccion {
    CREACION = 'CREACION',
    EDICION = 'EDICION',
    PROGRAMACION = 'PROGRAMACION',
    REPROGRAMACION = 'REPROGRAMACION',
    ACTIVACION = 'ACTIVACION',
    PAUSA = 'PAUSA',
    REANUDACION = 'REANUDACION',
    CANCELACION = 'CANCELACION',
    FINALIZACION = 'FINALIZACION',
    ARCHIVO = 'ARCHIVO',
    DUPLICACION = 'DUPLICACION',
    ERROR_EJECUCION = 'ERROR_EJECUCION'
}

/**
 * Item de historial (mapea a HistorialItemResponse.java)
 */
export interface HistorialItem {
    idHistorial: number;
    idCampana: number;
    fechaAccion: string; // ISO 8601
    tipoAccion: TipoAccion;
    usuarioResponsable: string;
    descripcionDetalle: string | null;
}
