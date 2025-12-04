import React from 'react';
import type { LlamadasPorDia } from '../../types';

interface ConversionTrendChartProps {
    data: LlamadasPorDia[];
    className?: string;
}

export const ConversionTrendChart: React.FC<ConversionTrendChartProps> = ({ data, className = '' }) => {
    // Calcular tasa de conversión por día
    const conversionData = data.map(d => ({
        fecha: d.fecha,
        tasa: d.totalLlamadas > 0 ? (d.llamadasEfectivas / d.totalLlamadas) * 100 : 0
    }));

    // FIXED: Prevent NaN when no data is available
    const tasaPromedio = conversionData.length > 0
        ? conversionData.reduce((sum, d) => sum + d.tasa, 0) / conversionData.length
        : 0;

    // Agrupar por semanas para el eje X
    const semanas = Math.ceil(data.length / 7);
    const semanasLabels = Array.from({ length: Math.min(semanas, 4) }, (_, i) => `Sem ${i + 1}`);

    return (
        <div className={`flex min-w-72 flex-1 flex-col gap-2 rounded-lg bg-white p-6 border border-gray-200/50 shadow-sm ${className}`}>
            <p className="text-base font-medium text-gray-600">
                Evolución de la efectividad
            </p>
            <div className="flex items-baseline gap-2">
                <p className="tracking-light text-4xl font-bold truncate text-gray-900">
                    {tasaPromedio.toFixed(1)}%
                </p>
                <div className="flex gap-1">
                    <p className="text-gray-500 text-sm font-normal">
                        Promedio últimos {data.length} días
                    </p>
                </div>
            </div>

            <div className="flex min-h-[180px] flex-1 flex-col gap-8 py-4">
                <svg
                    fill="none"
                    height="100%"
                    preserveAspectRatio="none"
                    viewBox="0 0 475 150"
                    width="100%"
                    xmlns="http://www.w3.org/2000/svg"
                >
                    {/* Gradiente de relleno */}
                    <defs>
                        <linearGradient
                            gradientUnits="userSpaceOnUse"
                            id="line-chart-gradient"
                            x1="236"
                            x2="236"
                            y1="1"
                            y2="149"
                        >
                            <stop stopColor="#3b82f6" stopOpacity="0.2" />
                            <stop offset="1" stopColor="#3b82f6" stopOpacity="0" />
                        </linearGradient>
                    </defs>

                    {/* Área de relleno */}
                    <path
                        d="M0 109C18.1538 109 18.1538 21 36.3077 21C54.4615 21 54.4615 41 72.6154 41C90.7692 41 90.7692 93 108.923 93C127.077 93 127.077 33 145.231 33C163.385 33 163.385 101 181.538 101C199.692 101 199.692 61 217.846 61C236 61 236 45 254.154 45C272.308 45 272.308 121 290.462 121C308.615 121 308.615 149 326.769 149C344.923 149 344.923 1 363.077 1C381.231 1 381.231 81 399.385 81C417.538 81 417.538 129 435.692 129C453.846 129 453.846 25 472 25V150H0V109Z"
                        fill="url(#line-chart-gradient)"
                    />

                    {/* Línea */}
                    <path
                        d="M0 109C18.1538 109 18.1538 21 36.3077 21C54.4615 21 54.4615 41 72.6154 41C90.7692 41 90.7692 93 108.923 93C127.077 93 127.077 33 145.231 33C163.385 33 163.385 101 181.538 101C199.692 101 199.692 61 217.846 61C236 61 236 45 254.154 45C272.308 45 272.308 121 290.462 121C308.615 121 308.615 149 326.769 149C344.923 149 344.923 1 363.077 1C381.231 1 381.231 81 399.385 81C417.538 81 417.538 129 435.692 129C453.846 129 453.846 25 472 25"
                        stroke="#3b82f6"
                        strokeLinecap="round"
                        strokeWidth="3"
                    />
                </svg>

                <div className="flex justify-around">
                    {semanasLabels.map((label, index) => (
                        <p key={index} className="text-gray-500 text-xs font-bold">
                            {label}
                        </p>
                    ))}
                </div>
            </div>
        </div>
    );
};
