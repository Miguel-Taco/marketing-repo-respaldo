import axios from 'axios';
import { Encuesta, EncuestaDisponible } from '../../../../../shared/types/encuesta.types';

const API_BASE_URL = 'http://localhost:8080/api/v1/marketing/campanas/encuestas';

export const encuestasApi = {
    // GET /api/v1/marketing/campanas/encuestas - List all surveys
    getAll: async (): Promise<Encuesta[]> => {
        const response = await axios.get<Encuesta[]>(API_BASE_URL);
        return response.data;
    },

    // GET /api/v1/marketing/campanas/encuestas/disponibles - List available (ACTIVA) surveys
    getDisponibles: async (): Promise<EncuestaDisponible[]> => {
        const response = await axios.get<EncuestaDisponible[]>(`${API_BASE_URL}/disponibles`);
        return response.data;
    },

    // GET /api/v1/marketing/campanas/encuestas/{id} - Get specific survey
    getById: async (id: number): Promise<Encuesta> => {
        const response = await axios.get<Encuesta>(`${API_BASE_URL}/${id}`);
        return response.data;
    },
};
