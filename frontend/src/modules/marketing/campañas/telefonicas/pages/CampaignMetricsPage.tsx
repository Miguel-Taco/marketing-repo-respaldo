import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { telemarketingApi } from '../services/telemarketingApi';
import type { MetricasCampania } from '../types';
import { useCachedCampaignData } from '../context/CampaignCacheContext';
import { LoadingSpinner } from '../../../../../shared/components/ui/LoadingSpinner';
import { LoadingDots } from '../../../../../shared/components/ui/LoadingDots';

// Componentes
import { MetricsHeader } from '../components/metrics/MetricsHeader';
import { MetricsGrid } from '../components/metrics/MetricsGrid';
import { CallsByDayChart } from '../components/metrics/CallsByDayChart';
import { ResultsDistributionChart } from '../components/metrics/ResultsDistributionChart';
import { ConversionTrendChart } from '../components/metrics/ConversionTrendChart';
import { AgentPerformanceTable } from '../components/metrics/AgentPerformanceTable';

export const CampaignMetricsPage: React.FC = () => {
    const { id } = useParams<{ id: string }>();
    const [error, setError] = useState<string | null>(null);
    const [diasFiltro, setDiasFiltro] = useState<number>(30);

    // Usar caché para métricas de campaña
    const { data: metricas, loading, refresh } = useCachedCampaignData<MetricasCampania>(
        id ? Number(id) : undefined,
        'campaignMetrics'
    );

    // Recargar cuando cambie el filtro de días
    useEffect(() => {
        if (id && diasFiltro !== 30) {
            // Si cambia el filtro de días, hacer una recarga manual
            loadMetricasConFiltro();
        }
    }, [diasFiltro]);

    const loadMetricasConFiltro = async () => {
        try {
            setError(null);
            const data = await telemarketingApi.getMetricasCampaniaCompletas(
                parseInt(id!),
                diasFiltro
            );
            // Aquí deberíamos actualizar el caché manualmente, pero por ahora solo mostramos
            // En una implementación completa, agregaríamos un método setCachedData al contexto
        } catch (err) {
            console.error('Error cargando métricas:', err);
            setError('Error al cargar las métricas de la campaña');
        }
    };

    if (loading) {
        return (
            <div className="flex items-center justify-center h-full">
                <div className="flex flex-col items-center gap-4">
                    <LoadingSpinner size="lg" />
                    <LoadingDots text="Cargando métricas" className="text-gray-600 font-medium" />
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
                        onClick={refresh}
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
                        onRefresh={refresh}
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
