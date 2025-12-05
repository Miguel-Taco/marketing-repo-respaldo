import React, { useEffect, useState } from 'react';
import { telemarketingApi } from '../services/telemarketingApi';
import type { MetricasAgente } from '../types';
import { Button } from '../../../../../shared/components/ui/Button';
import { downloadCSV } from '../../../../../shared/utils/exportUtils';
import { useAuth } from '../../../../../shared/context/AuthContext';
import { LoadingSpinner } from '../../../../../shared/components/ui/LoadingSpinner';
import { LoadingDots } from '../../../../../shared/components/ui/LoadingDots';

export const AgentMetricsPage: React.FC = () => {
    const [metricas, setMetricas] = useState<MetricasAgente | null>(null);
    const [loading, setLoading] = useState(true);

    const { user, hasRole } = useAuth();
    const isAdmin = hasRole('ADMIN');
    const idAgente = user?.agentId;

    useEffect(() => {
        if (idAgente) {
            loadMetricas();
        } else {
            setMetricas(null);
            setLoading(false);
        }
    }, [idAgente]);

    const loadMetricas = async () => {
        try {
            setLoading(true);
            const data = await telemarketingApi.getMetricasGenerales();
            setMetricas(data);
        } catch (error) {
            console.error('Error cargando métricas:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleExport = () => {
        if (!metricas) return;

        const reportData = [
            { Métrica: 'Llamadas Realizadas', Valor: metricas.llamadasRealizadas },
            { Métrica: 'Contactos Efectivos', Valor: metricas.contactosEfectivos },
            { Métrica: 'Duración Promedio (seg)', Valor: metricas.duracionPromedio },
            { Métrica: 'Tasa de Contacto (%)', Valor: metricas.tasaContacto.toFixed(2) },
            { Métrica: 'Llamadas del Mes', Valor: metricas.llamadasMes },
        ];

        downloadCSV(reportData, 'reporte_metricas', [
            { key: 'Métrica', label: 'Métrica' },
            { key: 'Valor', label: 'Valor' }
        ]);
    };

    const formatDuration = (seconds: number) => {
        const mins = Math.floor(seconds / 60);
        const secs = seconds % 60;
        return `${mins}m ${secs}s`;
    };

    if (!isAdmin && !idAgente) {
        return (
            <div className="flex flex-col items-center justify-center h-screen gap-4">
                <h2 className="text-2xl font-bold text-gray-900">No tienes un agente asignado</h2>
                <p className="text-gray-600">Las métricas personales estarán disponibles cuando el administrador te asigne a una campaña.</p>
            </div>
        );
    }

    if (loading) {
        return (
            <div className="flex flex-col items-center justify-center h-screen gap-4">
                <LoadingSpinner size="lg" />
                <LoadingDots text="Cargando métricas" className="text-gray-600 font-medium" />
            </div>
        );
    }

    if (!metricas) {
        return <div className="p-6">No hay métricas disponibles</div>;
    }

    return (
        <div className="flex flex-col h-full overflow-y-auto p-6">
            <div className="flex flex-col gap-4 mb-6">
                <h1 className="text-3xl md:text-4xl font-black text-gray-900">Mis métricas de llamadas</h1>

                <div className="flex flex-wrap gap-2 items-center justify-center w-full sm:max-w-xl mx-auto">
                    <button className="flex h-10 items-center gap-x-2 rounded-full bg-white px-4 shadow-sm border border-gray-200">
                        <p className="text-sm font-medium">Todas las campañas</p>
                        <span className="material-symbols-outlined text-base">expand_more</span>
                    </button>
                    <button className="flex h-10 items-center gap-x-2 rounded-full bg-white px-4 shadow-sm border border-gray-200">
                        <p className="text-sm font-medium">Últimos 30 días</p>
                        <span className="material-symbols-outlined text-base">expand_more</span>
                    </button>
                    <Button variant="primary" icon="download" onClick={handleExport}>
                        Descargar reporte
                    </Button>
                </div>
            </div>

            {/* Stats Cards */}
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
                <div className="flex flex-col gap-2 rounded-lg bg-white p-6 border border-gray-200/50 shadow-sm">
                    <p className="text-base font-medium text-gray-600">Llamadas realizadas</p>
                    <p className="text-3xl font-bold text-gray-900">{metricas.llamadasRealizadas.toLocaleString()}</p>
                    <p className="text-green-600 text-base font-medium">+5.2%</p>
                </div>

                <div className="flex flex-col gap-2 rounded-lg bg-white p-6 border border-gray-200/50 shadow-sm">
                    <p className="text-base font-medium text-gray-600">Contactos efectivos</p>
                    <p className="text-3xl font-bold text-gray-900">{metricas.contactosEfectivos.toLocaleString()}</p>
                    <p className="text-green-600 text-base font-medium">+8.1%</p>
                </div>

                <div className="flex flex-col gap-2 rounded-lg bg-white p-6 border border-gray-200/50 shadow-sm">
                    <p className="text-base font-medium text-gray-600">Duración media</p>
                    <p className="text-3xl font-bold text-gray-900">
                        {formatDuration(metricas.duracionPromedio)}
                    </p>
                    <p className="text-red-600 text-base font-medium">-1.5%</p>
                </div>

                <div className="flex flex-col gap-2 rounded-lg bg-white p-6 border border-gray-200/50 shadow-sm">
                    <p className="text-base font-medium text-gray-600">Tasa de contacto</p>
                    <p className="text-3xl font-bold text-gray-900">{metricas.tasaContacto.toFixed(0)}%</p>
                    <p className="text-green-600 text-base font-medium">+2.9%</p>
                </div>
            </div>

            {/* Charts Section */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
                {/* Llamadas por día */}
                <div className="flex flex-col gap-2 rounded-lg bg-white p-6 border border-gray-200/50 shadow-sm">
                    <p className="text-base font-medium text-gray-600">Llamadas por día</p>
                    <div className="flex items-baseline gap-2">
                        <p className="text-4xl font-bold text-gray-900">{metricas.llamadasMes}</p>
                        <div className="flex gap-1">
                            <p className="text-gray-500 text-sm">Últimos 30 días</p>
                            <p className="text-green-600 text-sm font-medium">+12.5%</p>
                        </div>
                    </div>
                    <div className="grid grid-flow-col gap-4 items-end justify-items-center min-h-[220px] px-3 pt-4">
                        {[70, 50, 80, 40, 60, 90, 100].map((height, i) => (
                            <div
                                key={i}
                                className={`w-full rounded-t ${i === 6 ? 'bg-primary' : 'bg-primary/20'}`}
                                style={{ height: `${height}%` }}
                            ></div>
                        ))}
                    </div>
                    <div className="flex justify-around pt-2">
                        {['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'].map(day => (
                            <p key={day} className="text-gray-500 text-xs font-bold">{day}</p>
                        ))}
                    </div>
                </div>

                {/* Distribución de resultados */}
                <div className="flex flex-col gap-2 rounded-lg bg-white p-6 border border-gray-200/50 shadow-sm">
                    <p className="text-base font-medium text-gray-600">Distribución de resultados</p>
                    <div className="flex items-baseline gap-2">
                        <p className="text-4xl font-bold text-gray-900">{metricas.llamadasRealizadas.toLocaleString()}</p>
                        <p className="text-gray-500 text-sm">Llamadas</p>
                    </div>
                    <div className="flex-1 flex items-center justify-center py-4">
                        <div className="relative w-40 h-40">
                            {/* Simple circular chart representation */}
                            <svg className="w-full h-full transform -rotate-90" viewBox="0 0 36 36">
                                <circle
                                    className="stroke-current text-gray-200"
                                    cx="18"
                                    cy="18"
                                    r="15.9155"
                                    fill="none"
                                    strokeWidth="4"
                                />
                                <circle
                                    className="stroke-current text-green-500"
                                    cx="18"
                                    cy="18"
                                    r="15.9155"
                                    fill="none"
                                    strokeWidth="4"
                                    strokeDasharray="46 100"
                                    strokeLinecap="round"
                                />
                                <circle
                                    className="stroke-current text-primary"
                                    cx="18"
                                    cy="18"
                                    r="15.9155"
                                    fill="none"
                                    strokeWidth="4"
                                    strokeDasharray="25 100"
                                    strokeDashoffset="-46"
                                    strokeLinecap="round"
                                />
                                <circle
                                    className="stroke-current text-yellow-500"
                                    cx="18"
                                    cy="18"
                                    r="15.9155"
                                    fill="none"
                                    strokeWidth="4"
                                    strokeDasharray="15 100"
                                    strokeDashoffset="-71"
                                    strokeLinecap="round"
                                />
                            </svg>
                        </div>
                    </div>
                    <div className="grid grid-cols-2 gap-x-4 gap-y-2">
                        <div className="flex items-center gap-2">
                            <div className="w-3 h-3 rounded-full bg-green-500"></div>
                            <span className="text-sm font-medium">Efectivo (46%)</span>
                        </div>
                        <div className="flex items-center gap-2">
                            <div className="w-3 h-3 rounded-full bg-primary"></div>
                            <span className="text-sm font-medium">Buzón (25%)</span>
                        </div>
                        <div className="flex items-center gap-2">
                            <div className="w-3 h-3 rounded-full bg-yellow-500"></div>
                            <span className="text-sm font-medium">No contesta (15%)</span>
                        </div>
                        <div className="flex items-center gap-2">
                            <div className="w-3 h-3 rounded-full bg-gray-300"></div>
                            <span className="text-sm font-medium">Ocupado (14%)</span>
                        </div>
                    </div>
                </div>
            </div>

            {/* Comparación: Tú vs Tú */}
            <h2 className="text-2xl font-bold text-gray-900 mb-4">Tú vs. Tú</h2>
            <div className="bg-white rounded-lg border border-gray-200/50 shadow-sm overflow-hidden">
                <div className="overflow-x-auto">
                    <table className="w-full text-left text-sm">
                        <thead className="bg-gray-50">
                            <tr>
                                <th className="px-6 py-4 font-medium text-gray-600">Periodo</th>
                                <th className="px-6 py-4 font-medium text-gray-600">Llamadas realizadas</th>
                                <th className="px-6 py-4 font-medium text-gray-600">Contactos efectivos</th>
                                <th className="px-6 py-4 font-medium text-gray-600">Tasa de contacto</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-200">
                            {metricas.periodoActual && (
                                <tr>
                                    <td className="px-6 py-4 font-medium">{metricas.periodoActual.periodo}</td>
                                    <td className="px-6 py-4 font-semibold text-base">{metricas.periodoActual.llamadasRealizadas}</td>
                                    <td className="px-6 py-4 font-semibold text-base">{metricas.periodoActual.contactosEfectivos}</td>
                                    <td className="px-6 py-4 font-semibold text-base">{metricas.periodoActual.tasaContacto}%</td>
                                </tr>
                            )}
                            {metricas.periodoAnterior && (
                                <tr>
                                    <td className="px-6 py-4 font-medium">{metricas.periodoAnterior.periodo}</td>
                                    <td className="px-6 py-4 font-semibold text-base">{metricas.periodoAnterior.llamadasRealizadas}</td>
                                    <td className="px-6 py-4 font-semibold text-base">{metricas.periodoAnterior.contactosEfectivos}</td>
                                    <td className="px-6 py-4 font-semibold text-base">{metricas.periodoAnterior.tasaContacto}%</td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
};
