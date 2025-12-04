import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { mailingApi } from '../services/mailing.api';
import { CampanaMailing, MetricasMailing } from '../types/mailing.types';
import { MailingStatsCards } from '../components/MailingStatsCards';
import { MetricsPanel } from '../components/MetricsPanel';
import { LoadingSpinner } from '../../../../../shared/components/ui/LoadingSpinner';
import { useToast } from '../../../../../shared/components/ui/Toast';

export const MetricsDetailPage: React.FC = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { showToast } = useToast();

    const [campaign, setCampaign] = useState<CampanaMailing | null>(null);
    const [metricas, setMetricas] = useState<MetricasMailing | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        loadMetricas();
        // Recargar cada 30 segundos para actualizar métricas en tiempo real
        const interval = setInterval(() => {
            loadMetricas();
        }, 30000);

        return () => clearInterval(interval);
    }, [id]);

    const loadMetricas = async () => {
        if (!id) return;

        try {
            setLoading(true);
            setError(null);

            // Cargar detalle de campaña
            const campanaData = await mailingApi.obtenerDetalle(parseInt(id));
            setCampaign(campanaData);

            // Cargar métricas
            const metricasData = await mailingApi.obtenerMetricas(parseInt(id));
            setMetricas(metricasData);

        } catch (err: any) {
            const errorMsg = err.message || 'Error al cargar métricas';
            setError(errorMsg);
            showToast(errorMsg, 'error');
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <div className="flex items-center justify-center min-h-screen">
                <div className="text-center">
                    <LoadingSpinner size="lg" />
                    <p className="mt-4 text-gray-600">Cargando métricas...</p>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="bg-red-50 border border-red-200 p-8 rounded-lg">
                <div className="flex items-start gap-3">
                    <span className="material-symbols-outlined text-red-600 text-2xl">error</span>
                    <div className="flex-1">
                        <h3 className="text-red-800 font-semibold text-lg">Error al cargar métricas</h3>
                        <p className="text-red-700 mt-2">{error}</p>
                        <button
                            onClick={() => navigate('/emailing')}
                            className="mt-4 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition font-medium"
                        >
                            Volver a la lista
                        </button>
                    </div>
                </div>
            </div>
        );
    }

    if (!campaign) {
        return (
            <div className="bg-yellow-50 border border-yellow-200 p-8 rounded-lg">
                <p className="text-yellow-800 font-semibold">Campaña no encontrada</p>
                <button
                    onClick={() => navigate('/emailing')}
                    className="mt-4 px-4 py-2 bg-yellow-600 text-white rounded-lg hover:bg-yellow-700 transition font-medium"
                >
                    Volver a la lista
                </button>
            </div>
        );
    }

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex justify-between items-center mb-8">
                <div>
                    <h1 className="text-3xl font-bold text-dark">
                        Reporte de Métricas: {campaign.nombre}
                    </h1>
                    <p className="text-gray-500 mt-1">Análisis detallado del rendimiento de la campaña</p>
                </div>
                <button
                    onClick={() => navigate('/emailing')}
                    className="flex items-center gap-2 px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-lg transition"
                >
                    <span className="material-symbols-outlined">arrow_back</span>
                    <span className="font-medium">Volver a la lista</span>
                </button>
            </div>

            {/* Stats Cards */}
            <MailingStatsCards metricas={metricas} loading={loading} />

            {/* Metrics Panel / Summary Table */}
            <MetricsPanel 
                metricas={metricas} 
                loading={loading}
                ultimaActualizacion={metricas?.id ? new Date().toISOString() : undefined}
            />

            {/* Info Adicional */}
            <div className="bg-blue-50 border border-blue-200 rounded-lg p-6">
                <div className="flex gap-3">
                    <span className="material-symbols-outlined text-blue-600 text-2xl flex-shrink-0">info</span>
                    <div className="text-sm text-blue-800">
                        <p className="font-semibold">💡 Información útil</p>
                        <ul className="mt-2 space-y-1 text-blue-700">
                            <li>• <strong>Tasa de Apertura:</strong> Porcentaje de destinatarios que abrieron el email</li>
                            <li>• <strong>Tasa de Clics (CTR):</strong> Porcentaje de destinatarios que hicieron clic en el CTA</li>
                            <li>• <strong>Bajas:</strong> Cantidad de personas que se dieron de baja de la lista</li>
                            <li>• Las métricas se actualizan automáticamente cada 30 segundos</li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    );
};