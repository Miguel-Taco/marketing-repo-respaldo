import React from 'react';
import { MetricasMailing } from '../types/mailing.types';

interface MailingStatsCardsProps {
    metricas: MetricasMailing | null;
    loading?: boolean;
}

export const MailingStatsCards: React.FC<MailingStatsCardsProps> = ({ metricas, loading = false }) => {
    const StatCard = ({ 
        title, 
        value, 
        unit = '', 
        icon,
        bgColor = 'bg-blue-50',
        textColor = 'text-blue-600'
    }: {
        title: string;
        value: string | number;
        unit?: string;
        icon?: string;
        bgColor?: string;
        textColor?: string;
    }) => (
        <div className={`${bgColor} rounded-lg p-6 flex flex-col items-center justify-center min-h-40`}>
            {icon && <span className={`material-symbols-outlined text-4xl ${textColor} mb-3`}>{icon}</span>}
            <p className="text-gray-600 text-sm font-medium mb-2">{title}</p>
            <p className={`text-4xl font-bold ${textColor}`}>
                {loading ? '...' : value}
                {unit && <span className="text-lg ml-1">{unit}</span>}
            </p>
        </div>
    );

    if (!metricas) {
        return (
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
                <StatCard title="Tasa de Apertura" value="--" unit="%" />
                <StatCard title="Tasa de Clics (CTR)" value="--" unit="%" />
                <StatCard title="Bajas" value="--" />
            </div>
        );
    }

    return (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
            {/* Tasa de Apertura */}
            <StatCard 
                title="Tasa de Apertura"
                value={metricas.tasaApertura.toFixed(1)}
                unit="%"
                icon="mail_open"
                bgColor="bg-blue-50"
                textColor="text-blue-600"
            />

            {/* Tasa de Clics (CTR) */}
            <StatCard 
                title="Tasa de Clics (CTR)"
                value={metricas.tasaClics.toFixed(1)}
                unit="%"
                icon="touch_app"
                bgColor="bg-green-50"
                textColor="text-green-600"
            />

            {/* Bajas */}
            <StatCard 
                title="Bajas"
                value={metricas.bajas}
                icon="unsubscribe"
                bgColor="bg-red-50"
                textColor="text-red-600"
            />
        </div>
    );
};