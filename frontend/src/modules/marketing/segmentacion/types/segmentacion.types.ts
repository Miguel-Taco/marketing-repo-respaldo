import { ApiResponse } from "../../../../shared/types/api.types";

// Enums
export type SegmentoEstado = 'ACTIVO' | 'INACTIVO' | 'ELIMINADO';
export type TipoAudiencia = 'LEAD' | 'CLIENTE' | 'MIXTO';
export type TipoRegla = 'SIMPLE' | 'AND' | 'OR';

// Regla de Segmentación (Estructura recursiva)
export interface ReglaDto {
    tipo: TipoRegla;

    // Para SIMPLE
    idFiltro?: number;
    campo?: string;
    operador?: string;
    valorTexto?: string;

    // Para AND/OR
    reglas?: ReglaDto[];
}

// Entidad Principal - Segmento
export interface Segmento {
    id: number;
    nombre: string;
    descripcion?: string;
    tipoAudiencia: TipoAudiencia;
    estado: SegmentoEstado;
    fechaCreacion: string;
    fechaActualizacion: string;
    cantidadMiembros?: number;
    reglaPrincipal?: ReglaDto;
}

// DTO para Crear/Actualizar Segmento
export interface CreateSegmentoDTO {
    nombre: string;
    descripcion?: string;
    tipoAudiencia: TipoAudiencia;
    estado?: SegmentoEstado;
    reglaPrincipal?: ReglaDto;
}

// Respuestas de API
export type SegmentoListResponse = ApiResponse<Segmento[]>;
export type SegmentoDetailResponse = ApiResponse<Segmento>;
export type SegmentoPreviewResponse = ApiResponse<number>;

// Alias para compatibilidad
export type ReglaSegmento = ReglaDto;
