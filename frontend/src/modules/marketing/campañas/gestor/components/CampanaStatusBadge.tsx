import React from 'react';
import { CampanaEstado, EstadoCampanaEnum } from '../types/campana.types';

interface CampanaStatusBadgeProps {
    estado: CampanaEstado | EstadoCampanaEnum;
}

export const CampanaStatusBadge: React.FC<CampanaStatusBadgeProps> = ({ estado }) => {
    // Convert enum or string to display string
    let estadoStr: string;

    // Handle both enum values and string values from backend
    if (typeof estado === 'string') {
        // If it's a string, normalize it (could be "BORRADOR" or "Borrador")
        const normalized = estado.toUpperCase();
        switch (normalized) {
            case 'BORRADOR':
                estadoStr = 'Borrador';
                break;
            case 'PROGRAMADA':
                estadoStr = 'Programada';
                break;
            case 'VIGENTE':
                estadoStr = 'Vigente';
                break;
            case 'PAUSADA':
                estadoStr = 'Pausada';
                break;
            case 'FINALIZADA':
                estadoStr = 'Finalizada';
                break;
            case 'CANCELADA':
                estadoStr = 'Cancelada';
                break;
            default:
                estadoStr = estado; // Fallback to original
        }
    } else {
        // Convert EstadoCampanaEnum to display string
        switch (estado) {
            case EstadoCampanaEnum.BORRADOR:
                estadoStr = 'Borrador';
                break;
            case EstadoCampanaEnum.PROGRAMADA:
                estadoStr = 'Programada';
                break;
            case EstadoCampanaEnum.VIGENTE:
                estadoStr = 'Vigente';
                break;
            case EstadoCampanaEnum.PAUSADA:
                estadoStr = 'Pausada';
                break;
            case EstadoCampanaEnum.FINALIZADA:
                estadoStr = 'Finalizada';
                break;
            case EstadoCampanaEnum.CANCELADA:
                estadoStr = 'Cancelada';
                break;
            default:
                estadoStr = 'Desconocido';
        }
    }

    const getStatusStyle = () => {
        switch (estadoStr) {
            case 'Vigente':
                return 'bg-success-chip text-success-chip';
            case 'Pausada':
                return 'bg-warn-chip text-warn-chip';
            case 'Finalizada':
            case 'Cancelada':
                return 'bg-default-chip text-default-chip';
            case 'Borrador':
            case 'Programada':
                return 'bg-info-chip text-info-chip';
            default:
                return 'bg-default-chip text-default-chip';
        }
    };

    return (
        <span className={`px-2.5 py-1 rounded-full text-xs font-medium ${getStatusStyle()}`}>
            {estadoStr}
        </span>
    );
};
