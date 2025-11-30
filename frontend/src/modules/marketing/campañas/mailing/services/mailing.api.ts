import { http } from '../../../../../shared/services/api.client';
import { CampanaMailing, MetricasMailing, ActualizarContenidoRequest } from '../types/mailing.types';

const BASE_URL = '/mailing/v1';

export const mailingApi = {
    // Listar campañas
    listarCampanas: (idAgente: number, estado?: string) => {
        const params: any = { idAgente };
        if (estado) params.estado = estado;
        return http.get<CampanaMailing[]>(`${BASE_URL}/campañas`, { params });
    },

    // Obtener detalle
    obtenerDetalle: (id: number) => {
        return http.get<CampanaMailing>(`${BASE_URL}/campañas/${id}`);
    },

    // Guardar borrador
    guardarBorrador: (id: number, data: ActualizarContenidoRequest) => {
        return http.put<void>(`${BASE_URL}/campañas/${id}/preparacion`, data);
    },

    // Marcar como listo
    marcarListo: (id: number) => {
        return http.put<void>(`${BASE_URL}/campañas/${id}/estado`, {});
    },

    // Obtener métricas
    obtenerMetricas: (id: number) => {
        return http.get<MetricasMailing>(`${BASE_URL}/campañas/${id}/metricas`);
    }
};