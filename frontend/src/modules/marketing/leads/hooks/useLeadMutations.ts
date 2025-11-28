import { useState } from 'react';
import { leadsApi } from '../services/leads.api';
import { CreateLeadDTO, ChangeStatusDTO } from '../types/lead.types';

export const useLeadMutations = () => {
    const [processing, setProcessing] = useState(false);

    const createLead = async (data: CreateLeadDTO) => {
        setProcessing(true);
        try {
            await leadsApi.create(data);
            return { success: true };
        } catch (error: any) {
            // Extract error message from backend response
            const errorMessage = error.response?.data?.message || error.message || "Error al crear el lead";
            return { success: false, error: errorMessage };
        } finally {
            setProcessing(false);
        }
    };

    const cualificarLead = async (id: number, nuevoEstado: 'CALIFICADO' | 'DESCARTADO', motivo?: string) => {
        setProcessing(true);
        try {
            await leadsApi.updateStatus(id, nuevoEstado, motivo);
            return { success: true };
        } catch (error: any) {
            return { success: false, error: error.message };
        } finally {
            setProcessing(false);
        }
    };

    const eliminarLead = async (id: number) => {
        setProcessing(true);
        try {
            await leadsApi.delete(id);
            return { success: true };
        } catch (error: any) {
            return { success: false, error: error.message };
        } finally {
            setProcessing(false);
        }
    };

    return {
        createLead,
        cualificarLead,
        eliminarLead,
        processing
    };
};
