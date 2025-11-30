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

interface PlantillasTabProps {
    onTotalElementsChange?: (total: number) => void;
    refreshTrigger?: number;
}

export const PlantillasTab: React.FC<PlantillasTabProps> = ({ onTotalElementsChange, refreshTrigger = 0 }) => {
    const [plantillas, setPlantillas] = useState<PlantillaCampana[]>([]);
    const [segmentos, setSegmentos] = useState<Segmento[]>([]);
    const [encuestas, setEncuestas] = useState<EncuestaDisponible[]>([]);
    const [loading, setLoading] = useState(false);
    const [searchTerm, setSearchTerm] = useState('');
    const [canalFilter, setCanalFilter] = useState('');
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);

    // Modal states
    const [selectedPlantilla, setSelectedPlantilla] = useState<PlantillaCampana | null>(null);
    const [isDetailModalOpen, setIsDetailModalOpen] = useState(false);
    const [plantillaToDelete, setPlantillaToDelete] = useState<number | null>(null);
    const [isDeleting, setIsDeleting] = useState(false);

    useEffect(() => {
        loadMetadata();
    }, []);

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

    const fetchPlantillas = async () => {
        setLoading(true);
        try {
            const response = await plantillasApi.getAll({
                nombre: searchTerm || undefined,
                canalEjecucion: canalFilter || undefined,
                page: currentPage,
                size: 10,
            });

            setPlantillas(response.content);
            setTotalPages(response.total_pages);
            setTotalElements(response.total_elements);

            if (onTotalElementsChange) {
                onTotalElementsChange(response.total_elements);
            }
        } catch (error) {
            console.error('Error fetching plantillas:', error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchPlantillas();
    }, [currentPage, searchTerm, canalFilter, refreshTrigger]);

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
        setSearchTerm(e.target.value);
        setCurrentPage(0);
    };

    const handleCanalFilterChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        setCanalFilter(e.target.value);
        setCurrentPage(0);
    };

    const goToPage = (page: number) => {
        if (page >= 0 && page < totalPages) {
            setCurrentPage(page);
        }
    };

    const handleDelete = async () => {
        if (!plantillaToDelete) return;

        setIsDeleting(true);
        try {
            await plantillasApi.delete(plantillaToDelete);
            setPlantillaToDelete(null);
            await fetchPlantillas();
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
                            value={searchTerm}
                            onChange={handleSearchChange}
                        />
                    </div>

                    {/* Filters */}
                    <div className="flex items-center gap-2">
                        <select
                            className="border border-separator rounded-lg px-3 py-2 text-sm text-gray-600 focus:outline-none focus:border-primary"
                            value={canalFilter}
                            onChange={handleCanalFilterChange}
                        >
                            <option value="">Todos los canales</option>
                            <option value="SIN_ASIGNAR">Sin asignar</option>
                            <option value="Mailing">Mailing</option>
                            <option value="Llamadas">Llamadas</option>
                        </select>

                        <Button variant="secondary" onClick={fetchPlantillas} title="Actualizar lista">
                            <span className="material-symbols-outlined text-xl">refresh</span>
                        </Button>
                    </div>
                </div>

                {/* Table */}
                <div className="overflow-x-auto">
                    {loading ? (
                        <div className="p-8 text-center">
                            <div className="inline-block h-8 w-8 animate-spin rounded-full border-2 border-solid border-primary border-r-transparent"></div>
                            <p className="mt-2 text-gray-500">Cargando plantillas...</p>
                        </div>
                    ) : plantillas.length === 0 ? (
                        <div className="p-8 text-center text-gray-500">
                            No se encontraron plantillas.
                        </div>
                    ) : (
                        <table className="w-full">
                            <thead className="bg-gray-50 border-b border-separator">
                                <tr>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Nombre
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Temática
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Canal
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Segmento
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Encuesta
                                    </th>
                                    <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Acciones
                                    </th>
                                </tr>
                            </thead>
                            <tbody className="bg-white divide-y divide-separator">
                                {plantillas.map((plantilla) => (
                                    <tr key={plantilla.idPlantilla} className="hover:bg-gray-50 transition-colors">
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <div className="text-sm font-medium text-gray-900">{plantilla.nombre}</div>
                                        </td>
                                        <td className="px-6 py-4">
                                            <div className="text-sm text-gray-900">{plantilla.tematica}</div>
                                            {plantilla.descripcion && (
                                                <div className="text-xs text-gray-500 mt-1">{plantilla.descripcion}</div>
                                            )}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
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
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                                            {getSegmentoNombre(plantilla.idSegmento)}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                                            {getEncuestaTitulo(plantilla.idEncuesta)}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                                            <button
                                                onClick={() => openDetailModal(plantilla)}
                                                className="text-primary hover:text-primary/80 mr-3"
                                                title="Ver detalles"
                                            >
                                                <span className="material-symbols-outlined text-xl">visibility</span>
                                            </button>
                                            <button
                                                onClick={() => setPlantillaToDelete(plantilla.idPlantilla)}
                                                className="text-red-600 hover:text-red-800"
                                                title="Eliminar"
                                            >
                                                <span className="material-symbols-outlined text-xl">delete</span>
                                            </button>
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
                            {plantillas.length > 0 ? currentPage * 10 + 1 : 0}
                        </span>{' '}
                        de <span className="font-semibold">{totalElements}</span> resultados totales
                        {totalPages > 1 && (
                            <span className="ml-2">
                                (Página {currentPage + 1} de {totalPages})
                            </span>
                        )}
                    </div>

                    {/* Pagination Controls */}
                    {totalPages > 1 && (
                        <div className="flex items-center gap-1">
                            <button
                                className="px-3 py-1.5 border border-separator rounded-md bg-white hover:bg-gray-50 text-gray-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                                onClick={() => goToPage(0)}
                                disabled={currentPage === 0 || loading}
                            >
                                « Inicio
                            </button>
                            <button
                                className="px-3 py-1.5 border border-separator rounded-md bg-white hover:bg-gray-50 text-gray-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                                onClick={() => goToPage(currentPage - 1)}
                                disabled={currentPage === 0 || loading}
                            >
                                ‹ Anterior
                            </button>

                            {/* Page numbers */}
                            <div className="flex gap-1">
                                {Array.from({ length: Math.min(totalPages, 5) }, (_, i) => {
                                    let pageNum;
                                    if (totalPages <= 5) {
                                        pageNum = i;
                                    } else if (currentPage < 3) {
                                        pageNum = i;
                                    } else if (currentPage >= totalPages - 3) {
                                        pageNum = totalPages - 5 + i;
                                    } else {
                                        pageNum = currentPage - 2 + i;
                                    }

                                    return (
                                        <button
                                            key={pageNum}
                                            className={`px-3 py-1.5 border rounded-md transition-colors ${currentPage === pageNum
                                                ? 'bg-primary text-white border-primary'
                                                : 'bg-white hover:bg-gray-50 text-gray-600 border-separator'
                                                }`}
                                            onClick={() => goToPage(pageNum)}
                                            disabled={loading}
                                        >
                                            {pageNum + 1}
                                        </button>
                                    );
                                })}
                            </div>

                            <button
                                className="px-3 py-1.5 border border-separator rounded-md bg-white hover:bg-gray-50 text-gray-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                                onClick={() => goToPage(currentPage + 1)}
                                disabled={currentPage >= totalPages - 1 || loading}
                            >
                                Siguiente ›
                            </button>
                            <button
                                className="px-3 py-1.5 border border-separator rounded-md bg-white hover:bg-gray-50 text-gray-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                                onClick={() => goToPage(totalPages - 1)}
                                disabled={currentPage >= totalPages - 1 || loading}
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
                onUpdate={fetchPlantillas}
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
