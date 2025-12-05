import React, { useState } from 'react';
import type { LlamadasPorDia } from '../../types';

interface ConversionTrendChartProps {
    data: LlamadasPorDia[];
    className?: string;
}

export const ConversionTrendChart: React.FC<ConversionTrendChartProps> = ({ data, className = '' }) => {
    const [hoveredPoint, setHoveredPoint] = useState<number | null>(null);

    // Calcular tasa de conversión por día
    const conversionData = data.map(d => ({
        fecha: d.fecha,
        tasa: d.totalLlamadas > 0 ? (d.llamadasEfectivas / d.totalLlamadas) * 100 : 0,
        totalLlamadas: d.totalLlamadas,
        llamadasEfectivas: d.llamadasEfectivas
    }));

    // FIXED: Prevent NaN when no data is available
    const tasaPromedio = conversionData.length > 0
        ? conversionData.reduce((sum, d) => sum + d.tasa, 0) / conversionData.length
        : 0;

    // SVG dimensions
    const width = 475;
    const height = 150;
    const padding = { top: 10, right: 10, bottom: 10, left: 10 };
    const chartWidth = width - padding.left - padding.right;
    const chartHeight = height - padding.top - padding.bottom;

    // Calculate min and max values for scaling
    const maxTasa = Math.max(...conversionData.map(d => d.tasa), 100);
    const minTasa = 0;

    // Helper function to map data to SVG coordinates
    const getX = (index: number) => {
        if (conversionData.length <= 1) return padding.left + chartWidth / 2;
        return padding.left + (index / (conversionData.length - 1)) * chartWidth;
    };

    const getY = (tasa: number) => {
        const normalized = (tasa - minTasa) / (maxTasa - minTasa);
        return padding.top + chartHeight - (normalized * chartHeight);
    };

    // Generate path data for the line
    const generateLinePath = () => {
        if (conversionData.length === 0) return '';

        return conversionData.map((d, i) => {
            const x = getX(i);
            const y = getY(d.tasa);
            return i === 0 ? `M ${x} ${y}` : `L ${x} ${y}`;
        }).join(' ');
    };

    // Generate path data for the area (fill)
    const generateAreaPath = () => {
        if (conversionData.length === 0) return '';

        const linePath = generateLinePath();
        const lastX = getX(conversionData.length - 1);
        const firstX = getX(0);
        const bottomY = padding.top + chartHeight;

        return `${linePath} L ${lastX} ${bottomY} L ${firstX} ${bottomY} Z`;
    };

    // Generate grid lines (horizontal)
    const gridLines = [0, 25, 50, 75, 100].map(value => ({
        value,
        y: getY(value)
    }));

    // Format date for display
    const formatDate = (dateStr: string) => {
        const date = new Date(dateStr);
        return `${date.getDate().toString().padStart(2, '0')}/${(date.getMonth() + 1).toString().padStart(2, '0')}`;
    };

    // Generate X-axis labels (show every N days based on data length)
    const getXAxisLabels = () => {
        if (conversionData.length === 0) return [];

        const maxLabels = 6;
        const step = Math.ceil(conversionData.length / maxLabels);

        return conversionData
            .map((d, i) => ({ ...d, index: i }))
            .filter((_, i) => i % step === 0 || i === conversionData.length - 1)
            .map(d => ({
                label: formatDate(d.fecha),
                x: getX(d.index)
            }));
    };

    const xAxisLabels = getXAxisLabels();

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

            {conversionData.length === 0 ? (
                <div className="flex items-center justify-center min-h-[180px] text-gray-400">
                    No hay datos disponibles
                </div>
            ) : (
                <div className="flex min-h-[180px] flex-1 flex-col gap-2 py-4 relative">
                    <svg
                        fill="none"
                        height="100%"
                        preserveAspectRatio="none"
                        viewBox={`0 0 ${width} ${height}`}
                        width="100%"
                        xmlns="http://www.w3.org/2000/svg"
                    >
                        <defs>
                            <linearGradient
                                gradientUnits="userSpaceOnUse"
                                id="line-chart-gradient"
                                x1={width / 2}
                                x2={width / 2}
                                y1={padding.top}
                                y2={height - padding.bottom}
                            >
                                <stop stopColor="#3b82f6" stopOpacity="0.2" />
                                <stop offset="1" stopColor="#3b82f6" stopOpacity="0" />
                            </linearGradient>
                        </defs>

                        {/* Grid lines */}
                        {gridLines.map((line, i) => (
                            <line
                                key={i}
                                x1={padding.left}
                                y1={line.y}
                                x2={width - padding.right}
                                y2={line.y}
                                stroke="#e5e7eb"
                                strokeWidth="1"
                                strokeDasharray="4 4"
                            />
                        ))}

                        {/* Area fill */}
                        <path
                            d={generateAreaPath()}
                            fill="url(#line-chart-gradient)"
                        />

                        {/* Line */}
                        <path
                            d={generateLinePath()}
                            stroke="#3b82f6"
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth="3"
                            fill="none"
                        />

                        {/* Data points */}
                        {conversionData.map((d, i) => (
                            <g key={i}>
                                <circle
                                    cx={getX(i)}
                                    cy={getY(d.tasa)}
                                    r={hoveredPoint === i ? 6 : 4}
                                    fill="#3b82f6"
                                    stroke="white"
                                    strokeWidth="2"
                                    style={{ cursor: 'pointer', transition: 'r 0.2s' }}
                                    onMouseEnter={() => setHoveredPoint(i)}
                                    onMouseLeave={() => setHoveredPoint(null)}
                                />
                                {hoveredPoint === i && (
                                    <g>
                                        {/* Tooltip background */}
                                        <rect
                                            x={getX(i) - 50}
                                            y={getY(d.tasa) - 45}
                                            width="100"
                                            height="35"
                                            fill="rgba(0, 0, 0, 0.8)"
                                            rx="4"
                                        />
                                        {/* Tooltip text */}
                                        <text
                                            x={getX(i)}
                                            y={getY(d.tasa) - 30}
                                            textAnchor="middle"
                                            fill="white"
                                            fontSize="10"
                                            fontWeight="bold"
                                        >
                                            {formatDate(d.fecha)}
                                        </text>
                                        <text
                                            x={getX(i)}
                                            y={getY(d.tasa) - 18}
                                            textAnchor="middle"
                                            fill="white"
                                            fontSize="12"
                                            fontWeight="bold"
                                        >
                                            {d.tasa.toFixed(1)}%
                                        </text>
                                    </g>
                                )}
                            </g>
                        ))}
                    </svg>

                    {/* X-axis labels */}
                    <div className="flex justify-between px-2 relative" style={{ marginTop: '-8px' }}>
                        {xAxisLabels.map((label, index) => (
                            <p
                                key={index}
                                className="text-gray-500 text-xs font-bold"
                                style={{
                                    position: 'absolute',
                                    left: `${(label.x / width) * 100}%`,
                                    transform: 'translateX(-50%)'
                                }}
                            >
                                {label.label}
                            </p>
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
};
