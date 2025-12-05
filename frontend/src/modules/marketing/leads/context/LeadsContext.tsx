import React, { createContext, useContext, useState, useCallback, ReactNode } from 'react';
import { Lead } from '../types/lead.types';
import { leadsApi } from '../services/leads.api';
import { useAuth } from '../../../../shared/context/AuthContext';

interface LeadsContextProps {
    leads: Lead[];
    loading: boolean;
    error: string | null;
    totalPages: number;
    totalElements: number;
    currentPage: number;
    filters: {
        page: number;
        estado: string;
        search: string;
        fuenteTipo: string;
    };
    setFilter: (key: string, value: any) => void;
    fetchLeads: (force?: boolean) => Promise<void>;
    refresh: () => Promise<void>;
}

const LeadsContext = createContext<LeadsContextProps | undefined>(undefined);

export const LeadsProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
    const { isAuthenticated } = useAuth();
    const [leads, setLeads] = useState<Lead[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    // Metadatos de paginación
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const [currentPage, setCurrentPage] = useState(0);

    // Filtros
    const [filters, setFilters] = useState({
        page: 0,
        estado: '',
        search: '',
        fuenteTipo: ''
    });

    // Flag para saber si ya se cargó al menos una vez
    const [hasLoaded, setHasLoaded] = useState(false);

    const fetchLeads = useCallback(async (force = false) => {
        // Si ya tenemos datos y no es forzado, no hacemos nada (cache hit)
        if (!force && hasLoaded) {
            return;
        }

        setLoading(true);
        try {
            const response = await leadsApi.getAll(
                filters.page,
                10,
                filters.estado || undefined,
                filters.search || undefined,
                filters.fuenteTipo || undefined
            );
            const paginatedData = response.data;
            setLeads(paginatedData?.content || []);
            setTotalPages(paginatedData?.totalPages || 0);
            setTotalElements(paginatedData?.totalElements || 0);
            setCurrentPage(paginatedData?.number || 0);
            setHasLoaded(true);
        } catch (err: any) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    }, [filters, hasLoaded]);

    // Efecto para recargar cuando cambian los filtros
    React.useEffect(() => {
        const load = async () => {
            setLoading(true);
            try {
                const response = await leadsApi.getAll(
                    filters.page,
                    10,
                    filters.estado || undefined,
                    filters.search || undefined,
                    filters.fuenteTipo || undefined
                );
                const paginatedData = response.data;
                setLeads(paginatedData?.content || []);
                setTotalPages(paginatedData?.totalPages || 0);
                setTotalElements(paginatedData?.totalElements || 0);
                setCurrentPage(paginatedData?.number || 0);
                setHasLoaded(true);
            } catch (err: any) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        if (isAuthenticated) {
            // Si ya cargamos (hasLoaded) y cambiaron los filtros, recargamos.
            // Si NO hemos cargado (primera vez), cargamos.
            // PERO: Si hasLoaded es true, y los filtros NO cambiaron (porque useEffect se dispara por mount),
            // no deberíamos cargar si ya tenemos datos.
            // El problema es que este useEffect depende de [filters].
            // Al montar, filters es el inicial. Si venimos de otra pestaña, el estado se mantuvo?
            // Si LeadsProvider está arriba en AppRouter, el estado se mantiene.
            // Entonces al navegar, filters no cambia. Este useEffect NO se dispara si filters es igual.
            // EXCEPTO si el componente se desmontó y montó.
            // Si LeadsProvider NO se desmonta, este useEffect no corre al navegar.
            // Si LeadsProvider SE desmonta, perdemos el estado y hasLoaded es false.

            // Asumimos LeadsProvider persiste (está en AppRouter).
            // Entonces este useEffect solo corre cuando filters cambia.
            // Y necesitamos una carga inicial si hasLoaded es false.
            if (!hasLoaded) {
                load();
            } else {
                // Si ya cargó, y este efecto se disparó, es porque filters cambió?
                // O porque isAuthenticated cambió?
                // Si filters cambió, queremos recargar.
                // Como detectamos si filters cambió respecto al anterior?
                // React useEffect se dispara si deps cambian.
                // Si estamos aquí, es porque filters o isAuthenticated cambiaron.
                // Si hasLoaded es true, asumimos que es un cambio de filtro y recargamos.
                // PERO cuidado con el mount inicial si ya estaba cargado.
                // (React StrictMode puede ejecutar efectos doble vez, pero en prod no).

                // Simplificación: Si cambiamos filtros, invalidamos hasLoaded implícitamente al cargar nuevo data.
                load();
            }
        }

    }, [filters, isAuthenticated]);

    // Clear state on logout
    React.useEffect(() => {
        if (!isAuthenticated) {
            setLeads([]);
            setHasLoaded(false);
        }
    }, [isAuthenticated]);

    const setFilter = (key: string, value: any) => {
        setFilters(prev => {
            // Si el valor es el mismo, no actualizamos para evitar re-render/fetch
            if (prev[key as keyof typeof prev] === value) return prev;
            // Al cambiar filtro, hasLoaded sigue true, pero el useEffect de arriba detectará el cambio y recargará.
            return { ...prev, [key]: value };
        });
    };

    // Wrapper para forzar recarga manual
    const refresh = useCallback(async () => {
        await fetchLeads(true);
    }, [fetchLeads]);

    return (
        <LeadsContext.Provider value={{
            leads,
            loading,
            error,
            totalPages,
            totalElements,
            currentPage,
            filters,
            setFilter,
            fetchLeads,
            refresh
        }}>
            {children}
        </LeadsContext.Provider>
    );
};

export const useLeadsContext = () => {
    const context = useContext(LeadsContext);
    if (!context) {
        throw new Error('useLeadsContext must be used within a LeadsProvider');
    }
    return context;
};
