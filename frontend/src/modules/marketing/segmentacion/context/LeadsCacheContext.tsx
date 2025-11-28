import React, { createContext, useContext, useState, useCallback, ReactNode, useEffect } from 'react';
import { leadsApi } from '../../leads/services/leads.api';

interface Lead {
    id: number;
    nombre: string;
    email: string;
    telefono?: string;
    edad?: number;
    genero?: string;
    distrito?: string;
    // ... otros campos según necesites
}

interface LeadsCacheContextType {
    leads: Lead[];
    loading: boolean;
    error: string | null;
    refresh: () => Promise<void>;
    isLoaded: boolean;
}

const LeadsCacheContext = createContext<LeadsCacheContextType | undefined>(undefined);

export const LeadsCacheProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
    const [leads, setLeads] = useState<Lead[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [isLoaded, setIsLoaded] = useState(false);

    const fetchAllLeads = useCallback(async () => {
        // Solo cargar si aún no se ha cargado
        if (isLoaded) return;

        setLoading(true);
        try {
            console.log('🔄 Cargando leads en caché del frontend...');
            const response = await leadsApi.getAll();
            const allLeads = Array.isArray(response) ? response : [];

            setLeads(allLeads);
            setIsLoaded(true);
            console.log(`✓ ${allLeads.length} leads cargados en caché del frontend`);
        } catch (err: any) {
            console.error('Error cargando leads en caché:', err);
            setError(err.message);
        } finally {
            setLoading(false);
        }
    }, [isLoaded]);

    // Cargar automáticamente al montar el componente
    useEffect(() => {
        fetchAllLeads();
    }, [fetchAllLeads]);

    const refresh = async () => {
        setIsLoaded(false);
        await fetchAllLeads();
    };

    return (
        <LeadsCacheContext.Provider
            value={{
                leads,
                loading,
                error,
                refresh,
                isLoaded
            }}
        >
            {children}
        </LeadsCacheContext.Provider>
    );
};

export const useLeadsCache = () => {
    const context = useContext(LeadsCacheContext);
    if (!context) {
        throw new Error('useLeadsCache must be used within LeadsCacheProvider');
    }
    return context;
};
