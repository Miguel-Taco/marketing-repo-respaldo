import React from 'react';
import type { LlamadasPorDia } from '../../types';
import { getDayName, formatNumber } from '../../utils/metricsUtils';

interface CallsByDayChartProps {
    data: LlamadasPorDia[];
}

export const CallsByDayChart: React.FC<CallsByDayChartProps> = ({ data }) => {
    const last7Days = data.slice(-7);
    const maxCalls = Math.max(...last7Days.map(d => d.totalLlamadas), 1);

    const getBarHeight = (calls: number) => {
        return (calls / maxCalls) * 100;
    };

    const isToday = (fecha: string) => {
        const today = new Date().toISOString().split('T')[0];
        return fecha === today;
    };

    return (
        <div className="flex flex-col gap-2 rounded-lg bg-white p-6 border border-gray-200/50 shadow-sm">
            <p className="text-base font-medium text-gray-600">
                Llamadas por día
            </p>
            <div className="flex items-baseline gap-2">
                <p className="tracking-light text-4xl font-bold truncate text-gray-900">
                    {formatNumber(last7Days.reduce((sum, d) => sum + d.totalLlamadas, 0))}
                </p>
                <p className="text-gray-500 text-sm font-normal">
                    Últimos 7 días
                </p>
            </div>

            <div className="grid min-h-[220px] grid-flow-col gap-4 items-end justify-items-center px-3 pt-4">
                {last7Days.map((day, index) => (
                    <div
                        key={index}
                        className={`w-full rounded-t transition-all ${isToday(day.fecha)
                                ? 'bg-blue-600'
                                : 'bg-blue-600/20'
                            }`}
                        style={{ height: `${getBarHeight(day.totalLlamadas)}%` }}
                        title={`${day.totalLlamadas} llamadas`}
                    />
                ))}
            </div>

            <div className="flex justify-around pt-2">
                {last7Days.map((day, index) => (
                    <p key={index} className="text-gray-500 text-xs font-bold">
                        {getDayName(day.fecha)}
                    </p>
                ))}
            </div>
        </div>
    );
};
