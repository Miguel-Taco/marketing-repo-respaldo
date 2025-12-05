import React, { useState, useEffect } from 'react';
import { campanasApi } from '../services/campanas.api';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';

import { useCampanasGestorContext } from '../context/CampanasGestorContext';

interface HistorialTabProps {
    onTotalElementsChange?: (total: number) => void;
}

export const HistorialTab: React.FC<HistorialTabProps> = ({ onTotalElementsChange }) => {
    const { historial, fetchHistorial, setHistorialFilter, setHistorialPage } = useCampanasGestorContext();
    const [searchTerm, setSearchTerm] = useState('');

    // Sync total elements with parent
    useEffect(() => {
        if (onTotalElementsChange) {
            onTotalElementsChange(historial.pagination.totalElements);
        }
    }, [historial.pagination.totalElements, onTotalElementsChange]);

    // Client-side filtering for search term
    const filteredHistorial = historial.data.filter(item =>
        item.nombreCampana.toLowerCase().includes(searchTerm.toLowerCase())
    );

    const getActionColor = (action: string) => {
        switch (action) {
            case 'CREACION': return 'text-green-600';
            case 'ACTUALIZACION': return 'text-blue-600';
            case 'ELIMINACION': return 'text-red-600';
            case 'ARCHIVADO': return 'text-gray-600';
            default: return 'text-primary';
        }
    };

    const formatActionName = (action: string) => {
        switch (action) {
            case 'CREACION': return 'creada';
            case 'ACTUALIZACION': return 'actualizada';
            case 'ELIMINACION': return 'eliminada';
            case 'ARCHIVADO': return 'archivada';
            case 'PROGRAMACION': return 'programada';
            case 'ACTIVACION': return 'activada';
            case 'PAUSA': return 'pausada';
            case 'REANUDACION': return 'reanudada';
            case 'FINALIZACION': return 'finalizada';
            case 'CANCELACION': return 'cancelada';
            default: return action.toLowerCase();
        }
    };

    return (
        <div className="bg-white rounded-lg shadow-card border border-separator">
            {/* Toolbar */}
            <div className="p-5 border-b border-separator flex flex-col xl:flex-row justify-between items-end xl:items-center gap-4">
                {/* Search */}
                <div className="relative w-full xl:w-96">
                    <span className="material-symbols-outlined absolute left-3 top-2.5 text-gray-400">
                        search
                    </span>
                    <input
                        type="text"
                        placeholder="Buscar por nombre de campaña..."
                        className="w-full pl-10 pr-4 py-2 border border-separator rounded-lg focus:ring-2 focus:ring-primary focus:outline-none transition-all"
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>

                {/* Filters */}
                <div className="flex flex-col sm:flex-row items-center gap-2 w-full xl:w-auto">
                    <select
                        className="w-full sm:w-auto border border-separator rounded-lg px-3 py-2 text-sm text-gray-600 focus:outline-none focus:border-primary"
                        value={historial.filters.tipoAccion}
                        onChange={(e) => {
                            setHistorialFilter('tipoAccion', e.target.value);
                        }}
                    >
                        <option value="">Todas las acciones</option>
                        <option value="CREACION">Creada</option>
                        <option value="ACTUALIZACION">Actualizada</option>
                        <option value="ARCHIVADO">Archivada</option>
                        <option value="PROGRAMACION">Programada</option>
                        <option value="ACTIVACION">Activada</option>
                        <option value="PAUSA">Pausada</option>
                        <option value="CANCELACION">Cancelada</option>
                    </select>

                    <div className="flex items-center gap-2 w-full sm:w-auto">
                        <div className="flex items-center gap-1.5">
                            <label className="text-sm text-gray-600 whitespace-nowrap">Desde:</label>
                            <input
                                type="date"
                                className="w-full sm:w-auto border border-separator rounded-lg px-3 py-2 text-sm text-gray-600 focus:outline-none focus:border-primary"
                                value={historial.filters.fechaDesde}
                                onChange={(e) => {
                                    setHistorialFilter('fechaDesde', e.target.value);
                                }}
                            />
                        </div>
                        <div className="flex items-center gap-1.5">
                            <label className="text-sm text-gray-600 whitespace-nowrap">Hasta:</label>
                            <input
                                type="date"
                                className="w-full sm:w-auto border border-separator rounded-lg px-3 py-2 text-sm text-gray-600 focus:outline-none focus:border-primary"
                                value={historial.filters.fechaHasta}
                                onChange={(e) => {
                                    setHistorialFilter('fechaHasta', e.target.value);
                                }}
                            />
                        </div>
                    </div>

                    <button
                        onClick={() => fetchHistorial(true)}
                        className="p-2 border border-separator rounded-lg hover:bg-gray-50 text-gray-600 transition-colors"
                        title="Actualizar lista"
                    >
                        <span className="material-symbols-outlined text-xl">refresh</span>
                    </button>
                </div>
            </div>

            {/* History List */}
            <div className="flex flex-col">
                {historial.loading ? (
                    <div className="p-8 text-center">
                        <div className="inline-block h-8 w-8 animate-spin rounded-full border-2 border-solid border-primary border-r-transparent"></div>
                        <p className="mt-2 text-gray-500">Cargando historial...</p>
                    </div>
                ) : filteredHistorial.length === 0 ? (
                    <div className="p-8 text-center text-gray-500">
                        No se encontraron registros en el historial.
                    </div>
                ) : (
                    <ul className="divide-y divide-separator">
                        {filteredHistorial.map((item) => (
                            <li key={item.idHistorial} className="p-6 hover:bg-gray-50 transition-colors">
                                <div className="flex justify-between items-start">
                                    <div>
                                        <p className="text-gray-500 text-sm">
                                            <span className="font-bold text-primary hover:underline cursor-pointer">
                                                {item.nombreCampana}
                                            </span>
                                            {' '}ha sido{' '}
                                            <span className={`font-medium ${getActionColor(item.tipoAccion)}`}>
                                                {formatActionName(item.tipoAccion)}
                                            </span>
                                            {' '}por{' '}
                                            <span className="font-medium text-dark">
                                                {item.usuarioResponsable || 'Sistema'}
                                            </span>.
                                        </p>
                                        <p className="text-gray-500 text-sm mt-1">
                                            {format(new Date(item.fechaAccion), "d 'de' MMMM, yyyy 'a las' HH:mm", { locale: es })}
                                            {item.descripcionDetalle && (
                                                <span className="block mt-1 text-gray-600 italic">
                                                    "{item.descripcionDetalle}"
                                                </span>
                                            )}
                                        </p>
                                    </div>
                                </div>
                            </li>
                        ))}
                    </ul>
                )}

                {/* Pagination Footer */}
                <div className="p-5 border-t border-separator flex justify-between items-center text-sm">
                    <div className="text-gray-600">
                        Mostrando{' '}
                        <span className="font-semibold">
                            {filteredHistorial.length > 0
                                ? `${historial.pagination.page * 6 + 1}-${Math.min(historial.pagination.page * 6 + filteredHistorial.length, historial.pagination.totalElements)}`
                                : '0'}
                        </span>{' '}
                        de <span className="font-semibold">{historial.pagination.totalElements}</span> resultados totales
                        {historial.pagination.totalPages > 1 && (
                            <span className="ml-2">
                                (Página {historial.pagination.page + 1} de {historial.pagination.totalPages})
                            </span>
                        )}
                    </div>

                    {/* Pagination Controls */}
                    {historial.pagination.totalPages > 1 && (
                        <div className="flex items-center gap-1">
                            <button
                                className="px-3 py-1.5 border border-separator rounded-md bg-white hover:bg-gray-50 text-gray-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                                onClick={() => setHistorialPage(0)}
                                disabled={historial.pagination.page === 0 || historial.loading}
                            >
                                « Inicio
                            </button>
                            <button
                                className="px-3 py-1.5 border border-separator rounded-md bg-white hover:bg-gray-50 text-gray-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                                onClick={() => setHistorialPage(Math.max(0, historial.pagination.page - 1))}
                                disabled={historial.pagination.page === 0 || historial.loading}
                            >
                                ‹ Anterior
                            </button>

                            {/* Page numbers */}
                            <div className="flex gap-1">
                                {Array.from({ length: Math.min(historial.pagination.totalPages, 5) }, (_, i) => {
                                    let pageNum;
                                    if (historial.pagination.totalPages <= 5) {
                                        pageNum = i;
                                    } else if (historial.pagination.page < 3) {
                                        pageNum = i;
                                    } else if (historial.pagination.page >= historial.pagination.totalPages - 3) {
                                        pageNum = historial.pagination.totalPages - 5 + i;
                                    } else {
                                        pageNum = historial.pagination.page - 2 + i;
                                    }

                                    return (
                                        <button
                                            key={pageNum}
                                            className={`px-3 py-1.5 border rounded-md transition-colors ${historial.pagination.page === pageNum
                                                ? 'bg-primary text-white border-primary'
                                                : 'bg-white hover:bg-gray-50 text-gray-600 border-separator'
                                                }`}
                                            onClick={() => setHistorialPage(pageNum)}
                                            disabled={historial.loading}
                                        >
                                            {pageNum + 1}
                                        </button>
                                    );
                                })}
                            </div>

                            <button
                                className="px-3 py-1.5 border border-separator rounded-md bg-white hover:bg-gray-50 text-gray-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                                onClick={() => setHistorialPage(Math.min(historial.pagination.totalPages - 1, historial.pagination.page + 1))}
                                disabled={historial.pagination.page >= historial.pagination.totalPages - 1 || historial.loading}
                            >
                                Siguiente ›
                            </button>
                            <button
                                className="px-3 py-1.5 border border-separator rounded-md bg-white hover:bg-gray-50 text-gray-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                                onClick={() => setHistorialPage(historial.pagination.totalPages - 1)}
                                disabled={historial.pagination.page >= historial.pagination.totalPages - 1 || historial.loading}
                            >
                                Fin »
                            </button>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};
