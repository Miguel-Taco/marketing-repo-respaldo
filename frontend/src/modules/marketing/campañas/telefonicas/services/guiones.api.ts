import axios from 'axios';
import { GuionDTO, CreateGuionRequest } from '../types/guiones.types';

const API_BASE_URL = '/api/v1';

export const guionesApi = {
    /**
     * POST /api/v1/guiones
     * Crea un nuevo guión estructurado
     */
    createGuion: async (data: CreateGuionRequest): Promise<GuionDTO> => {
        const response = await axios.post(`${API_BASE_URL}/guiones`, data);
        return response.data.data;
    },

    /**
     * PUT /api/v1/guiones/{id}
     * Actualiza un guión estructurado
     */
    updateGuion: async (id: number, data: CreateGuionRequest): Promise<GuionDTO> => {
        const response = await axios.put(`${API_BASE_URL}/guiones/${id}`, data);
        return response.data.data;
    },

    /**
     * GET /api/v1/guiones/{id}
     * Obtiene un guión por ID con todas sus secciones
     */
    getGuion: async (id: number): Promise<GuionDTO> => {
        const response = await axios.get(`${API_BASE_URL}/guiones/${id}`);
        return response.data.data;
    },

    /**
     * GET /api/v1/guiones-estructurados
     * Lista todos los guiones estructurados
     */
    listGuiones: async (): Promise<GuionDTO[]> => {
        const response = await axios.get(`${API_BASE_URL}/guiones-estructurados`);
        return response.data.data;
    },

    /**
     * GET /api/v1/guiones (legacy endpoint)
     * Obtiene todos los guiones (formato antiguo)
     */
    listarTodosLosGuiones: async () => {
        const response = await axios.get(`${API_BASE_URL}/guiones`);
        return response.data.data;
    },

    /**
     * POST /api/v1/campanias-telefonicas/{id}/vincular-guion
     * Vincula un guión estructurado a una campaña
     */
    vincularGuionACampana: async (idCampania: number, idGuion: number) => {
        const response = await axios.post(`${API_BASE_URL}/campanias-telefonicas/${idCampania}/vincular-guion`, {
            idGuion
        });
        return response.data.data;
    },

    /**
     * POST /campanias-telefonicas/{id}/guiones/general
     * Sube un archivo .md como guión general para una campaña
     */
    uploadGuionFile: async (idCampania: number, file: File): Promise<any> => {
        const formData = new FormData();
        formData.append('file', file);

        const response = await axios.post(
            `${API_BASE_URL}/campanias-telefonicas/${idCampania}/guiones/general`,
            formData,
            {
                headers: {
                    'Content-Type': 'multipart/form-data',
                },
            }
        );
        return response.data.data;
    },

    /**
     * DELETE /guiones/{id}
     * Elimina un guión estructurado
     */
    deleteGuion: async (id: number): Promise<void> => {
        await axios.delete(`${API_BASE_URL}/guiones/${id}`);
    },
};
