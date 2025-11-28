import { useState, useEffect } from 'react';
import { http } from '../services/api.client';
import { ApiResponse } from '../types/api.types';

export interface Ubicacion {
    id: string;
    nombre: string;
}

export const useUbigeo = () => {
    const [departamentos, setDepartamentos] = useState<Ubicacion[]>([]);
    const [provincias, setProvincias] = useState<Ubicacion[]>([]);
    const [distritos, setDistritos] = useState<Ubicacion[]>([]);
    const [loadingUbi, setLoadingUbi] = useState(false);

    // 1. Cargar Departamentos al iniciar
    useEffect(() => {
        const loadDeps = async () => {
            try {
                const res = await http.get<ApiResponse<Ubicacion[]>>('/ubigeo/departamentos');
                setDepartamentos(res.data);
            } catch (e) {
                console.error("Error cargando departamentos", e);
            }
        };
        loadDeps();
    }, []);

    // 2. Cargar Provincias (Cascada Nivel 1)
    const loadProvincias = async (depId: string) => {
        setProvincias([]);
        setDistritos([]);
        if (!depId) return;

        setLoadingUbi(true);
        try {
            const res = await http.get<ApiResponse<Ubicacion[]>>(`/ubigeo/provincias/${depId}`);
            setProvincias(res.data);
        } finally {
            setLoadingUbi(false);
        }
    };

    // 3. Cargar Distritos (Cascada Nivel 2)
    const loadDistritos = async (provId: string) => {
        setDistritos([]);
        if (!provId) return;

        setLoadingUbi(true);
        try {
            const res = await http.get<ApiResponse<Ubicacion[]>>(`/ubigeo/distritos/${provId}`);
            setDistritos(res.data);
        } finally {
            setLoadingUbi(false);
        }
    };

    return {
        departamentos,
        provincias,
        distritos,
        loadProvincias,
        loadDistritos,
        loadingUbi
    };
};
