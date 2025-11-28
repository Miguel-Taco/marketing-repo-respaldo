import axios from 'axios';
import { Segmento, SegmentoResumen } from '../../../../../shared/types/segmento.types';

const API_BASE_URL = 'http://localhost:8080/api/v1/internal/segmentos';

export const segmentosApi = {
    // GET /api/v1/internal/segmentos/activos - List only active segments
    getActivos: async (): Promise<Segmento[]> => {
        const response = await axios.get<Segmento[]>(`${API_BASE_URL}/activos`);
        return response.data;
    },

    // GET /api/v1/internal/segmentos/{id} - Get specific segment
    getById: async (id: number): Promise<Segmento> => {
        const response = await axios.get<Segmento>(`${API_BASE_URL}/${id}`);
        return response.data;
    },

    // GET /api/v1/internal/segmentos/{id}/resumen - Get segment summary
    getResumen: async (id: number): Promise<SegmentoResumen> => {
        const response = await axios.get<SegmentoResumen>(`${API_BASE_URL}/${id}/resumen`);
        return response.data;
    },

    // GET /api/v1/internal/segmentos/{id}/miembros - Get segment member IDs
    getMiembros: async (id: number): Promise<number[]> => {
        const response = await axios.get<number[]>(`${API_BASE_URL}/${id}/miembros`);
        return response.data;
    },
};
