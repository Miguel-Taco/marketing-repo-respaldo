import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { telemarketingApi } from '../services/telemarketingApi';
import type { MetricasCampania } from '../types';

// Componentes
import { MetricsHeader } from '../components/metrics/MetricsHeader';
import { MetricsGrid } from '../components/metrics/MetricsGrid';
import { CallsByDayChart } from '../components/metrics/CallsByDayChart';
import { ResultsDistributionChart } from '../components/metrics/ResultsDistributionChart';
import { ConversionTrendChart } from '../components/metrics/ConversionTrendChart';
import { AgentPerformanceTable } from '../components/metrics/AgentPerformanceTable';

export const CampaignMetricsPage: React.FC = () => {
    const { id } = useParams<{ id: string }>();
    const [metricas, setMetricas] = useState<MetricasCampania | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [diasFiltro, setDiasFiltro] = useState<number>(30);

    useEffect(() => {
        if (id) {
            loadMetricas();
        }
    }, [id, diasFiltro]);

    const loadMetricas = async () => {
        try {
            setLoading(true);
            setError(null);
            const data = await telemarketingApi.getMetricasCampaniaCompletas(
                parseInt(id!),
                diasFiltro
            );
            setMetricas(data);
        } catch (err) {
            console.error('Error cargando métricas:', err);
            setError('Error al cargar las métricas de la campaña');
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <div className="flex items-center justify-center h-full">
                <div className="text-center">
                    <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
                    <p className="mt-4 text-gray-600">Cargando métricas...</p>
                </div>
            </div>
        );
    }

    if (error || !metricas) {
        return (
            <div className="flex items-center justify-center h-full">
                <div className="text-center">
                    <span className="material-symbols-outlined text-6xl text-red-500">error</span>
                    <p className="mt-4 text-xl font-semibold text-gray-800">
                        {error || 'No se pudieron cargar las métricas'}
                    </p>
                    <button
                        onClick={loadMetricas}
                        className="mt-4 px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
                    >
                        Reintentar
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="flex flex-col h-full overflow-y-auto">
            <div className="flex flex-1 justify-center py-5 sm:px-6 md:px-10 lg:px-20 xl:px-20">
                <div className="layout-content-container flex flex-col max-w-7xl flex-1 w-full">

                    {/* Header con filtros */}
                    <MetricsHeader
                        diasFiltro={diasFiltro}
                        onDiasChange={setDiasFiltro}
                        onRefresh={loadMetricas}
                    />

                    {/* Grid de stats principales */}
                    <MetricsGrid metricas={metricas} />

                    {/* Gráficos */}
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4 p-4 mt-6">
                        <CallsByDayChart data={metricas.llamadasPorDia} />
                        <ResultsDistributionChart data={metricas.distribucionResultados} />
                        <ConversionTrendChart
                            data={metricas.llamadasPorDia}
                            className="md:col-span-2"
                        />
                    </div>

                    {/* Tabla de rendimiento por agente */}
                    <AgentPerformanceTable agentes={metricas.rendimientoPorAgente} />
                </div>
            </div>
        </div>
    );
};
