import React from 'react';
import { MetricasMailing } from '../types/mailing.types';

interface MetricsPanelProps {
    metricas: MetricasMailing | null;
    loading?: boolean;
    ultimaActualizacion?: string;
}

export const MetricsPanel: React.FC<MetricsPanelProps> = ({ 
    metricas, 
    loading = false,
    ultimaActualizacion 
}) => {
    if (!metricas) {
        return null;
    }

    // Formatear fecha de última actualización
    const formatearFecha = (fecha?: string) => {
        if (!fecha) return new Date().toLocaleString('es-ES');
        const d = new Date(fecha);
        return d.toLocaleString('es-ES');
    };

    const MetricRow = ({ 
        label, 
        valor, 
        porcentaje 
    }: {
        label: string;
        valor: number;
        porcentaje?: number;
    }) => (
        <tr className="border-b border-gray-200 hover:bg-gray-50">
            <td className="px-6 py-4 text-sm text-gray-700 font-medium">{label}</td>
            <td className="px-6 py-4 text-sm text-gray-900 font-semibold text-center">{valor.toLocaleString('es-ES')}</td>
            <td className="px-6 py-4 text-sm text-gray-600 text-center">
                {porcentaje !== undefined 
                    ? `${porcentaje.toFixed(1)}%` 
                    : '-'
                }
            </td>
        </tr>
    );

    return (
        <div className="bg-white rounded-lg shadow-sm border border-separator p-6">
            {/* Título */}
            <h3 className="text-lg font-semibold text-dark mb-6">Resumen de Rendimiento</h3>

            {/* Tabla */}
            <div className="overflow-x-auto">
                <table className="w-full text-left">
                    <thead className="bg-table-header border-b-2 border-separator">
                        <tr>
                            <th className="px-6 py-3 text-sm font-semibold text-dark tracking-wide">Métrica</th>
                            <th className="px-6 py-3 text-sm font-semibold text-dark tracking-wide text-center">Total</th>
                            <th className="px-6 py-3 text-sm font-semibold text-dark tracking-wide text-center">Porcentaje</th>
                        </tr>
                    </thead>
                    <tbody>
                        <MetricRow 
                            label="Enviados" 
                            valor={metricas.enviados}
                            porcentaje={100}
                        />
                        <MetricRow 
                            label="Rebotes" 
                            valor={metricas.rebotes}
                            porcentaje={metricas.enviados > 0 ? (metricas.rebotes / metricas.enviados) * 100 : 0}
                        />
                        <MetricRow 
                            label="Entregados" 
                            valor={metricas.entregados}
                            porcentaje={metricas.enviados > 0 ? (metricas.entregados / metricas.enviados) * 100 : 0}
                        />
                        <MetricRow 
                            label="Aperturas (Únicas)" 
                            valor={metricas.aperturas}
                            porcentaje={metricas.tasaApertura}
                        />
                        <MetricRow 
                            label="Clics (Únicos)" 
                            valor={metricas.clics}
                            porcentaje={metricas.tasaClics}
                        />
                        <MetricRow 
                            label="Bajas" 
                            valor={metricas.bajas}
                            porcentaje={metricas.tasaBajas}
                        />
                    </tbody>
                </table>
            </div>

            {/* Pie: Última actualización */}
            <div className="mt-6 pt-4 border-t border-gray-200 text-xs text-gray-500 text-center">
                Última actualización: {formatearFecha(ultimaActualizacion)}
            </div>
        </div>
    );
};