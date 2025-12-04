import React from 'react';
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

interface TrendData {
    fecha: string;
    cantidad: number;
}

interface TrendChartProps {
    data: TrendData[];
}

export const TrendChartComponent: React.FC<TrendChartProps> = ({ data }) => {
    if (!data || data.length === 0) {
        return <div className="text-gray-500 text-center py-10">No hay datos de tendencia disponibles.</div>;
    }

    return (
        <div className="h-[300px] w-full bg-white p-4 rounded-lg shadow-sm border border-gray-100 flex flex-col">
            <h3 className="text-lg font-semibold text-gray-800 mb-4">Tendencia de Participación</h3>
            <div className="w-full h-[250px] min-w-0">
                <ResponsiveContainer width="100%" height={250}>
                    <AreaChart
                        data={data}
                        margin={{
                            top: 10,
                            right: 30,
                            left: 0,
                            bottom: 0,
                        }}
                    >
                        <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E5E7EB" />
                        <XAxis
                            dataKey="fecha"
                            axisLine={false}
                            tickLine={false}
                            tick={{ fill: '#6B7280', fontSize: 12 }}
                            dy={10}
                        />
                        <YAxis
                            axisLine={false}
                            tickLine={false}
                            tick={{ fill: '#6B7280', fontSize: 12 }}
                        />
                        <Tooltip
                            contentStyle={{ backgroundColor: '#fff', borderRadius: '8px', border: '1px solid #E5E7EB', boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)' }}
                            itemStyle={{ color: '#1F2937' }}
                        />
                        <Area
                            type="monotone"
                            dataKey="cantidad"
                            stroke="#3B82F6"
                            fill="#EFF6FF"
                            strokeWidth={2}
                        />
                    </AreaChart>
                </ResponsiveContainer>
            </div>
        </div>
    );
};
