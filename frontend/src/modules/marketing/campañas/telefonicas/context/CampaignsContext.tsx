import React, { createContext, useContext, useState, useCallback, ReactNode } from 'react';
import { CampaniaTelefonica } from '../types';
import { telemarketingApi } from '../services/telemarketingApi';

interface CampaignsContextProps {
    campanias: CampaniaTelefonica[];
    loading: boolean;
    error: string | null;
    filters: {
        searchTerm: string;
        estadoFilter: string;
        ordenarPor: string;
    };
    setFilter: (key: string, value: any) => void;
    fetchCampanias: (force?: boolean) => Promise<void>;
    autoNext: boolean;
    setAutoNext: (value: boolean) => void;
}

const CampaignsContext = createContext<CampaignsContextProps | undefined>(undefined);

export const CampaignsProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
    const [campanias, setCampanias] = useState<CampaniaTelefonica[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    // Filtros
    const [filters, setFilters] = useState({
        searchTerm: '',
        estadoFilter: 'Todos',
        ordenarPor: 'prioridad'
    });

    // Flag para saber si ya se cargó al menos una vez
    const [hasLoaded, setHasLoaded] = useState(false);

    // Auto-next: obtener siguiente contacto automáticamente al finalizar llamada
    const [autoNext, setAutoNext] = useState(false);

    // TODO: Obtener el ID del agente actual del contexto/autenticación
    const idAgente = 1; // TODO: Get from auth context (using existing agent ID from database)

    const fetchCampanias = useCallback(async (force = false) => {
        // Si ya tenemos datos y no es forzado, no hacemos nada (cache hit)
        if (!force && hasLoaded) {
            return;
        }

        setLoading(true);
        try {
            const data = await telemarketingApi.getCampaniasAgente(idAgente);
            setCampanias(data);
            setHasLoaded(true);
        } catch (err: any) {
            console.error('Error cargando campañas:', err);
            setError(err.message);
        } finally {
            setLoading(false);
        }
    }, [hasLoaded, idAgente]);

    // Efecto para cargar cuando cambian los filtros
    // En este caso, los filtros son solo para UI (filtrado en frontend),
    // así que NO necesitamos recargar desde la API cuando cambian.
    // Solo cargamos la primera vez.
    React.useEffect(() => {
        const load = async () => {
            if (hasLoaded) return;

            setLoading(true);
            try {
                const data = await telemarketingApi.getCampaniasAgente(idAgente);
                setCampanias(data);
                setHasLoaded(true);
            } catch (err: any) {
                console.error('Error cargando campañas:', err);
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        load();
    }, [hasLoaded, idAgente]);

    const setFilter = (key: string, value: any) => {
        setFilters(prev => {
            // Si el valor es el mismo, no actualizamos para evitar re-render
            if (prev[key as keyof typeof prev] === value) return prev;
            return { ...prev, [key]: value };
        });
    };

    // Wrapper para forzar recarga manual (botón refresh)
    const refresh = useCallback(async () => {
        setLoading(true);
        try {
            const data = await telemarketingApi.getCampaniasAgente(idAgente);
            setCampanias(data);
            setHasLoaded(true);
        } catch (err: any) {
            console.error('Error cargando campañas:', err);
            setError(err.message);
        } finally {
            setLoading(false);
        }
    }, [idAgente]);

    return (
        <CampaignsContext.Provider value={{
            campanias,
            loading,
            error,
            filters,
            setFilter,
            fetchCampanias: refresh, // Exponemos refresh como fetchCampanias para compatibilidad
            autoNext,
            setAutoNext
        }}>
            {children}
        </CampaignsContext.Provider>
    );
};

export const useCampaignsContext = () => {
    const context = useContext(CampaignsContext);
    if (!context) {
        throw new Error('useCampaignsContext must be used within a CampaignsProvider');
    }
    return context;
};
