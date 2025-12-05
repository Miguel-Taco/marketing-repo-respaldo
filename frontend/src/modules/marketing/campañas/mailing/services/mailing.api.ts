import { http } from '../../../../../shared/services/api.client';
import { CampanaMailing, MetricasMailing, ActualizarContenidoRequest } from '../types/mailing.types';

const BASE_URL = '/mailing';

/**
 * API Client para Campañas de Mailing - VERSIÓN OPTIMIZADA
 * 
 * MEJORAS:
 * 1. Método para listar sin filtro (para cache local)
 * 2. Nuevo método para métricas en batch
 */
export const mailingApi = {
    /**
     * Listar campañas asignadas
     * 
     * @param estado - Opcional. Si no se especifica, retorna TODAS las campañas
     */
    listarCampanas: (estado?: string) => {
        const params: Record<string, any> = {};
        if (estado) {
            params.estado = estado;
        }
        return http.get<CampanaMailing[]>(`${BASE_URL}/campañas`, { params });
    },

    /**
     * Obtener detalle de una campaña
     */
    obtenerDetalle: (id: number) => {
        return http.get<CampanaMailing>(`${BASE_URL}/campañas/${id}`);
    },

    /**
     * Guardar borrador de contenido
     */
    guardarBorrador: (id: number, data: ActualizarContenidoRequest) => {
        return http.put<void>(`${BASE_URL}/campañas/${id}/preparacion`, data);
    },

    /**
     * Marcar campaña como lista para envío
     */
    marcarListo: (id: number) => {
        return http.put<void>(`${BASE_URL}/campañas/${id}/estado`, {});
    },

    /**
     * Obtener métricas de una campaña
     */
    obtenerMetricas: (id: number) => {
        return http.get<MetricasMailing>(`${BASE_URL}/campañas/${id}/metricas`);
    },

    /**
     * ✅ NUEVO: Obtener métricas de múltiples campañas en una sola llamada
     * 
     * Esto es mucho más eficiente que llamar a obtenerMetricas() para cada campaña
     * 
     * @param ids - Array de IDs de campañas
     * @returns Map con ID como clave y métricas como valor
     * 
     * @example
     * const metricas = await mailingApi.obtenerMetricasBatch([1, 2, 3]);
     * console.log(metricas[1]); // Métricas de campaña 1
     */
    obtenerMetricasBatch: (ids: number[]): Promise<Record<number, MetricasMailing>> => {
        if (ids.length === 0) {
            return Promise.resolve({});
        }
        
        const params = { ids: ids.join(',') };
        return http.get<Record<number, MetricasMailing>>(`${BASE_URL}/campañas/metricas/batch`, { params });
    },
};









