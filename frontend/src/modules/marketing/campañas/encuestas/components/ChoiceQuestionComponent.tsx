import React from 'react';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Cell, PieChart, Pie, Legend } from 'recharts';

interface ChoiceQuestionProps {
    data: {
        etiqueta: string;
        metadata: {
            distribucion: Record<string, number>;
            porcentajes?: Record<string, number>;
            totalRespuestas?: number;
            totalEncuestadosUnicos?: number;
        };
    };
    type: 'UNICA' | 'MULTIPLE';
}

const COLORS = ['#3B82F6', '#10B981', '#F59E0B', '#EF4444', '#8B5CF6', '#EC4899', '#6366F1'];

export const ChoiceQuestionComponent: React.FC<ChoiceQuestionProps> = ({ data, type }) => {
    const chartData = Object.entries(data.metadata.distribucion || {}).map(([key, value]) => ({
        name: key,
        value: value,
        percentage: data.metadata.porcentajes ? data.metadata.porcentajes[key] : 0
    })).sort((a, b) => b.value - a.value);

    const isMultiple = type === 'MULTIPLE';
    const showDonut = !isMultiple; // Always Donut for single choice

    const renderLegend = () => {
        return (
            <ul className="list-none p-0 m-0">
                {chartData.map((entry, index) => (
                    <li key={`item-${index}`} className="flex items-center mb-4 text-xs text-gray-600">
                        <span
                            className="inline-block w-3 h-3 mr-2 rounded-sm"
                            style={{ backgroundColor: COLORS[index % COLORS.length] }}
                        ></span>
                        <span>{entry.name} ({entry.percentage}%)</span>
                    </li>
                ))}
            </ul>
        );
    };

    const CustomTooltip = ({ active, payload }: any) => {
        if (active && payload && payload.length) {
            const data = payload[0].payload;
            return (
                <div className="bg-white p-2 border border-gray-200 shadow-sm rounded text-xs">
                    <p className="text-gray-700">{`${data.name}: ${data.value}`}</p>
                </div>
            );
        }
        return null;
    };

    return (
        <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-100 flex flex-col h-full">
            <h4 className="text-md font-medium text-gray-900 mb-4">{data.etiqueta}</h4>

            {isMultiple && (
                <div className="mb-4 p-2 bg-yellow-50 text-yellow-700 text-xs rounded border border-yellow-100 flex items-start gap-2">
                    <span className="material-symbols-outlined text-sm">info</span>
                    <span>Selección múltiple: Los porcentajes pueden sumar más del 100%.</span>
                </div>
            )}

            <div className="w-full h-[250px] min-w-0">
                <ResponsiveContainer width="100%" height={250}>
                    {showDonut ? (
                        <PieChart>
                            <Pie
                                data={chartData}
                                cx="50%"
                                cy="50%"
                                innerRadius={60}
                                outerRadius={80}
                                fill="#8884d8"
                                paddingAngle={5}
                                dataKey="value"
                            >
                                {chartData.map((entry, index) => (
                                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                                ))}
                            </Pie>
                            <Tooltip content={<CustomTooltip />} />
                            <Legend
                                content={renderLegend}
                                layout="vertical"
                                verticalAlign="middle"
                                align="right"
                            />
                        </PieChart>
                    ) : (
                        <BarChart
                            layout="vertical"
                            data={chartData}
                            margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
                        >
                            <XAxis type="number" hide />
                            <YAxis
                                dataKey="name"
                                type="category"
                                width={100}
                                tick={{ fontSize: 11 }}
                                interval={0}
                            />
                            <Tooltip cursor={{ fill: 'transparent' }} content={<CustomTooltip />} />
                            <Legend
                                content={renderLegend}
                                layout="vertical"
                                verticalAlign="middle"
                                align="right"
                            />
                            <Bar
                                dataKey="value"
                                radius={[0, 4, 4, 0]}
                                barSize={20}
                                label={(props: any) => {
                                    const { x, y, width, height, payload } = props;
                                    if (!payload || payload.percentage === undefined) return null;

                                    return (
                                        <text
                                            x={x + width + 5}
                                            y={y + height / 2 + 4}
                                            fill="#6B7280"
                                            fontSize={11}
                                            textAnchor="start"
                                        >
                                            {payload.percentage}%
                                        </text>
                                    );
                                }}
                            >
                                {chartData.map((entry, index) => (
                                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                                ))}
                            </Bar>
                        </BarChart>
                    )}
                </ResponsiveContainer>
            </div>

            <div className="mt-4 pt-4 border-t border-gray-50 text-xs text-gray-500 text-right">
                Total: {isMultiple ? data.metadata.totalEncuestadosUnicos : data.metadata.totalRespuestas}
            </div>
        </div>
    );
};
