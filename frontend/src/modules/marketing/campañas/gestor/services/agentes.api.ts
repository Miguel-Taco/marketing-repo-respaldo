import axios from 'axios';
import { Agente } from '../types/agente.types';

const API_BASE_URL = 'http://localhost:8080/api/v1/campanas/agentes';

export const agentesApi = {
    // GET /api/v1/campanas/agentes
    getAllActive: async (): Promise<Agente[]> => {
        const response = await axios.get<Agente[]>(API_BASE_URL);
        return response.data;
    }
};
