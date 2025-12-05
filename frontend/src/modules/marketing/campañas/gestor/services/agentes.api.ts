import { apiClient } from '../../../../../shared/services/api.client';
import { Agente } from '../types/agente.types';

const API_BASE_URL = `${import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1'}/campanas/agentes`;

export const agentesApi = {
    // GET /api/v1/campanas/agentes
    getAllActive: async (): Promise<Agente[]> => {
        const response = await apiClient.get<Agente[]>(API_BASE_URL);
        return response.data;
    }
};
