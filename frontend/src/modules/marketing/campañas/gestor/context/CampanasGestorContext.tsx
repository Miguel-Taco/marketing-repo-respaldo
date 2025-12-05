import React, { createContext, useContext, useState, useCallback, ReactNode, useEffect } from 'react';
import { CampanaListItem as Campana } from '../types/campana.types';
import { PlantillaCampana } from '../types/plantilla.types';
import { campanasApi } from '../services/campanas.api';
import { plantillasApi } from '../services/plantillas.api';
import { useAuth } from '../../../../../shared/context/AuthContext';

// --- Interfaces de Estado ---

interface PaginationState {
    page: number;
    size: number;
    totalPages: number;
    totalElements: number;
}

interface CampanasState {
    data: Campana[];
    loading: boolean;
    pagination: PaginationState;
    filters: {
        nombre: string;
        estado: string;
        prioridad: string;
        canalEjecucion: string;
        esArchivado: boolean;
    };
    hasLoaded: boolean;
}

interface PlantillasState {
    data: PlantillaCampana[];
    loading: boolean;
    pagination: PaginationState;
    filters: {
        nombre: string;
        canalEjecucion: string;
    };
    hasLoaded: boolean;
}

interface HistorialItem {
    idHistorial: number;
    idCampana: number;
    nombreCampana: string;
    fechaAccion: string;
    tipoAccion: string;
    usuarioResponsable: string;
    descripcionDetalle: string;
}

interface HistorialState {
    data: HistorialItem[];
    loading: boolean;
    pagination: PaginationState;
    filters: {
        tipoAccion: string;
        fechaDesde: string;
        fechaHasta: string;
    };
    hasLoaded: boolean;
}

// --- Context Props ---

interface CampanasGestorContextProps {
    campanas: CampanasState;
    plantillas: PlantillasState;
    historial: HistorialState;

    // Actions Campanas
    setCampanasFilter: (key: string, value: any) => void;
    setCampanasPage: (page: number) => void;
    fetchCampanas: (force?: boolean) => Promise<void>;

    // Actions Plantillas
    setPlantillasFilter: (key: string, value: any) => void;
    setPlantillasPage: (page: number) => void;
    fetchPlantillas: (force?: boolean) => Promise<void>;

    // Actions Historial
    setHistorialFilter: (key: string, value: any) => void;
    setHistorialPage: (page: number) => void;
    fetchHistorial: (force?: boolean) => Promise<void>;
}

const CampanasGestorContext = createContext<CampanasGestorContextProps | undefined>(undefined);

