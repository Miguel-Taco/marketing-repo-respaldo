import React from 'react';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Cell } from 'recharts';

interface ScaleQuestionProps {
    data: {
        etiqueta: string;
        valor: number;
        metadata: {
            histograma: Record<string, number>;
            moda: number;
            desviacionEstandar: number;
            totalRespuestas: number;
        };
    };
}

export const ScaleQuestionComponent: React.FC<ScaleQuestionProps> = ({ data }) => {
    const histogramData = Object.entries(data.metadata.histograma || {}).map(([key, value]) => ({
        name: key,
        value: value
    })).sort((a, b) => parseInt(a.name) - parseInt(b.name));

    const CustomTooltip = ({ active, payload }: any) => {
        if (active && payload && payload.length) {
            return (
                <div className="bg-white p-2 border border-gray-200 shadow-sm rounded text-xs">
                    <p className="text-gray-700">{`Cantidad: ${payload[0].value}`}</p>
                </div>
            );
        }
        return null;
    };

    return (
        <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-100 flex flex-col h-full">
            <h4 className="text-md font-medium text-gray-900 mb-2">{data.etiqueta}</h4>

            <div className="flex items-end gap-2 mb-6">
                <span className="text-5xl font-bold text-blue-600">{data.valor.toFixed(1)}</span>
                <span className="text-gray-400 text-lg mb-1">/ 5.0</span>
            </div>

            <div className="w-full h-[150px] min-w-0">
                <p className="text-xs text-gray-500 mb-2">Distribución de votos</p>
                <ResponsiveContainer width="100%" height={120}>
                    <BarChart data={histogramData}>
                        <XAxis
                            dataKey="name"
                            axisLine={false}
                            tickLine={false}
                            tick={{ fontSize: 12 }}
                        />
                        <Tooltip cursor={{ fill: 'transparent' }} content={<CustomTooltip />} />
                        <Bar dataKey="value" radius={[4, 4, 0, 0]}>
                            {histogramData.map((entry, index) => (
                                <Cell key={`cell-${index}`} fill={entry.name === String(data.metadata.moda) ? '#3B82F6' : '#D1D5DB'} />
                            ))}
                        </Bar>
                    </BarChart>
                </ResponsiveContainer>
            </div>

            <div className="mt-4 pt-4 border-t border-gray-50 flex justify-between text-xs text-gray-500">
                <span>Total: {data.metadata.totalRespuestas}</span>
                <span>Desv. Est: {data.metadata.desviacionEstandar.toFixed(2)}</span>
            </div>
        </div>
    );
};
