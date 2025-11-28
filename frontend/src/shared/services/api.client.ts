import axios, { AxiosInstance, AxiosRequestConfig, AxiosError } from 'axios';

const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1';

export const apiClient: AxiosInstance = axios.create({
    baseURL: BASE_URL,
    headers: { 'Content-Type': 'application/json' },
    timeout: 30000, // 30 segundos
});

// Interceptor de respuesta mejorado
apiClient.interceptors.response.use(
    (response) => response,
    (error: AxiosError<{ message?: string }>) => {
        // Manejo específico de códigos HTTP
        const status = error.response?.status;
        const message = error.response?.data?.message || 'Error de conexión con el servidor';

        if (status === 401) {
            console.error('Sesión expirada');
            // Aquí podrías redirigir a login si implementas autenticación
        } else if (status === 404) {
            console.error('Recurso no encontrado:', message);
        } else if (status && status >= 500) {
            console.error('Error del servidor:', message);
        }

        return Promise.reject({
            status,
            message,
            originalError: error
        });
    }
);

export const http = {
    get: <T>(url: string, config?: AxiosRequestConfig) =>
        apiClient.get<T>(url, config).then(res => res.data),

    post: <T>(url: string, body: unknown, config?: AxiosRequestConfig) =>
        apiClient.post<T>(url, body, config).then(res => res.data),

    put: <T>(url: string, body: unknown, config?: AxiosRequestConfig) =>
        apiClient.put<T>(url, body, config).then(res => res.data),

    patch: <T>(url: string, body: unknown, config?: AxiosRequestConfig) =>
        apiClient.patch<T>(url, body, config).then(res => res.data),

    delete: <T>(url: string, config?: AxiosRequestConfig) =>
        apiClient.delete<T>(url, config).then(res => res.data),
};
