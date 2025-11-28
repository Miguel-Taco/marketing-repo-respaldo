import React, { createContext, useContext, useState, useCallback, ReactNode } from 'react';
import { LoteImportacion } from '../types/lead.types';
import { leadsApi } from '../services/leads.api';

interface ImportHistoryContextProps {
    history: LoteImportacion[];
    loading: boolean;
    error: string | null;
    currentPage: number;
    totalPages: number;
    totalElements: number;
    loadHistory: (page?: number, force?: boolean) => Promise<void>;
}

const ImportHistoryContext = createContext<ImportHistoryContextProps | undefined>(undefined);

export const ImportHistoryProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
    const [history, setHistory] = useState<LoteImportacion[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const [hasLoaded, setHasLoaded] = useState(false);

    const loadHistory = useCallback(async (page: number = 0, force: boolean = false) => {
        // Si ya cargamos y no es forzado, y estamos en la misma página, no recargar
        if (!force && hasLoaded && page === currentPage) {
            return;
        }

        setLoading(true);
        setError(null);

        try {
            const data = await leadsApi.getImportHistory(page, 10);
            setHistory(data.content || []);
            setCurrentPage(data.currentPage || 0);
            setTotalPages(data.totalPages || 0);
            setTotalElements(data.totalElements || 0);
            setHasLoaded(true);
        } catch (err: any) {
            console.error("Error loading history:", err);
            setError(err.message || "Error al cargar el historial");
            setHistory([]);
        } finally {
            setLoading(false);
        }
    }, [hasLoaded, currentPage]);

    // Cargar la primera vez
    React.useEffect(() => {
        if (!hasLoaded) {
            loadHistory(0);
        }
    }, [hasLoaded, loadHistory]);

    return (
        <ImportHistoryContext.Provider value={{
            history,
            loading,
            error,
            currentPage,
            totalPages,
            totalElements,
            loadHistory
        }}>
            {children}
        </ImportHistoryContext.Provider>
    );
};

export const useImportHistory = () => {
    const context = useContext(ImportHistoryContext);
    if (!context) {
        throw new Error('useImportHistory must be used within an ImportHistoryProvider');
    }
    return context;
};
