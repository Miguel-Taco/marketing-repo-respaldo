import { useState, useEffect } from 'react';
import { campanasApi } from '../services/campanas.api';
import { CampanaDetalle } from '../types/campana.types';

/**
 * Hook para obtener y gestionar los detalles de una campaña
 */
export const useCampanaDetail = (id: number | null) => {
    const [campana, setCampana] = useState<CampanaDetalle | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const fetchCampana = async () => {
        if (!id) {
            setCampana(null);
            return;
        }

        setLoading(true);
        setError(null);

        try {
            const data = await campanasApi.getById(id);
            setCampana(data);
        } catch (err) {
            console.error('Error fetching campaign details:', err);
            setError('Error al cargar los detalles de la campaña');
            setCampana(null);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchCampana();
    }, [id]);

    return {
        campana,
        loading,
        error,
        refresh: fetchCampana,
    };
};
