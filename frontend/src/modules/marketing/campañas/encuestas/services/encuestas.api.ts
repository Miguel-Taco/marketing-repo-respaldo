import { http } from "../../../../../shared/services/api.client";
import { Encuesta, CreateEncuestaRequest } from "../types";

const ENDPOINT = '/marketing/campanas/encuestas';

export const encuestasApi = {
    getAll: () => {
        return http.get<Encuesta[]>(ENDPOINT);
    },
    getById: (id: number) => {
        return http.get<Encuesta>(`${ENDPOINT}/${id}`);
    },
    create: (data: CreateEncuestaRequest) => {
        return http.post<Encuesta>(ENDPOINT, data);
    },
    update: (id: number, data: CreateEncuestaRequest) => {
        return http.put<Encuesta>(`${ENDPOINT}/${id}`, data);
    }
};

