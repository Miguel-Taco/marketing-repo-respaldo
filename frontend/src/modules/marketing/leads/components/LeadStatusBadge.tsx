import React from 'react';
import { Badge } from '../../../../shared/components/ui/Badge';
import { LeadState } from '../types/lead.types';

interface LeadStatusBadgeProps {
    estado: LeadState;
}

export const LeadStatusBadge: React.FC<LeadStatusBadgeProps> = ({ estado }) => {
    const variantMap: Record<LeadState, 'info' | 'success' | 'danger'> = {
        'NUEVO': 'info',
        'CALIFICADO': 'success',
        'DESCARTADO': 'danger'
    };

    return <Badge variant={variantMap[estado]}>{estado}</Badge>;
};
