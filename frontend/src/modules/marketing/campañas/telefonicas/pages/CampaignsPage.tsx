import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCampaignsContext } from '../context/CampaignsContext';
import { Button } from '../../../../../shared/components/ui/Button';
import { PageHeader } from '../../../../../shared/components/layout/PageHeader';
import { LoadingSpinner } from '../../../../../shared/components/ui/LoadingSpinner';
import { LoadingDots } from '../../../../../shared/components/ui/LoadingDots';

export const CampaignsPage: React.FC = () => {
    const navigate = useNavigate();
    const { campanias, loading, filters, setFilter, error } = useCampaignsContext();
    const [soloFavoritas, setSoloFavoritas] = useState(false);

    const filteredCampanias = campanias
        .filter(c => {
            if (filters.estadoFilter !== 'Todos' && c.estado !== filters.estadoFilter) return false;
            if (filters.searchTerm && !c.nombre.toLowerCase().includes(filters.searchTerm.toLowerCase())) return false;
            return true;
        })
        .sort((a, b) => {
            if (filters.ordenarPor === 'prioridad') {
                const prioridadOrder: Record<string, number> = { 'ALTA': 0, 'MEDIA': 1, 'BAJA': 2 };
                return (prioridadOrder[a.prioridad] || 99) - (prioridadOrder[b.prioridad] || 99);
            }
            return 0;
        });

    const getEstadoBadgeColor = (estado: string) => {
        switch (estado) {
            case 'ACTIVA': return 'bg-green-100 text-green-700';
            case 'PENDIENTE': return 'bg-gray-200 text-gray-600';
            case 'PAUSADA': return 'bg-yellow-100 text-yellow-700';
            case 'FINALIZADA': return 'bg-blue-100 text-blue-700';
            default: return 'bg-gray-200 text-gray-600';
        }
    };

    const getPrioridadBadgeColor = (prioridad: string) => {
        switch (prioridad) {
            case 'ALTA': return 'bg-red-100 text-red-700';
            case 'MEDIA': return 'bg-orange-100 text-orange-700';
            case 'BAJA': return 'bg-yellow-100 text-yellow-800';
            default: return 'bg-gray-200 text-gray-600';
        }
    };

    const canAccessCampaign = (estado: string) => {
        return estado === 'Vigente' || estado === 'ACTIVA';
    };

    if (error) {
        return (
            <div className="flex flex-col h-full">
                <PageHeader
                    title="Campa��as telef��nicas asignadas"
                />
                <div className="flex flex-1 items-center justify-center">
                    <div className="max-w-md text-center space-y-3">
                        <h2 className="text-2xl font-bold text-gray-900">No podemos mostrar tus campa��as</h2>
                        <p className="text-gray-600">{error}</p>
                        <p className="text-gray-500 text-sm">Si crees que se trata de un error, contacta al administrador del sistema.</p>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="flex flex-col h-full">
            <PageHeader
                title="Campañas telefónicas asignadas"
                action={
                    <Button variant="primary" onClick={() => navigate('/marketing/campanas/telefonicas/metricas')}>
                        Ir a métricas
                    </Button>
                }
            />

            {/* Tabs de navegación */}


            {/* Filtros y búsqueda */}
            <div className="rounded-lg border border-gray-200 bg-white shadow-sm mb-6">
                <div className="flex flex-wrap items-center justify-between gap-x-4 gap-y-2 p-4">
                    <div className="flex flex-wrap items-center gap-x-4 gap-y-2">
                        <label className="relative flex min-w-64 items-center">
                            <span className="material-symbols-outlined absolute left-4 text-gray-500">search</span>
                            <input
                                className="form-input h-10 w-full rounded-full border border-gray-300 bg-white pl-12 pr-4 text-gray-800"
                                placeholder="Buscar..."
                                type="text"
                                value={filters.searchTerm}
                                onChange={(e) => setFilter('searchTerm', e.target.value)}
                            />
                        </label>
                        <select
                            className="form-select h-10 min-w-48 rounded-full border border-gray-300 bg-white text-gray-800"
                            value={filters.estadoFilter}
                            onChange={(e) => setFilter('estadoFilter', e.target.value)}
                        >
                            <option>Todos</option>
                            <option>ACTIVA</option>
                            <option>PENDIENTE</option>
                            <option>PAUSADA</option>
                            <option>FINALIZADA</option>
                        </select>
                    </div>
                    <div className="flex flex-wrap items-center gap-x-4 gap-y-2">
                        <label className="flex flex-col min-w-40 flex-1">
                            <p className="text-sm font-medium text-gray-600 pb-2">Ordenar por</p>
                            <select
                                className="form-select w-full rounded-lg border border-gray-300 bg-white text-gray-800"
                                value={filters.ordenarPor}
                                onChange={(e) => setFilter('ordenarPor', e.target.value)}
                            >
                                <option value="prioridad">Prioridad</option>
                                <option value="fecha_inicio">Fecha de inicio</option>
                                <option value="fecha_fin">Fecha de fin</option>
                            </select>
                        </label>
                        <div className="flex items-center self-end pb-2">
                            <label className="flex cursor-pointer items-center gap-2">
                                <input
                                    className="form-checkbox h-5 w-5 rounded text-primary"
                                    type="checkbox"
                                    checked={soloFavoritas}
                                    onChange={(e) => setSoloFavoritas(e.target.checked)}
                                />
                                <span className="text-sm font-medium text-gray-700">Solo favoritas</span>
                            </label>
                        </div>
                    </div>
                </div>
            </div>

            {/* Tabla de campañas */}
            <div className="rounded-lg border border-gray-200 bg-white shadow-sm overflow-hidden flex-1">
                {loading ? (
                    <div className="flex flex-col items-center justify-center h-64 gap-4">
                        <LoadingSpinner size="lg" />
                        <LoadingDots text="Cargando campañas asignadas" className="text-gray-600 font-medium" />
                    </div>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="w-full text-left text-sm">
                            <thead className="bg-gray-100 text-xs uppercase text-gray-700">
                                <tr>
                                    <th className="px-4 py-2">Nombre campaña</th>
                                    <th className="px-4 py-2">Código</th>
                                    <th className="px-4 py-2">Estado</th>
                                    <th className="px-4 py-2">Prioridad</th>
                                    <th className="px-4 py-2">Fechas inicio/fin</th>
                                    <th className="px-4 py-2">Resumen personal</th>
                                    <th className="px-4 py-2"></th>
                                </tr>
                            </thead>
                            <tbody>
                                {filteredCampanias.map((campania) => (
                                    <tr
                                        key={campania.id}
                                        className="border-b bg-white hover:bg-gray-50"
                                    >
                                        <th className="whitespace-nowrap px-4 py-2 font-bold text-gray-900">
                                            {campania.nombre}
                                        </th>
                                        <td className="px-4 py-2">{campania.codigo}</td>
                                        <td className="px-4 py-2">
                                            <span className={`inline-flex items-center rounded-full px-3 py-1 text-xs font-medium ${getEstadoBadgeColor(campania.estado)}`}>
                                                {campania.estado}
                                            </span>
                                        </td>
                                        <td className="px-4 py-2">
                                            <span className={`inline-flex items-center rounded-full px-3 py-1 text-xs font-medium ${getPrioridadBadgeColor(campania.prioridad)}`}>
                                                {campania.prioridad}
                                            </span>
                                        </td>
                                        <td className="px-4 py-2">
                                            {new Date(campania.fechaInicio).toLocaleDateString()} - {new Date(campania.fechaFin).toLocaleDateString()}
                                        </td>
                                        <td className="px-4 py-2">
                                            <div className="flex w-36 flex-col gap-1">
                                                <div className="h-2 w-full rounded-full bg-gray-200">
                                                    <div
                                                        className="h-2 rounded-full bg-primary"
                                                        style={{ width: `${campania.porcentajeAvance}%` }}
                                                    ></div>
                                                </div>
                                                <span className="text-xs text-gray-500">
                                                    {campania.leadsContactados}/{campania.totalLeads} completado
                                                </span>
                                            </div>
                                        </td>
                                        <td className="px-4 py-2 text-right">
                                            <button
                                                className={`rounded-full px-4 py-2 text-xs font-bold transition-colors ${canAccessCampaign(campania.estado)
                                                    ? 'bg-primary text-white hover:bg-primary/90 cursor-pointer'
                                                    : 'bg-gray-300 text-gray-500 cursor-not-allowed'
                                                    }`}
                                                onClick={() => {
                                                    if (canAccessCampaign(campania.estado)) {
                                                        navigate(`/marketing/campanas/telefonicas/campanias/${campania.id}/cola`);
                                                    }
                                                }}
                                                disabled={!canAccessCampaign(campania.estado)}
                                            >
                                                {canAccessCampaign(campania.estado) ? 'Entrar' : 'No disponible'}
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        </div>
    );
};
