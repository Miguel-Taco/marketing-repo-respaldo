import axios from 'axios';
import { LoginRequest, LoginResponse, UserInfo } from '../types/auth.types';

const ENV_API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1';
const API_BASE_URL = ENV_API_URL.replace(/\/v1\/?$/, ''); // Removes /v1 suffix to get base /api
const API_URL = `${API_BASE_URL}/auth`;

// Crear instancia de axios con configuración base
const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Interceptor para agregar el token a las peticiones
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('jwt_token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Interceptor para manejar errores de autenticación (401)
api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response && error.response.status === 401) {
            // Si el token expiró o es inválido, limpiar localStorage y redirigir a login
            localStorage.removeItem('jwt_token');
            localStorage.removeItem('user_info');
            if (window.location.pathname !== '/login') {
                window.location.href = '/login';
            }
        }
        return Promise.reject(error);
    }
);

export const authApi = {
    login: async (credentials: LoginRequest): Promise<LoginResponse> => {
        const response = await axios.post<LoginResponse>(`${API_URL}/login`, credentials);
        return response.data;
    },

    getMe: async (): Promise<UserInfo> => {
        const response = await api.get<UserInfo>('/auth/me');
        return response.data;
    },

    logout: async (): Promise<void> => {
        await api.post('/auth/logout');
    }
};

export default api;
