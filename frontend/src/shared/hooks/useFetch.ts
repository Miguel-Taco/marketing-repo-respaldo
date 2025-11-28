import { useState, useEffect, useCallback } from 'react';
import { http } from '../services/api.client';
import { ApiResponse } from '../types/api.types';

interface UseFetchOptions {
    params?: Record<string, any>;
    autoFetch?: boolean; // Por defecto true
}

export function useFetch<T>(url: string, options: UseFetchOptions = {}) {
    const { params, autoFetch = true } = options;
    const [data, setData] = useState<T | null>(null);
    const [loading, setLoading] = useState<boolean>(autoFetch);
    const [error, setError] = useState<string | null>(null);

    const fetchData = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const response = await http.get<ApiResponse<T>>(url, { params });
            setData(response.data);
        } catch (err: any) {
            setError(err.message || 'Error desconocido');
        } finally {
            setLoading(false);
        }
    }, [url, JSON.stringify(params)]); // Dependencia estable

    useEffect(() => {
        if (autoFetch) {
            fetchData();
        }
    }, [autoFetch, fetchData]);

    return { data, loading, error, refetch: fetchData };
}
