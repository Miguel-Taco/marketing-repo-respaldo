import { useState, useEffect, useCallback } from 'react';
import { campanasApi } from '../services/campanas.api';
import { CampanaListItem } from '../types/campana.types';

interface UseCampanasReturn {
    campanas: CampanaListItem[];
    loading: boolean;
    error: string | null;
    refresh: () => Promise<void>;
    setFilter: (key: string, value: any) => void;
    totalPages: number;
    totalElements: number;
    currentPage: number;
}

export const useCampanas = (): UseCampanasReturn => {
    const [campanas, setCampanas] = useState<CampanaListItem[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const [currentPage, setCurrentPage] = useState(0);

    const [filters, setFilters] = useState<{
        nombre?: string;
        estado?: string;
        prioridad?: string;
        canalEjecucion?: string;
        page: number;
        size: number;
    }>({
        page: 0,
        size: 10,
    });

    const fetchCampanas = useCallback(async () => {
        setLoading(true);
        setError(null);

        try {
            const data = await campanasApi.getAll(filters);
            setCampanas(data.content);
            setTotalPages(data.totalPages);
            setTotalElements(data.totalElements);
            setCurrentPage(data.page);
        } catch (err: any) {
            setError(err.message || 'Error al cargar campañas');
            console.error('Error fetching campanas:', err);
        } finally {
            setLoading(false);
        }
    }, [filters]);

    useEffect(() => {
        fetchCampanas();
    }, [fetchCampanas]);

    const setFilter = (key: string, value: any) => {
        setFilters(prev => ({
            ...prev,
            [key]: value,
            page: key === 'page' ? value : 0, // Reset to page 0 when changing filters
        }));
    };

    const refresh = async () => {
        await fetchCampanas();
    };

    return {
        campanas,
        loading,
        error,
        refresh,
        setFilter,
        totalPages,
        totalElements,
        currentPage,
    };
};
