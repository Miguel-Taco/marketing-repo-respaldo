import React, { createContext, useContext, useState, useCallback, ReactNode, useEffect } from 'react';
import { Encuesta } from '../types';
import { encuestasApi } from '../services/encuestas.api';
import { useAuth } from '../../../../../shared/context/AuthContext';

interface EncuestasContextProps {
    encuestas: Encuesta[];
    loading: boolean;
    error: string | null;
    fetchEncuestas: (force?: boolean) => Promise<void>;
    analyticsCache: Record<string, any>;
    cacheAnalytics: (key: string, data: any) => void;
    analyticsSelectedId: string;
    setAnalyticsSelectedId: (id: string) => void;
}

const EncuestasContext = createContext<EncuestasContextProps | undefined>(undefined);

export const EncuestasProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
    const { isAuthenticated } = useAuth();
    const [encuestas, setEncuestas] = useState<Encuesta[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [hasLoaded, setHasLoaded] = useState(false);

    const fetchEncuestas = useCallback(async (force = false) => {
        if (!force && hasLoaded) {
            return;
        }

        setLoading(true);
        try {
            const data = await encuestasApi.getAll();
            setEncuestas(data);
            setHasLoaded(true);
            setError(null);
        } catch (err: any) {
            console.error('Error loading encuestas:', err);
            setError(err.message || 'Error al cargar las encuestas');
        } finally {
            setLoading(false);
        }
    }, [hasLoaded]);

    // Clear state on logout
    useEffect(() => {
        if (!isAuthenticated) {
            setEncuestas([]);
            setHasLoaded(false);
        }
    }, [isAuthenticated]);

    const [analyticsCache, setAnalyticsCache] = useState<Record<string, any>>({});
    const [analyticsSelectedId, setAnalyticsSelectedId] = useState<string>('');

    const cacheAnalytics = useCallback((key: string, data: any) => {
        setAnalyticsCache(prev => ({ ...prev, [key]: data }));
    }, []);

    return (
        <EncuestasContext.Provider value={{
            encuestas,
            loading,
            error,
            fetchEncuestas,
            analyticsCache,
            cacheAnalytics,
            analyticsSelectedId,
            setAnalyticsSelectedId
        }}>
            {children}
        </EncuestasContext.Provider>
    );
};

export const useEncuestasContext = () => {
    const context = useContext(EncuestasContext);
    if (!context) {
        throw new Error('useEncuestasContext must be used within a EncuestasProvider');
    }
    return context;
};
