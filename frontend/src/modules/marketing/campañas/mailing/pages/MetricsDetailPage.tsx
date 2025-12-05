import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { mailingApi } from '../services/mailing.api';
import { CampanaMailing, MetricasMailing } from '../types/mailing.types';
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
        <div className="space-y-8">
            {/* Header */}
            <div className="flex items-start justify-between">
                <div>
                    <h1 className="text-3xl font-bold text-gray-900">
                        Reporte de Métricas: {campaign.nombre}
                    </h1>
                </div>
                <button
                    onClick={() => navigate('/emailing')}
                    className="flex items-center gap-2 px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-lg transition font-medium"
                >
                    <span className="material-symbols-outlined">arrow_back</span>
                    <span>Volver a la lista</span>
                </button>
            </div>

            {/* Stats Cards - 3 Columnas */}
            {metricas && (
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                    {/* Tasa de Apertura */}
                    <div className="bg-white rounded-lg p-6 shadow-sm border border-gray-200">
                        <p className="text-gray-600 text-sm font-medium mb-2">Tasa de Apertura</p>
                        <p className="text-5xl font-bold text-blue-600">
                            {metricas.tasaApertura.toFixed(1)}%
                        </p>
                    </div>

                    {/* Tasa de Clics (CTR) */}
                    <div className="bg-white rounded-lg p-6 shadow-sm border border-gray-200">
                        <p className="text-gray-600 text-sm font-medium mb-2">Tasa de Clics (CTR)</p>
                        <p className="text-5xl font-bold text-blue-600">
                            {metricas.tasaClics.toFixed(1)}%
                        </p>
                    </div>

                    {/* Bajas */}
                    <div className="bg-white rounded-lg p-6 shadow-sm border border-gray-200">
                        <p className="text-gray-600 text-sm font-medium mb-2">Bajas</p>
                        <p className="text-5xl font-bold text-gray-900">
                            {metricas.bajas}
                        </p>
                    </div>
                </div>
            )}

            {/* Tabla - Resumen de Rendimiento */}
            {metricas && (
                <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
                    <div className="p-6 border-b border-gray-200">
                        <h3 className="text-lg font-semibold text-gray-900">Resumen de Rendimiento</h3>
                    </div>

                    <div className="overflow-x-auto">
                        <table className="w-full text-left">
                            <thead className="bg-gray-100">
                                <tr>
                                    <th className="px-6 py-3 text-sm font-semibold text-gray-900">Métrica</th>
                                    <th className="px-6 py-3 text-sm font-semibold text-gray-900 text-center">Total</th>
                                    <th className="px-6 py-3 text-sm font-semibold text-gray-900 text-center">Porcentaje</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-gray-200">
                                <tr className="hover:bg-gray-50">
                                    <td className="px-6 py-4 text-sm text-gray-700 font-medium">Enviados</td>
                                    <td className="px-6 py-4 text-sm text-gray-900 text-center font-semibold">
                                        {metricas.enviados.toLocaleString('es-ES')}
                                    </td>
                                    <td className="px-6 py-4 text-sm text-gray-600 text-center">100.0%</td>
                                </tr>
                                <tr className="hover:bg-gray-50">
                                    <td className="px-6 py-4 text-sm text-gray-700 font-medium">Rebotes</td>
                                    <td className="px-6 py-4 text-sm text-gray-900 text-center font-semibold">
                                        {metricas.rebotes}
                                    </td>
                                    <td className="px-6 py-4 text-sm text-gray-600 text-center">
                                        {metricas.enviados > 0 
                                            ? ((metricas.rebotes / metricas.enviados) * 100).toFixed(1) 
                                            : '0.0'}%
                                    </td>
                                </tr>
                                <tr className="hover:bg-gray-50">
                                    <td className="px-6 py-4 text-sm text-gray-700 font-medium">Entregados</td>
                                    <td className="px-6 py-4 text-sm text-gray-900 text-center font-semibold">
                                        {metricas.entregados.toLocaleString('es-ES')}
                                    </td>
                                    <td className="px-6 py-4 text-sm text-gray-600 text-center">
                                        {metricas.enviados > 0 
                                            ? ((metricas.entregados / metricas.enviados) * 100).toFixed(1) 
                                            : '0.0'}%
                                    </td>
                                </tr>
                                <tr className="hover:bg-gray-50">
                                    <td className="px-6 py-4 text-sm text-gray-700 font-medium">Aperturas (Únicas)</td>
                                    <td className="px-6 py-4 text-sm text-gray-900 text-center font-semibold">
                                        {metricas.aperturas.toLocaleString('es-ES')}
                                    </td>
                                    <td className="px-6 py-4 text-sm text-gray-600 text-center">
                                        {metricas.tasaApertura.toFixed(1)}%
                                    </td>
                                </tr>
                                <tr className="hover:bg-gray-50">
                                    <td className="px-6 py-4 text-sm text-gray-700 font-medium">Clics (Únicos)</td>
                                    <td className="px-6 py-4 text-sm text-gray-900 text-center font-semibold">
                                        {metricas.clics.toLocaleString('es-ES')}
                                    </td>
                                    <td className="px-6 py-4 text-sm text-gray-600 text-center">
                                        {metricas.tasaClics.toFixed(1)}%
                                    </td>
                                </tr>
                                <tr className="hover:bg-gray-50">
                                    <td className="px-6 py-4 text-sm text-gray-700 font-medium">Bajas</td>
                                    <td className="px-6 py-4 text-sm text-gray-900 text-center font-semibold">
                                        {metricas.bajas}
                                    </td>
                                    <td className="px-6 py-4 text-sm text-gray-600 text-center">
                                        {metricas.tasaBajas.toFixed(1)}%
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>

                    {/* Footer - Última actualización */}
                    <div className="px-6 py-4 bg-gray-50 border-t border-gray-200 text-xs text-gray-500 text-center">
                        Última actualización: {new Date().toLocaleString('es-ES')}
                    </div>
                </div>
            )}
        </div>
    );
};