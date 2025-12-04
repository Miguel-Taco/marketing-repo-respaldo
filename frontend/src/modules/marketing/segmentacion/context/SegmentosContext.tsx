import React, { createContext, useContext, useState, useCallback, ReactNode, useEffect } from 'react';
import { segmentacionApi } from '../services/segmentacion.api';
import { useAuth } from '../../../../shared/context/AuthContext';
import { Segmento } from '../types/segmentacion.types';

interface SegmentosContextType {
    allSegmentos: Segmento[]; // All segments cached
    segmentos: Segmento[]; // Filtered and paginated segments
    loading: boolean;
    error: string | null;
    totalPages: number;
    totalElements: number;
    currentPage: number;
    filters: {
        page: number;
        estado: string;
        search: string;
        tipoAudiencia: string;
    };
    setFilter: (key: string, value: any) => void;
    refresh: () => Promise<void>;
    addSegmento: (segmento: Segmento) => void;
    updateSegmento: (segmento: Segmento) => void;
    removeSegmento: (id: number) => void;
}

const SegmentosContext = createContext<SegmentosContextType | undefined>(undefined);

export const SegmentosProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
    const { isAuthenticated } = useAuth();
    const [allSegmentos, setAllSegmentos] = useState<Segmento[]>([]);
    const [segmentos, setSegmentos] = useState<Segmento[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [hasLoaded, setHasLoaded] = useState(false);

    // Metadatos de paginación
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const [currentPage, setCurrentPage] = useState(0);

    // Filtros
    const [filters, setFilters] = useState({
        page: 0,
        estado: '',
        search: '',
        tipoAudiencia: ''
    });

    // Fetch all segments once (only on first load)
    const fetchAllSegmentos = useCallback(async () => {
        setLoading(true);
        try {
            const response = await segmentacionApi.getAll({ includeDeleted: true });
            let allData = Array.isArray(response) ? response : [];

            // Sort by creation date DESC (newest first)
            allData.sort((a, b) =>
                new Date(b.fechaCreacion).getTime() - new Date(a.fechaCreacion).getTime()
            );

            setAllSegmentos(allData);
            setHasLoaded(true);
            console.log('✓ Segmentos cargados en caché global:', allData.length);
        } catch (err: any) {
            console.error('Error fetching segments:', err);
            setError(err.message);
        } finally {
            setLoading(false);
        }
    }, []);

    // Apply filters and pagination client-side whenever filters or data change
    useEffect(() => {
        if (!isAuthenticated) return;

        // Only fetch if we haven't loaded yet
        if (!hasLoaded) {
            fetchAllSegmentos();
            return;
        }

        // Apply filters client-side
        let filtered = [...allSegmentos];

        // Apply estado filter
        if (filters.estado) {
            filtered = filtered.filter(s => s.estado === filters.estado);
        }

        // Apply tipoAudiencia filter
        if (filters.tipoAudiencia) {
            filtered = filtered.filter(s => s.tipoAudiencia === filters.tipoAudiencia);
        }

        // Apply search filter
        if (filters.search) {
            const searchLower = filters.search.toLowerCase();
            filtered = filtered.filter(s =>
                s.nombre.toLowerCase().includes(searchLower) ||
                s.descripcion?.toLowerCase().includes(searchLower)
            );
        }

        // Client-side pagination (10 per page)
        const pageSize = 10;
        const startIndex = filters.page * pageSize;
        const endIndex = startIndex + pageSize;
        const paginatedData = filtered.slice(startIndex, endIndex);

        setSegmentos(paginatedData);
        setTotalElements(filtered.length);
        setTotalPages(Math.ceil(filtered.length / pageSize));
        setCurrentPage(filters.page);
    }, [allSegmentos, filters, hasLoaded, fetchAllSegmentos, isAuthenticated]);

    // Clear state on logout
    useEffect(() => {
        if (!isAuthenticated) {
            setAllSegmentos([]);
            setSegmentos([]);
            setHasLoaded(false);
        }
    }, [isAuthenticated]);

    const setFilter = (key: string, value: any) => {
        setFilters(prev => {
            // If value is the same, don't update to avoid re-render
            if (prev[key as keyof typeof prev] === value) return prev;

            // Reset to page 0 when changing filters (except page itself)
            if (key !== 'page') {
                return { ...prev, [key]: value, page: 0 };
            }
            return { ...prev, [key]: value };
        });
    };

    // Manual refresh
    const refresh = async () => {
        await fetchAllSegmentos();
    };

    // Optimistic updates
    const addSegmento = useCallback((segmento: Segmento) => {
        setAllSegmentos(prev => [segmento, ...prev]);
        console.log('✓ Segmento agregado al caché:', segmento.id);
    }, []);

    const updateSegmento = useCallback((updatedSegmento: Segmento) => {
        setAllSegmentos(prev =>
            prev.map(s => s.id === updatedSegmento.id ? updatedSegmento : s)
        );
        console.log('✓ Segmento actualizado en caché:', updatedSegmento.id);
    }, []);

    const removeSegmento = useCallback((id: number) => {
        // Mark as deleted instead of removing
        setAllSegmentos(prev =>
            prev.map(s => s.id === id ? { ...s, estado: 'ELIMINADO' } : s)
        );
        console.log('✓ Segmento marcado como eliminado:', id);
    }, []);

    return (
        <SegmentosContext.Provider
            value={{
                allSegmentos,
                segmentos,
                loading,
                error,
                totalPages,
                totalElements,
                currentPage,
                filters,
                setFilter,
                refresh,
                addSegmento,
                updateSegmento,
                removeSegmento
            }}
        >
            {children}
        </SegmentosContext.Provider>
    );
};

export const useSegmentosContext = () => {
    const context = useContext(SegmentosContext);
    if (!context) {
        throw new Error('useSegmentosContext must be used within SegmentosProvider');
    }
    return context;
};
