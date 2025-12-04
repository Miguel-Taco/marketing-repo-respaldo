import React from 'react';
import { MetricasMailing } from '../types/mailing.types';

interface MailingStatsCardsProps {
    metricas: MetricasMailing | null;
    loading?: boolean;
}

export const MailingStatsCards: React.FC<MailingStatsCardsProps> = ({ metricas, loading = false }) => {
    
    // Componente de tarjeta de estadística
    const StatCard = ({ 
        title, 
        value, 
        unit = '', 
        icon,
        bgColor = 'bg-blue-50',
        iconBgColor = 'bg-blue-100',
        textColor = 'text-blue-600'
    }: {
        title: string;
        value: string | number;
        unit?: string;
        icon: React.ReactNode;
        bgColor?: string;
        iconBgColor?: string;
        textColor?: string;
    }) => (
        <div className={`${bgColor} rounded-xl p-6 flex flex-col items-center justify-center min-h-44 transition-all hover:shadow-md`}>
            {/* Icono */}
            <div className={`${iconBgColor} rounded-full p-3 mb-4`}>
                {icon}
            </div>
            
            {/* Título */}
            <p className="text-gray-600 text-sm font-medium mb-2">{title}</p>
            
            {/* Valor */}
            <p className={`text-4xl font-bold ${textColor}`}>
                {loading ? (
                    <span className="animate-pulse">...</span>
                ) : (
                    <>
                        {value}
                        {unit && <span className="text-xl ml-1">{unit}</span>}
                    </>
                )}
            </p>
        </div>
    );

    // Iconos SVG personalizados (más confiables que Material Symbols)
    const MailOpenIcon = () => (
        <svg className="w-8 h-8 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} 
                d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} 
                d="M3 8l9 6 9-6" />
        </svg>
    );

    const ClickIcon = () => (
        <svg className="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} 
                d="M15 15l-2 5L9 9l11 4-5 2zm0 0l5 5M7.188 2.239l.777 2.897M5.136 7.965l-2.898-.777M13.95 4.05l-2.122 2.122m-5.657 5.656l-2.12 2.122" />
        </svg>
    );

    const UnsubscribeIcon = () => (
        <svg className="w-8 h-8 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} 
                d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} 
                d="M9 12l2 2 4-4" />
        </svg>
    );

    // Si no hay métricas, mostrar placeholder
    if (!metricas) {
        return (
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
                <StatCard 
                    title="Tasa de Apertura" 
                    value="--" 
                    unit="%" 
                    icon={<MailOpenIcon />}
                    bgColor="bg-blue-50"
                    iconBgColor="bg-blue-100"
                    textColor="text-blue-600"
                />
                <StatCard 
                    title="Tasa de Clics (CTR)" 
                    value="--" 
                    unit="%" 
                    icon={<ClickIcon />}
                    bgColor="bg-green-50"
                    iconBgColor="bg-green-100"
                    textColor="text-green-600"
                />
                <StatCard 
                    title="Bajas" 
                    value="--" 
                    icon={<UnsubscribeIcon />}
                    bgColor="bg-red-50"
                    iconBgColor="bg-red-100"
                    textColor="text-red-600"
                />
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
                icon={<MailOpenIcon />}
                bgColor="bg-blue-50"
                iconBgColor="bg-blue-100"
                textColor="text-blue-600"
            />

            {/* Tasa de Clics (CTR) */}
            <StatCard 
                title="Tasa de Clics (CTR)"
                value={metricas.tasaClics.toFixed(1)}
                unit="%"
                icon={<ClickIcon />}
                bgColor="bg-green-50"
                iconBgColor="bg-green-100"
                textColor="text-green-600"
            />

            {/* Bajas */}
            <StatCard 
                title="Bajas"
                value={metricas.bajas}
                icon={<UnsubscribeIcon />}
                bgColor="bg-red-50"
                iconBgColor="bg-red-100"
                textColor="text-red-600"
            />
        </div>
    );
};