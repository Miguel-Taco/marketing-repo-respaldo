import { useCampanasGestorContext } from '../context/CampanasGestorContext';
import { CampanaListItem } from '../types/campana.types';

interface UseCampanasReturn {
    campanas: CampanaListItem[];
    loading: boolean;
    error: string | null;
    refresh: () => Promise<void>;
    setFilter: (key: string, value: any) => void;
    totalPages: number;
    totalElements: number;
    currentPage: number;
    filtros: any; // Or define a specific type if available
}

export const useCampanas = (): UseCampanasReturn => {
    const { campanas, fetchCampanas, setCampanasFilter, setCampanasPage } = useCampanasGestorContext();

    const setFilter = (key: string, value: any) => {
        if (key === 'page') {
            setCampanasPage(value);
        } else {
            setCampanasFilter(key, value);
        }
    };

    const refresh = async () => {
        await fetchCampanas(true);
    };

    return {
        campanas: campanas.data,
        loading: campanas.loading,
        error: null, // Context doesn't currently track error explicitly in state interface but logs it
        refresh,
        setFilter,
        totalPages: campanas.pagination.totalPages,
        totalElements: campanas.pagination.totalElements,
        currentPage: campanas.pagination.page,
        filtros: campanas.filters, // Expose filters
    };
};
