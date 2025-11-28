import React from 'react';

export type Prioridad = 'ALTA' | 'MEDIA' | 'BAJA';

interface CampanaPriorityBadgeProps {
    prioridad: string;
}

export const CampanaPriorityBadge: React.FC<CampanaPriorityBadgeProps> = ({ prioridad }) => {
    const getPriorityStyle = () => {
        const prioridadUpper = prioridad.toUpperCase();
        switch (prioridadUpper) {
            case 'ALTA':
                return 'bg-red-100 text-red-800';
            case 'MEDIA':
                return 'bg-yellow-100 text-yellow-800';
            case 'BAJA':
                return 'bg-green-100 text-green-800';
            default:
                return 'bg-gray-100 text-gray-800';
        }
    };

    return (
        <span className={`px-2.5 py-1 rounded-full text-xs font-medium ${getPriorityStyle()}`}>
            {prioridad}
        </span>
    );
};
