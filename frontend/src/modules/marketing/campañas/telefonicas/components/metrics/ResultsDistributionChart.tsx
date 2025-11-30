import React from 'react';
import type { ResultadoDistribucion } from '../../types';
import { getBgColorForResult, formatNumber } from '../../utils/metricsUtils';

interface ResultsDistributionChartProps {
    data: Record<string, ResultadoDistribucion>;
}

export const ResultsDistributionChart: React.FC<ResultsDistributionChartProps> = ({ data }) => {
    const resultados = Object.values(data);
    const totalLlamadas = resultados.reduce((sum, r) => sum + r.count, 0);

    // Calcular segmentos para el gráfico circular
    let offset = 0;
    const segments = resultados.map(resultado => {
        const segment = {
            resultado: resultado.resultado,
            nombre: resultado.nombre,
            porcentaje: resultado.porcentaje,
            count: resultado.count,
            offset: offset,
        };
        offset -= resultado.porcentaje;
        return segment;
    });

    return (
        <div className="flex flex-col gap-2 rounded-lg bg-white p-6 border border-gray-200/50 shadow-sm">
            <p className="text-base font-medium text-gray-600">
                Distribución de resultados
            </p>
            <div className="flex items-baseline gap-2">
                <p className="tracking-light text-4xl font-bold truncate text-gray-900">
                    {formatNumber(totalLlamadas)}
                </p>
                <p className="text-gray-500 text-sm font-normal">
                    Llamadas
                </p>
            </div>

            <div className="flex-1 flex items-center justify-center py-4">
                <div className="relative w-40 h-40">
                    <svg className="w-full h-full -rotate-90" viewBox="0 0 36 36">
                        {/* Fondo gris */}
                        <circle
                            className="stroke-current text-gray-200"
                            cx="18"
                            cy="18"
                            r="15.9155"
                            fill="none"
                            strokeWidth="4"
                        />

                        {/* Segmentos de resultados */}
                        {segments.map((segment, index) => (
                            <circle
                                key={index}
                                className={`stroke-current ${segment.resultado === 'CONTACTADO' || segment.resultado === 'INTERESADO'
                                        ? 'text-green-500'
                                        : segment.resultado === 'BUZON'
                                            ? 'text-blue-500'
                                            : segment.resultado === 'NO_CONTESTA'
                                                ? 'text-yellow-500'
                                                : 'text-gray-300'
                                    }`}
                                cx="18"
                                cy="18"
                                r="15.9155"
                                fill="none"
                                strokeWidth="4"
                                strokeDasharray={`${segment.porcentaje} ${100 - segment.porcentaje}`}
                                strokeDashoffset={segment.offset}
                                strokeLinecap="round"
                            />
                        ))}
                    </svg>
                </div>
            </div>

            <div className="grid grid-cols-2 gap-x-4 gap-y-2">
                {resultados.map((resultado, index) => (
                    <div key={index} className="flex items-center gap-2">
                        <div className={`w-3 h-3 rounded-full ${getBgColorForResult(resultado.resultado)}`} />
                        <span className="text-sm font-medium text-gray-700">
                            {resultado.nombre} ({resultado.porcentaje.toFixed(0)}%)
                        </span>
                    </div>
                ))}
            </div>
        </div>
    );
};
