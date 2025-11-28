import { useLeadsContext } from '../context/LeadsContext';

export const useLeads = () => {
    const context = useLeadsContext();

    return {
        leads: context.leads,
        loading: context.loading,
        error: context.error,
        totalPages: context.totalPages,
        totalElements: context.totalElements,
        currentPage: context.currentPage,
        refresh: () => context.fetchLeads(true), // Force refresh
        setFilter: context.setFilter
    };
};
