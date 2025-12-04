import React, { useState, useEffect } from 'react';
import { plantillasApi } from '../services/plantillas.api';
import { PlantillaCampana, CrearPlantillaRequest } from '../types/plantilla.types';
import { Modal } from '../../../../../shared/components/ui/Modal';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';

import { Button } from '../../../../../shared/components/ui/Button';

import { segmentosApi } from '../services/segmentos.api';
import { encuestasApi } from '../services/encuestas.api';
import { Segmento } from '../../../../../shared/types/segmento.types';
import { EncuestaDisponible } from '../../../../../shared/types/encuesta.types';
import { PlantillaDetailModal } from './PlantillaDetailModal';

import { useCampanasGestorContext } from '../context/CampanasGestorContext';

interface PlantillasTabProps {
    onTotalElementsChange?: (total: number) => void;
    refreshTrigger?: number;
}

export const PlantillasTab: React.FC<PlantillasTabProps> = ({ onTotalElementsChange, refreshTrigger = 0 }) => {
    const { plantillas, fetchPlantillas, setPlantillasFilter, setPlantillasPage } = useCampanasGestorContext();

    // Metadata state (kept local)
    const [segmentos, setSegmentos] = useState<Segmento[]>([]);
    const [encuestas, setEncuestas] = useState<EncuestaDisponible[]>([]);

    // Modal states
    const [selectedPlantilla, setSelectedPlantilla] = useState<PlantillaCampana | null>(null);
    const [isDetailModalOpen, setIsDetailModalOpen] = useState(false);
    const [plantillaToDelete, setPlantillaToDelete] = useState<number | null>(null);
    const [isDeleting, setIsDeleting] = useState(false);

    useEffect(() => {
        loadMetadata();
    }, []);

    // Sync total elements with parent
    useEffect(() => {
        if (onTotalElementsChange) {
            onTotalElementsChange(plantillas.pagination.totalElements);
        }
    }, [plantillas.pagination.totalElements, onTotalElementsChange]);

    // Refresh trigger
    useEffect(() => {
        if (refreshTrigger > 0) {
            fetchPlantillas(true);
        }
    }, [refreshTrigger, fetchPlantillas]);

    const loadMetadata = async () => {
        try {
            const [segmentosData, encuestasData] = await Promise.all([
                segmentosApi.getActivos(),
                encuestasApi.getDisponibles()
            ]);
            setSegmentos(segmentosData);
            setEncuestas(encuestasData);
        } catch (error) {
            console.error('Error loading metadata:', error);
        }
    };

    const getSegmentoNombre = (id?: number) => {
        if (!id) return 'Sin asignar';
        const segmento = segmentos.find(s => s.id === id);
        return segmento ? segmento.nombre : 'ID: ' + id;
    };

    const getEncuestaTitulo = (id?: number) => {
        if (!id) return 'Sin asignar';
        const encuesta = encuestas.find(e => e.idEncuesta === id);
        return encuesta ? encuesta.titulo : 'ID: ' + id;
    };

    const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setPlantillasFilter('nombre', e.target.value);
    };

    const handleCanalFilterChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        setPlantillasFilter('canalEjecucion', e.target.value);
    };

    const goToPage = (page: number) => {
        if (page >= 0 && page < plantillas.pagination.totalPages) {
            setPlantillasPage(page);
        }
    };

    const handleDelete = async () => {
        if (!plantillaToDelete) return;

        setIsDeleting(true);
        try {
            await plantillasApi.delete(plantillaToDelete);
            setPlantillaToDelete(null);
            await fetchPlantillas(true); // Force refresh
        } catch (error) {
            console.error('Error deleting plantilla:', error);
            alert('Hubo un error al eliminar la plantilla. Por favor intenta de nuevo.');
        } finally {
            setIsDeleting(false);
        }
    };

    const openDetailModal = (plantilla: PlantillaCampana) => {
        setSelectedPlantilla(plantilla);
        setIsDetailModalOpen(true);
    };

    const handleDetailModalClose = () => {
        setIsDetailModalOpen(false);
        setSelectedPlantilla(null);
    };

    return (
        <>
            <div className="bg-white rounded-lg shadow-card border border-separator">
                {/* Toolbar */}
                <div className="p-5 border-b border-separator flex flex-col sm:flex-row justify-between items-center gap-4">
                    {/* Search */}
                    <div className="relative w-full sm:w-96">
                        <span className="material-symbols-outlined absolute left-3 top-2.5 text-gray-400">
                            search
                        </span>
                        <input
                            type="text"
                            placeholder="Buscar por nombre..."
                            className="w-full pl-10 pr-4 py-2 border border-separator rounded-lg focus:ring-2 focus:ring-primary focus:outline-none transition-all"
                            value={plantillas.filters.nombre}
                            onChange={handleSearchChange}
                        />
                    </div>

                    {/* Filters */}
                    <div className="flex items-center gap-2">
                        <select
                            className="border border-separator rounded-lg px-3 py-2 text-sm text-gray-600 focus:outline-none focus:border-primary"
                            value={plantillas.filters.canalEjecucion}
                            onChange={handleCanalFilterChange}
                        >
                            <option value="">Todos los canales</option>
                            <option value="SIN_ASIGNAR">Sin asignar</option>
                            <option value="Mailing">Mailing</option>
                            <option value="Llamadas">Llamadas</option>
                        </select>

                        <Button variant="secondary" onClick={() => fetchPlantillas(true)} title="Actualizar lista">
                            <span className="material-symbols-outlined text-xl">refresh</span>
                        </Button>
                    </div>
                </div>

                {/* Table */}
                <div className="overflow-x-auto">
                    {plantillas.loading ? (
                        <div className="p-8 text-center">
                            <div className="inline-block h-8 w-8 animate-spin rounded-full border-2 border-solid border-primary border-r-transparent"></div>
                            <p className="mt-2 text-gray-500">Cargando plantillas...</p>
                        </div>
                    ) : plantillas.data.length === 0 ? (
                        <div className="p-8 text-center text-gray-500">
                            No se encontraron plantillas.
                        </div>
                    ) : (
                        <table className="w-full text-left">
                            <thead className="bg-table-header">
                                <tr>
                                    <th className="p-4 text-sm font-semibold text-dark tracking-wide">
                                        Nombre
                                    </th>
                                    <th className="p-4 text-sm font-semibold text-dark tracking-wide">
                                        Temática
                                    </th>
                                    <th className="p-4 text-sm font-semibold text-dark tracking-wide">
                                        Canal
                                    </th>
                                    <th className="p-4 text-sm font-semibold text-dark tracking-wide">
                                        Segmento
                                    </th>
                                    <th className="p-4 text-sm font-semibold text-dark tracking-wide">
                                        Encuesta
                                    </th>
                                    <th className="p-4 text-sm font-semibold text-dark tracking-wide">
                                        Acciones
                                    </th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-separator">
                                {plantillas.data.map((plantilla) => (
                                    <tr key={plantilla.idPlantilla} className="hover:bg-gray-50 transition-colors">
                                        <td className="p-4 font-medium text-dark whitespace-nowrap">
                                            {plantilla.nombre}
                                        </td>
                                        <td className="p-4 text-gray-600">
                                            <div className="text-sm">{plantilla.tematica}</div>
                                            {plantilla.descripcion && (
                                                <div className="text-xs text-gray-500 mt-1">{plantilla.descripcion}</div>
                                            )}
                                        </td>
                                        <td className="p-4 whitespace-nowrap">
                                            {plantilla.canalEjecucion ? (
                                                <span className={`px-2 py-1 text-xs font-medium rounded-full ${plantilla.canalEjecucion === 'Mailing'
                                                    ? 'bg-blue-100 text-blue-800'
                                                    : 'bg-green-100 text-green-800'
                                                    }`}>
                                                    {plantilla.canalEjecucion}
                                                </span>
                                            ) : (
                                                <span className="text-sm text-gray-400">Sin asignar</span>
                                            )}
                                        </td>
                                        <td className="p-4 text-gray-600 whitespace-nowrap">
                                            {getSegmentoNombre(plantilla.idSegmento)}
                                        </td>
                                        <td className="p-4 text-gray-600 whitespace-nowrap">
                                            {getEncuestaTitulo(plantilla.idEncuesta)}
                                        </td>
                                        <td className="p-4 whitespace-nowrap">
                                            <div className="flex items-center gap-2">
                                                <button
                                                    onClick={() => openDetailModal(plantilla)}
                                                    className="p-1.5 text-primary hover:bg-blue-50 rounded transition-colors"
                                                    title="Ver detalles"
                                                >
                                                    <span className="material-symbols-outlined text-xl">visibility</span>
                                                </button>
                                                <button
                                                    onClick={() => setPlantillaToDelete(plantilla.idPlantilla)}
                                                    className="p-1.5 text-red-600 hover:bg-red-50 rounded transition-colors"
                                                    title="Eliminar"
                                                >
                                                    <span className="material-symbols-outlined text-xl">delete</span>
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    )}
                </div>

                {/* Pagination Footer */}
                <div className="p-5 border-t border-separator flex justify-between items-center text-sm">
                    <div className="text-gray-600">
                        Mostrando{' '}
                        <span className="font-semibold">
                            {plantillas.data.length > 0
                                ? `${plantillas.pagination.page * 10 + 1}-${Math.min(plantillas.pagination.page * 10 + plantillas.data.length, plantillas.pagination.totalElements)}`
                                : '0'}
                        </span>{' '}
                        de <span className="font-semibold">{plantillas.pagination.totalElements}</span> resultados totales
                        {plantillas.pagination.totalPages > 1 && (
                            <span className="ml-2">
                                (Página {plantillas.pagination.page + 1} de {plantillas.pagination.totalPages})
                            </span>
                        )}
                    </div>

                    {/* Pagination Controls */}
                    {plantillas.pagination.totalPages > 1 && (
                        <div className="flex items-center gap-1">
                            <button
                                className="px-3 py-1.5 border border-separator rounded-md bg-white hover:bg-gray-50 text-gray-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                                onClick={() => goToPage(0)}
                                disabled={plantillas.pagination.page === 0 || plantillas.loading}
                            >
                                « Inicio
                            </button>
                            <button
                                className="px-3 py-1.5 border border-separator rounded-md bg-white hover:bg-gray-50 text-gray-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                                onClick={() => goToPage(plantillas.pagination.page - 1)}
                                disabled={plantillas.pagination.page === 0 || plantillas.loading}
                            >
                                ‹ Anterior
                            </button>

                            {/* Page numbers */}
                            <div className="flex gap-1">
                                {Array.from({ length: Math.min(plantillas.pagination.totalPages, 5) }, (_, i) => {
                                    let pageNum;
                                    if (plantillas.pagination.totalPages <= 5) {
                                        pageNum = i;
                                    } else if (plantillas.pagination.page < 3) {
                                        pageNum = i;
                                    } else if (plantillas.pagination.page >= plantillas.pagination.totalPages - 3) {
                                        pageNum = plantillas.pagination.totalPages - 5 + i;
                                    } else {
                                        pageNum = plantillas.pagination.page - 2 + i;
                                    }

                                    return (
                                        <button
                                            key={pageNum}
                                            className={`px-3 py-1.5 border rounded-md transition-colors ${plantillas.pagination.page === pageNum
                                                ? 'bg-primary text-white border-primary'
                                                : 'bg-white hover:bg-gray-50 text-gray-600 border-separator'
                                                }`}
                                            onClick={() => goToPage(pageNum)}
                                            disabled={plantillas.loading}
                                        >
                                            {pageNum + 1}
                                        </button>
                                    );
                                })}
                            </div>

                            <button
                                className="px-3 py-1.5 border border-separator rounded-md bg-white hover:bg-gray-50 text-gray-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                                onClick={() => goToPage(plantillas.pagination.page + 1)}
                                disabled={plantillas.pagination.page >= plantillas.pagination.totalPages - 1 || plantillas.loading}
                            >
                                Siguiente ›
                            </button>
                            <button
                                className="px-3 py-1.5 border border-separator rounded-md bg-white hover:bg-gray-50 text-gray-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                                onClick={() => goToPage(plantillas.pagination.totalPages - 1)}
                                disabled={plantillas.pagination.page >= plantillas.pagination.totalPages - 1 || plantillas.loading}
                            >
                                Fin »
                            </button>
                        </div>
                    )}
                </div>
            </div>

            {/* Detail Modal */}
            <PlantillaDetailModal
                isOpen={isDetailModalOpen}
                onClose={handleDetailModalClose}
                plantilla={selectedPlantilla}
                onUpdate={() => fetchPlantillas(true)}
            />

            {/* Delete Confirmation Modal */}
            <Modal
                isOpen={!!plantillaToDelete}
                title="Eliminar Plantilla"
                onClose={() => setPlantillaToDelete(null)}
                onConfirm={handleDelete}
                confirmText="Eliminar"
                variant="danger"
                isLoading={isDeleting}
            >
                <p>¿Estás seguro de que deseas eliminar esta plantilla? Esta acción no se puede deshacer.</p>
            </Modal>
        </>
    );
};
