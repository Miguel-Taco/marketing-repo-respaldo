import axios from 'axios';
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
        const response = await axios.get<{
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
        const response = await axios.post<PlantillaCampana>(BASE_URL, data);
        return response.data;
    },

    // Editar plantilla
    update: async (id: number, data: CrearPlantillaRequest) => {
        const response = await axios.put<PlantillaCampana>(`${BASE_URL}/${id}`, data);
        return response.data;
    },

    // Eliminar plantilla
    delete: async (id: number) => {
        await axios.delete(`${BASE_URL}/${id}`);
    },
};