export const CampanasGestorProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
    const { isAuthenticated } = useAuth();

    // --- State: Campanas ---
    const [campanas, setCampanas] = useState<CampanasState>({
        data: [],
        loading: false,
        pagination: { page: 0, size: 10, totalPages: 0, totalElements: 0 },
        filters: { nombre: '', estado: '', prioridad: '', canalEjecucion: '', esArchivado: false },
        hasLoaded: false
    });

    // --- State: Plantillas ---
    const [plantillas, setPlantillas] = useState<PlantillasState>({
        data: [],
        loading: false,
        pagination: { page: 0, size: 10, totalPages: 0, totalElements: 0 },
        filters: { nombre: '', canalEjecucion: '' },
        hasLoaded: false
    });

    // --- State: Historial ---
    const [historial, setHistorial] = useState<HistorialState>({
        data: [],
        loading: false,
        pagination: { page: 0, size: 6, totalPages: 0, totalElements: 0 },
        filters: { tipoAccion: '', fechaDesde: '', fechaHasta: '' },
        hasLoaded: false
    });

    // --- Actions: Campanas ---
    const fetchCampanas = useCallback(async (force = false) => {
        if (!force && campanas.hasLoaded) return;

        setCampanas(prev => ({ ...prev, loading: true }));
        try {
            const response = await campanasApi.getAll({
                page: campanas.pagination.page,
                size: campanas.pagination.size,
                nombre: campanas.filters.nombre || undefined,
                estado: campanas.filters.estado || undefined,
                prioridad: campanas.filters.prioridad || undefined,
                canalEjecucion: campanas.filters.canalEjecucion || undefined,
                esArchivado: campanas.filters.esArchivado
            });

            console.log('Campanas API Response:', response);

            setCampanas(prev => ({
                ...prev,
                loading: false,
                hasLoaded: true,
                data: response.content,
                pagination: {
                    ...prev.pagination,
                    totalPages: response.totalPages,
                    totalElements: response.totalElements
                }
            }));
        } catch (error) {
            console.error('Error fetching campanas:', error);
            setCampanas(prev => ({ ...prev, loading: false }));
        }
    }, [campanas.filters, campanas.pagination.page, campanas.pagination.size, campanas.hasLoaded]);

    const setCampanasFilter = (key: string, value: any) => {
        setCampanas(prev => ({
            ...prev,
            hasLoaded: false, // Invalidate cache
            pagination: { ...prev.pagination, page: 0 }, // Reset page
            filters: { ...prev.filters, [key]: value }
        }));
    };

    const setCampanasPage = (page: number) => {
        setCampanas(prev => ({
            ...prev,
            hasLoaded: false,
            pagination: { ...prev.pagination, page }
        }));
    };

    // --- Actions: Plantillas ---
    const fetchPlantillas = useCallback(async (force = false) => {
        if (!force && plantillas.hasLoaded) return;

        setPlantillas(prev => ({ ...prev, loading: true }));
        try {
            const response = await plantillasApi.getAll({
                page: plantillas.pagination.page,
                size: plantillas.pagination.size,
                nombre: plantillas.filters.nombre || undefined,
                canalEjecucion: plantillas.filters.canalEjecucion || undefined
            });

            setPlantillas(prev => ({
                ...prev,
                loading: false,
                hasLoaded: true,
                data: response.content,
                pagination: {
                    ...prev.pagination,
                    totalPages: response.total_pages,
                    totalElements: response.total_elements
                }
            }));
        } catch (error) {
            console.error('Error fetching plantillas:', error);
            setPlantillas(prev => ({ ...prev, loading: false }));
        }
    }, [plantillas.filters, plantillas.pagination.page, plantillas.pagination.size, plantillas.hasLoaded]);

    const setPlantillasFilter = (key: string, value: any) => {
        setPlantillas(prev => ({
            ...prev,
            hasLoaded: false,
            pagination: { ...prev.pagination, page: 0 },
            filters: { ...prev.filters, [key]: value }
        }));
    };

    const setPlantillasPage = (page: number) => {
        setPlantillas(prev => ({
            ...prev,
            hasLoaded: false,
            pagination: { ...prev.pagination, page }
        }));
    };

    // --- Actions: Historial ---
    const fetchHistorial = useCallback(async (force = false) => {
        if (!force && historial.hasLoaded) return;

        setHistorial(prev => ({ ...prev, loading: true }));
        try {
            const response = await campanasApi.getHistorial({
                page: historial.pagination.page,
                size: historial.pagination.size,
                tipoAccion: historial.filters.tipoAccion || undefined,
                fechaDesde: historial.filters.fechaDesde ? new Date(historial.filters.fechaDesde).toISOString() : undefined,
                fechaHasta: historial.filters.fechaHasta ? new Date(historial.filters.fechaHasta).toISOString() : undefined
            });

            setHistorial(prev => ({
                ...prev,
                loading: false,
                hasLoaded: true,
                data: response.content,
                pagination: {
                    ...prev.pagination,
                    totalPages: response.total_pages,
                    totalElements: response.total_elements
                }
            }));
        } catch (error) {
            console.error('Error fetching historial:', error);
            setHistorial(prev => ({ ...prev, loading: false }));
        }
    }, [historial.filters, historial.pagination.page, historial.pagination.size, historial.hasLoaded]);

    const setHistorialFilter = (key: string, value: any) => {
        setHistorial(prev => ({
            ...prev,
            hasLoaded: false,
            pagination: { ...prev.pagination, page: 0 },
            filters: { ...prev.filters, [key]: value }
        }));
    };

    const setHistorialPage = (page: number) => {
        setHistorial(prev => ({
            ...prev,
            hasLoaded: false,
            pagination: { ...prev.pagination, page }
        }));
    };

    // --- Effects to trigger fetch on state change (if not loaded) ---
    useEffect(() => {
        if (!isAuthenticated) {
            setCampanas(prev => ({ ...prev, hasLoaded: false, data: [] }));
            setPlantillas(prev => ({ ...prev, hasLoaded: false, data: [] }));
            setHistorial(prev => ({ ...prev, hasLoaded: false, data: [] }));
        }
    }, [isAuthenticated]);

    useEffect(() => {
        if (isAuthenticated && !campanas.hasLoaded) fetchCampanas();
    }, [campanas.hasLoaded, fetchCampanas, isAuthenticated]);

    useEffect(() => {
        if (isAuthenticated && !plantillas.hasLoaded) fetchPlantillas();
    }, [plantillas.hasLoaded, fetchPlantillas, isAuthenticated]);

    useEffect(() => {
        if (isAuthenticated && !historial.hasLoaded) fetchHistorial();
    }, [historial.hasLoaded, fetchHistorial, isAuthenticated]);


    return (
        <CampanasGestorContext.Provider value={{
            campanas,
            plantillas,
            historial,
            setCampanasFilter,
            setCampanasPage,
            fetchCampanas,
            setPlantillasFilter,
            setPlantillasPage,
            fetchPlantillas,
            setHistorialFilter,
            setHistorialPage,
            fetchHistorial
        }}>
            {children}
        </CampanasGestorContext.Provider>
    );
};

export const useCampanasGestorContext = () => {
    const context = useContext(CampanasGestorContext);
    if (!context) {
        throw new Error('useCampanasGestorContext must be used within a CampanasGestorProvider');
    }
    return context;
};
