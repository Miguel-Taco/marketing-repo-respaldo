import { apiClient } from "../../../../../shared/services/api.client";
import type {
    CampaniaTelefonica,
    Contacto,
    Llamada,
    ResultadoLlamadaRequest,
    MetricasAgente,
    Guion,
    CreateCampaniaRequest
} from '../types';

const BASE_URL = '';

export const telemarketingApi = {
    // ========== CAMPAÑAS ==========

    /**
     * Obtiene las campañas asignadas a un agente
     */
    async getCampaniasAgente(idAgente: number): Promise<CampaniaTelefonica[]> {
        const response = await apiClient.get(`${BASE_URL}/agentes/${idAgente}/campanias-telefonicas`);
        return response.data.data;
    },

    /**
     * Crea una nueva campaña telefónica
     */
    async crearCampania(request: CreateCampaniaRequest): Promise<CampaniaTelefonica> {
        const response = await apiClient.post(`${BASE_URL}/campanias-telefonicas`, request);
        return response.data.data;
    },

    // ========== CONTACTOS ==========

    /**
     * Obtiene los contactos de una campaña
     */
    async getContactosCampania(idCampania: number): Promise<Contacto[]> {
        const response = await apiClient.get(`${BASE_URL}/campanias-telefonicas/${idCampania}/contactos`);
        return response.data.data;
    },

    // ========== COLA ==========

    /**
     * Obtiene la cola de llamadas pendientes
     */
    async getCola(idCampania: number): Promise<Contacto[]> {
        const response = await apiClient.get(`${BASE_URL}/campanias-telefonicas/${idCampania}/cola`);
        return response.data.data;
    },

    /**
     * Obtiene el siguiente contacto automáticamente
     */
    async getSiguienteContacto(idCampania: number, idAgente: number): Promise<Contacto> {
        const response = await apiClient.post(
            `${BASE_URL}/campanias-telefonicas/${idCampania}/cola/siguiente`,
            { idAgente }
        );
        return response.data.data;
    },

    /**
     * Toma un contacto específico de la cola
     */
    async tomarContacto(idCampania: number, idContacto: number, idAgente: number): Promise<Contacto> {
        const response = await apiClient.post(
            `${BASE_URL}/campanias-telefonicas/${idCampania}/contactos/${idContacto}/tomar`,
            { idAgente }
        );
        return response.data.data;
    },

    /**
     * Pausa la cola de llamadas
     */
    async pausarCola(idAgente: number, idCampania: number): Promise<void> {
        await apiClient.post(`${BASE_URL}/agentes/${idAgente}/campanias-telefonicas/${idCampania}/pausar-cola`);
    },

    /**
     * Reanuda la cola de llamadas
     */
    async reanudarCola(idAgente: number, idCampania: number): Promise<void> {
        await apiClient.post(`${BASE_URL}/agentes/${idAgente}/campanias-telefonicas/${idCampania}/reanudar-cola`);
    },

    // ========== LLAMADAS ==========

    /**
     * Obtiene el detalle de una llamada
     */
    async getLlamada(idLlamada: number): Promise<Llamada> {
        const response = await apiClient.get(`${BASE_URL}/llamadas/${idLlamada}`);
        return response.data.data;
    },

    /**
     * Registra el resultado de una llamada
     */
    async registrarResultado(
        idCampania: number,
        idAgente: number,
        request: ResultadoLlamadaRequest
    ): Promise<Llamada> {
        const response = await apiClient.post(
            `${BASE_URL}/campanias-telefonicas/${idCampania}/llamadas/resultado?idAgente=${idAgente}`,
            request
        );
        return response.data.data;
    },

    /**
     * Obtiene el historial de llamadas de una campaña
     */
    async getHistorialLlamadas(idCampania: number, idAgente: number): Promise<Llamada[]> {
        const response = await apiClient.get(
            `${BASE_URL}/campanias-telefonicas/${idCampania}/llamadas?idAgente=${idAgente}`
        );
        return response.data.data;
    },

    // ========== GUIONES ==========

    /**
     * Obtiene el guion de una campaña
     */
    async getGuion(idCampania: number): Promise<Guion> {
        const response = await apiClient.get(`${BASE_URL}/campanias-telefonicas/${idCampania}/guion`);
        return response.data.data;
    },

    /**
     * Obtiene todos los guiones disponibles
     */
    async getAllGuiones(): Promise<Guion[]> {
        const response = await apiClient.get(`${BASE_URL}/guiones`);
        return response.data.data;
    },

    // ========== MÉTRICAS ==========

    /**
     * Obtiene las métricas de un agente en una campaña
     */
    async getMetricasCampania(idCampania: number, idAgente: number): Promise<MetricasAgente> {
        const response = await apiClient.get(
            `${BASE_URL}/campanias-telefonicas/${idCampania}/metricas/agentes/${idAgente}`
        );
        return response.data.data;
    },

    /**
     * Obtiene las métricas generales de un agente
     */
    async getMetricasGenerales(idAgente: number): Promise<MetricasAgente> {
        const response = await apiClient.get(`${BASE_URL}/agentes/${idAgente}/metricas-campania`);
        return response.data.data;
    },

    // ========== SESION DE GUION (MEMENTO) ==========

    /**
     * Guarda el estado actual del guion de una llamada
     */
    async guardarSesionGuion(idLlamada: number, idAgente: number, payload: { pasoActual: number; respuestas: Record<string, string> }) {
        const response = await apiClient.post(
            `${BASE_URL}/llamadas/${idLlamada}/guion/sesion?idAgente=${idAgente}`,
            payload
        );
        return response.data.data;
    },

    /**
     * Recupera el estado del guion de una llamada
     */
    async obtenerSesionGuion(idLlamada: number, idAgente: number) {
        const response = await apiClient.get(`${BASE_URL}/llamadas/${idLlamada}/guion/sesion?idAgente=${idAgente}`);
        return response.data.data;
    }
};
