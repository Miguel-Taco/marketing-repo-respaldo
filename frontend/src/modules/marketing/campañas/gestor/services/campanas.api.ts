import axios from 'axios';
import { CampanaListResponse, CreateCampanaDTO, UpdateCampanaDTO } from '../types/campana.types';

const API_BASE_URL = 'http://localhost:8080/api/v1/campanas';

export const campanasApi = {
    // GET /api/v1/campanas - List campaigns with filters
    getAll: async (params?: {
        nombre?: string;
        estado?: string;
        canalEjecucion?: string;
        page?: number;
        size?: number;
    }): Promise<CampanaListResponse> => {
        const response = await axios.get<CampanaListResponse>(API_BASE_URL, { params });
        return response.data;
    },

    // POST /api/v1/campanas - Create new campaign
    create: async (data: CreateCampanaDTO) => {
        const response = await axios.post(API_BASE_URL, data);
        return response.data;
    },

    // PUT /api/v1/campanas/{id} - Update campaign
    update: async (id: number, data: UpdateCampanaDTO) => {
        const response = await axios.put(`${API_BASE_URL}/${id}`, data);
        return response.data;
    },

    // DELETE /api/v1/campanas/{id} - Delete campaign
    delete: async (id: number) => {
        await axios.delete(`${API_BASE_URL}/${id}`);
    },

    // POST /api/v1/campanas/{id}/programar
    programar: async (id: number, data: {
        fechaProgramadaInicio: string;
        fechaProgramadaFin: string;
        idAgente?: number;
        idSegmento?: number;
        idEncuesta?: number;
    }) => {
        const response = await axios.post(`${API_BASE_URL}/${id}/programar`, data);
        return response.data;
    },

    // POST /api/v1/campanas/{id}/pausar
    pausar: async (id: number, motivo?: string) => {
        const response = await axios.post(`${API_BASE_URL}/${id}/pausar`, { motivo });
        return response.data;
    },

    // POST /api/v1/campanas/{id}/reanudar
    reanudar: async (id: number) => {
        const response = await axios.post(`${API_BASE_URL}/${id}/reanudar`);
        return response.data;
    },

    // POST /api/v1/campanas/{id}/cancelar
    cancelar: async (id: number, motivo?: string) => {
        const response = await axios.post(`${API_BASE_URL}/${id}/cancelar`, { motivo });
        return response.data;
    },

    // GET /api/v1/campanas/{id} - Get campaign details
    getById: async (id: number) => {
        const response = await axios.get(`${API_BASE_URL}/${id}`);
        return response.data;
    },

    // GET /api/v1/campanas/historial - Get campaign history
    getHistorial: async (params?: {
        idCampana?: number;
        tipoAccion?: string;
        fechaDesde?: string;
        fechaHasta?: string;
        page?: number;
        size?: number;
    }) => {
        const response = await axios.get(`${API_BASE_URL}/historial`, { params });
        return response.data;
    },

    // POST /api/v1/campanas/{id}/reprogramar
    reprogramar: async (id: number, data: {
        nuevaFechaInicio: string;
        nuevaFechaFin: string;
    }) => {
        const response = await axios.post(`${API_BASE_URL}/${id}/reprogramar`, data);
        return response.data;
    },

    // POST /api/v1/campanas/{id}/archivar
    archivar: async (id: number) => {
        const response = await axios.post(`${API_BASE_URL}/${id}/archivar`);
        return response.data;
    },

    // POST /api/v1/campanas/{id}/duplicar
    duplicar: async (id: number) => {
        const response = await axios.post(`${API_BASE_URL}/${id}/duplicar`);
        return response.data;
    },
};
