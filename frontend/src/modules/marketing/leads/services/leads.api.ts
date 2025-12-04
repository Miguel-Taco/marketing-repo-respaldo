import { http, apiClient } from "../../../../shared/services/api.client";
import { exportToExcel, generateExportFilename } from "../../../../shared/utils/exportUtils";
import { ApiResponse } from "../../../../shared/types/api.types";
import {
    Lead,
    CreateLeadDTO,
    ChangeStatusDTO,
    LeadListResponse,
    LeadDetailResponse,
    LoteImportacion
} from "../types/lead.types";

const ENDPOINT = '/leads'; // Prefijo base del módulo

export const leadsApi = {
    // 1. Listar Leads (Soporta filtros y paginación)
    getAll: (page = 0, size = 10, estado?: string, search?: string, fuenteTipo?: string) => {
        const params: any = { page, size };
        if (estado) params.estado = estado;
        if (search) params.search = search;
        if (fuenteTipo) params.fuenteTipo = fuenteTipo;

        // NOTA: Asumimos que el backend tiene implementado GET /api/v1/leads
        return http.get<LeadListResponse>(`${ENDPOINT}`, { params });
    },

    // 2. Obtener un Lead por ID
    getById: (id: number) => {
        return http.get<LeadDetailResponse>(`${ENDPOINT}/${id}`);
    },

    // 3. Crear Lead (Formulario Web)
    create: (data: CreateLeadDTO) => {
        // Apunta a LeadCaptureController
        return http.post(`${ENDPOINT}/capture/web`, data);
    },

    // 4. Actualizar Estado (Cualificación)
    updateStatus: (id: number, nuevoEstado: string, motivo?: string) => {
        return http.patch(`${ENDPOINT}/${id}/estado`, { nuevoEstado, motivo });
    },

    // 5. Subir Archivo (Multipart)
    uploadFile: (file: File) => {
        const formData = new FormData();
        formData.append('file', file);
        // Axios maneja el Content-Type multipart automáticamente
        return http.post<ApiResponse<LoteImportacion>>(`${ENDPOINT}/capture/import`, formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        });
    },

    // 6. Obtener Historial con Paginación
    getImportHistory: (page = 0, size = 10) => {
        return http.get<any>(`${ENDPOINT}/capture/import/history`, {
            params: { page, size }
        });
    },

    // 7. Obtener Estado de Importación
    getImportStatus: (id: number) => {
        return http.get<ApiResponse<LoteImportacion>>(`${ENDPOINT}/capture/import/${id}`);
    },

    // 8. Eliminar Lead
    delete: (id: number) => {
        return http.delete(`${ENDPOINT}/${id}`);
    },

    // 9. Eliminar Múltiples Leads
    deleteBatch: (ids: number[]) => {
        return http.delete(`${ENDPOINT}/batch`, { data: { ids } });
    },

    // 10. Cambiar Estado en Lote
    updateStatusBatch: (ids: number[], nuevoEstado: string, motivo?: string) => {
        return http.patch(`${ENDPOINT}/batch/estado`, { ids, nuevoEstado, motivo });
    },

    // 11. Exportar Todos los Leads (con filtros) - Using shared export utility
    exportAll: (estado?: string, search?: string, fuenteTipo?: string) => {
        const params: any = {};
        if (estado) params.estado = estado;
        if (search) params.search = search;
        if (fuenteTipo) params.fuenteTipo = fuenteTipo;

        const filename = generateExportFilename('leads');
        return exportToExcel(`${ENDPOINT}/export`, params, 'GET', undefined, filename);
    },

    // 12. Exportar Leads Seleccionados - Using shared export utility
    exportSelected: (ids: number[]) => {
        const filename = generateExportFilename('leads_selected');
        return exportToExcel(`${ENDPOINT}/export/selected`, undefined, 'POST', { ids }, filename);
    },

    // 13. Obtener Múltiples Leads por IDs (Batch)
    getLeadsBatch: (ids: number[]) => {
        return http.post<ApiResponse<Lead[]>>(`${ENDPOINT}/batch`, { ids });
    }
};

