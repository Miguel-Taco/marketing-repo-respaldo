import { http } from "../../../../../shared/services/api.client";
import { Encuesta, CreateEncuestaRequest, CampanaSummary } from "../types";

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
    },
    archivar: (id: number) => {
        return http.put<void>(`${ENDPOINT}/${id}/archivar`, {});
    },
    getCampanas: (id: number) => {
        return http.get<CampanaSummary[]>(`${ENDPOINT}/${id}/campanas`);
    },
    getTendencia: (id: number) => {
        return http.get<any[]>(`/encuestas/respuestas/analytics/${id}/tendencia`);
    },
    getResumen: (id: number, rango: string) => {
        return http.get<any>(`/encuestas/respuestas/analytics/${id}/resumen?rango=${rango}`);
    },
    getIndicadores: (id: number, rango: string) => {
        return http.get<any[]>(`/encuestas/respuestas/analytics/${id}/indicadores?rango=${rango}`);
    }
};

