import { useState, useEffect } from 'react';
import { campanasApi } from '../services/campanas.api';
import { HistorialItem } from '../types/campana.types';

/**
 * Hook para obtener y gestionar el historial de una campaña
 */
export const useCampanaHistorial = (idCampana: number | null) => {
    const [historial, setHistorial] = useState<HistorialItem[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const fetchHistorial = async () => {
        if (!idCampana) {
            setHistorial([]);
            return;
        }

        setLoading(true);
        setError(null);

        try {
            const data = await campanasApi.getHistorial(idCampana);
            setHistorial(data || []);
        } catch (err) {
            console.error('Error fetching campaign history:', err);
            setError('Error al cargar el historial');
            setHistorial([]);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchHistorial();
    }, [idCampana]);

    return {
        historial,
        loading,
        error,
        refresh: fetchHistorial,
    };
};
