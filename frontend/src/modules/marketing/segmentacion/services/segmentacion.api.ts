import { http } from "../../../../shared/services/api.client";
import {
    Segmento,
    CreateSegmentoDTO,
    SegmentoListResponse,
    SegmentoDetailResponse,
    SegmentoPreviewResponse
} from "../types/segmentacion.types";

const ENDPOINT = '/segmentos'; // Prefijo base del módulo

export const segmentacionApi = {
    // 1. Listar Segmentos (con filtros opcionales)
    getAll: (params?: { estado?: string; tipoAudiencia?: string; includeDeleted?: boolean }) => {
        const queryParams = new URLSearchParams();
        if (params?.estado) queryParams.append('estado', params.estado);
        if (params?.tipoAudiencia) queryParams.append('tipoAudiencia', params.tipoAudiencia);
        if (params?.includeDeleted) queryParams.append('includeDeleted', 'true');

        const query = queryParams.toString();
        // Backend devuelve array directamente, no { data: [...] }
        return http.get<Segmento[]>(`${ENDPOINT}${query ? `?${query}` : ''}`);
    },

    // 2. Obtener un Segmento por ID
    getById: (id: number): Promise<Segmento> => {
        return http.get<Segmento>(`${ENDPOINT}/${id}`);
    },

    // 3. Crear Segmento
    create: (data: CreateSegmentoDTO): Promise<Segmento> => {
        return http.post<Segmento>(`${ENDPOINT}`, data);
    },

    // 4. Actualizar Segmento
    update: (id: number, data: Partial<CreateSegmentoDTO>): Promise<Segmento> => {
        return http.patch<Segmento>(`${ENDPOINT}/${id}`, data);
    },

    // 4b. Actualización rápida (solo campos básicos, sin rematerialización)
    quickUpdate: (id: number, data: { nombre: string; descripcion: string; estado: string }): Promise<Segmento> => {
        return http.patch<Segmento>(`${ENDPOINT}/${id}/quick`, data);
    },

    // 5. Eliminar Segmento (soft delete)
    delete: (id: number) => {
        return http.delete<void>(`${ENDPOINT}/${id}`);
    },

    // 6. Preview (Conteo de registros)
    preview: (id: number) => {
        return http.post<SegmentoPreviewResponse>(`${ENDPOINT}/${id}/preview`, {});
    },

    // 7. Materializar Segmento
    materializar: (id: number) => {
        return http.post<void>(`${ENDPOINT}/${id}/materializar`, {});
    },

    // 8. Preview Temporal (sin guardar)
    previewTemporal: (data: Partial<CreateSegmentoDTO>) => {
        return http.post<{ data: { count: number; leadIds: number[] } }>(`${ENDPOINT}/preview-temp`, data);
    },

    // 9. Exportar Segmento
    export: async (id: number, format: 'csv' | 'excel' = 'csv') => {
        // TODO: Implementar endpoint de exportación en el backend
        console.log(`Exportando segmento ${id} en formato ${format}`);
        alert(`Funcionalidad de exportación en desarrollo.\nSegmento ID: ${id}\nFormato: ${format}`);
    }
};
