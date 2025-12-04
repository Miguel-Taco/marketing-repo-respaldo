import { apiClient } from '../../../../../shared/services/api.client';
import { PlantillaCampana, CrearPlantillaRequest } from '../types/plantilla.types';

const BASE_URL = 'http://localhost:8080/api/v1/plantillas';

export const plantillasApi = {
    // Listar plantillas con filtros y paginación
    getAll: async (params?: {
        nombre?: string;
        canalEjecucion?: string;
        page?: number;
        size?: number;
    }) => {
        const response = await apiClient.get<{
            content: PlantillaCampana[];
            page: number;
            size: number;
            total_elements: number;
            total_pages: number;
        }>(BASE_URL, { params });
        return response.data;
    },

    // Crear plantilla
    create: async (data: CrearPlantillaRequest) => {
        const response = await apiClient.post<PlantillaCampana>(BASE_URL, data);
        return response.data;
    },

    // Editar plantilla
    update: async (id: number, data: CrearPlantillaRequest) => {
        const response = await apiClient.put<PlantillaCampana>(`${BASE_URL}/${id}`, data);
        return response.data;
    },

    // Eliminar plantilla
    delete: async (id: number) => {
        await apiClient.delete(`${BASE_URL}/${id}`);
    },
};
