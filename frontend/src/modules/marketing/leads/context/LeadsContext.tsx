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
        // Pero si cambiaron los filtros, deberíamos invalidar?
        // La estrategia aquí es: si los filtros cambian, useEffect en el consumidor o aquí debería disparar.
        // Para simplificar: fetchLeads siempre trae datos basados en 'filters'.
        // La optimización es: si llamamos fetchLeads con los MISMOS filtros que ya tenemos cargados, no traer.
        // Pero comparar filtros es complejo.
        // El requerimiento es: "cuando cambio de pestaña... no volver a hacer la consulta".
        // Esto implica que al montar el componente, si ya hay datos, no se llama a fetchLeads automáticamente.

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
    // OJO: Si cambiamos filtros, queremos recargar SÍ o SÍ.
    // Por tanto, necesitamos un useEffect que escuche 'filters'.
    // Pero si ponemos useEffect aquí, se ejecutará al inicio.
    React.useEffect(() => {
        // Si cambiamos filtros, reseteamos hasLoaded para forzar carga?
        // O simplemente llamamos a la API.
        // Mejor estrategia:
        // Cuando se actualiza un filtro, setHasLoaded(false) para que la próxima llamada (o este efecto) cargue.

        // Vamos a hacer que el cambio de filtro dispare la carga automáticamente.
        // Pero necesitamos distinguir la "primera carga" de "cambio de filtros".

        // Si es la primera vez (mount), y no hemos cargado, cargamos.
        // Si cambiamos filtros, cargamos.

        // El problema es que al montar el Provider, filters es default.
        // Si ya veníamos de otra pestaña, el Provider se desmontó?
        // Si el Provider está en AppRouter, NO se desmonta al cambiar de ruta (siempre que estemos dentro del Router).
        // Entonces, al cambiar de pestaña (Ruta A -> Ruta B), el Provider sigue vivo.
        // Los filtros siguen igual.
        // El useEffect de 'filters' NO se dispara porque no cambiaron.
        // Entonces NO se hace fetch. ¡Perfecto!

        // Pero necesitamos cargar la primera vez.
        // Si hasLoaded es false, cargamos.

        // Y si cambiamos filtros, hasLoaded debería invalidarse?
        // Si cambio pagina, quiero nuevos datos.

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
            load();
        }

    }, [filters, isAuthenticated]); // Se ejecuta al montar (filters inicial) y al cambiar filters.

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
            return { ...prev, [key]: value };
        });
    };

    // Wrapper para forzar recarga manual (botón refresh)
    const refresh = useCallback(async () => {
        // Forzamos re-ejecución del fetch con los mismos filtros
        // Podemos hacerlo invalidando hasLoaded o llamando directo a la API
        // Llamamos directo para reutilizar lógica
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
    }, [filters]);

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
            fetchLeads: refresh // Exponemos refresh como fetchLeads para compatibilidad o uso manual
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
