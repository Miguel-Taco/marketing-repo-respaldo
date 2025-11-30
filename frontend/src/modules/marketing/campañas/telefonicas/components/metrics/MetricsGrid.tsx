import React from 'react';
import type { MetricasCampania } from '../../types';
import { StatsCard } from './StatsCard';
import { formatDuration, formatNumber } from '../../utils/metricsUtils';

interface MetricsGridProps {
    metricas: MetricasCampania;
}

export const MetricsGrid: React.FC<MetricsGridProps> = ({ metricas }) => {
    const tasaContacto = metricas.tasaContactoGlobal;

    return (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 p-4 mt-6">
            <StatsCard
                title="Llamadas realizadas"
                value={formatNumber(metricas.totalLlamadas)}
            />
            <StatsCard
                title="Contactos efectivos"
                value={formatNumber(metricas.leadsContactados)}
            />
            <StatsCard
                title="Duración media"
                value={formatDuration(metricas.duracionPromedio)}
            />
            <StatsCard
                title="Tasa de contacto"
                value={`${tasaContacto.toFixed(1)}%`}
            />
        </div>
    );
};
