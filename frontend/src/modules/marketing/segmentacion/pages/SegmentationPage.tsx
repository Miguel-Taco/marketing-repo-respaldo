import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useSegmentos } from '../hooks/useSegmentos';
import { SegmentTable } from '../components/SegmentTable';
import { DeleteConfirmModal } from '../components/DeleteConfirmModal';
import { QuickEditSegmentModal } from '../components/QuickEditSegmentModal';
import { Button } from '../../../../shared/components/ui/Button';
import { Tabs } from '../../../../shared/components/ui/Tabs';
import { segmentacionApi } from '../services/segmentacion.api';
import { Segmento } from '../types/segmentacion.types';
import { useSegmentosContext } from '../context/SegmentosContext';

export const SegmentationPage: React.FC = () => {
    const {
        segmentos,
        loading,
        refresh,
        setFilter,
        totalPages,
        totalElements,
        currentPage
    } = useSegmentos();

    const navigate = useNavigate();
    const [activeTab, setActiveTab] = useState<'all' | 'used' | 'unused' | 'deleted'>('all');
    const [searchTerm, setSearchTerm] = useState('');
    const [selectedIds, setSelectedIds] = useState<number[]>([]);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [segmentToDelete, setSegmentToDelete] = useState<number | null>(null);
    const [isDeleting, setIsDeleting] = useState(false);

    // Quick Edit Modal state
    const [isQuickEditOpen, setIsQuickEditOpen] = useState(false);
    const [selectedSegment, setSelectedSegment] = useState<Segmento | null>(null);

    const { updateSegmento } = useSegmentosContext();

    // Search handler with debounce
    const handleSearch = (e: React.ChangeEvent<HTMLInputElement>) => {
        const value = e.target.value;
        setSearchTerm(value);

        const timeoutId = setTimeout(() => {
            setFilter('search', value);
        }, 500);

        return () => clearTimeout(timeoutId);
    };

    // Tab change handler
    const handleTabChange = (tab: 'all' | 'used' | 'unused' | 'deleted') => {
        setActiveTab(tab);
        if (tab === 'used') {
            setFilter('estado', 'ACTIVO');
        } else if (tab === 'unused') {
            setFilter('estado', 'INACTIVO');
        } else if (tab === 'deleted') {
            setFilter('estado', 'ELIMINADO');
        } else {
            setFilter('estado', '');
        }
    };

    // Pagination handlers
    const goToPage = (page: number) => {
        if (page >= 0 && page < totalPages) {
            setFilter('page', page);
        }
    };

    // Delete handlers
    const handleDeleteSingle = (id: number) => {
        setSegmentToDelete(id);
        setShowDeleteModal(true);
    };

    const handleDeleteMultiple = () => {
        if (selectedIds.length > 0) {
            setSegmentToDelete(null);
            setShowDeleteModal(true);
        }
    };

    const confirmDelete = async () => {
        setIsDeleting(true);
        try {
            if (segmentToDelete !== null) {
                await segmentacionApi.delete(segmentToDelete);
            } else {
                for (const id of selectedIds) {
                    await segmentacionApi.delete(id);
                }
            }

            await refresh();
            setSelectedIds([]);
            setShowDeleteModal(false);
        } catch (error) {
            console.error('Error al eliminar segmento(s):', error);
            alert('Hubo un error al eliminar los segmentos. Por favor intenta de nuevo.');
        } finally {
            setIsDeleting(false);
            setSegmentToDelete(null);
        }
    };

    // Quick Edit handlers
    const handleEdit = (id: number) => {
        const segment = segmentos.find(s => s.id === id);
        if (segment) {
            setSelectedSegment(segment);
            setIsQuickEditOpen(true);
        }
    };

    const handleQuickSave = (updatedSegment: Segmento) => {
        updateSegmento(updatedSegment);
        setIsQuickEditOpen(false);
    };

    const handleAdvancedEdit = (id: number) => {
        setIsQuickEditOpen(false);
        navigate(`/marketing/segmentacion/edit/${id}`);
    };

    return (
        <div className="p-6 space-y-6">
            {/* Header */}
            <header className="flex justify-between items-center">
                <div>
                    <h1 className="text-3xl font-bold text-dark">Segmentos</h1>
                    <p className="text-gray-500 mt-1">{totalElements} Segmentos</p>
                </div>
                <div className="flex items-center space-x-3">
                    <Button variant="primary" onClick={() => navigate('/marketing/segmentacion/new')}>
                        <span className="material-symbols-outlined text-lg mr-1.5">add</span>
                        Crear segmento
                    </Button>
                </div>
            </header>

            {/* Tabs */}
            <Tabs
                items={[
                    { label: 'Todos los segmentos', value: 'all' },
                    { label: 'Segmentos usados', value: 'used' },
                    { label: 'Segmentos sin usar', value: 'unused' },
                    { label: 'Eliminados recientemente', value: 'deleted' }
                ]}
                activeValue={activeTab}
                onChange={(value) => handleTabChange(value as 'all' | 'used' | 'unused' | 'deleted')}
            />

            {/* Toolbar */}
            <div className="flex flex-col sm:flex-row gap-4 justify-between items-start sm:items-center">
                {/* Search */}
                <div className="relative w-full sm:w-96">
                    <span className="material-symbols-outlined absolute left-3 top-2.5 text-gray-400">search</span>
                    <input
                        type="text"
                        placeholder="Buscar por nombre o descripción..."
                        className="w-full pl-10 pr-4 py-2 border border-separator rounded-lg focus:ring-2 focus:ring-primary focus:outline-none transition-all"
                        value={searchTerm}
                        onChange={handleSearch}
                    />
                </div>

                {/* Filters */}
                <div className="flex items-center gap-2">
                    <select
                        className="border border-separator rounded-lg px-3 py-2 text-sm text-gray-600 focus:outline-none focus:border-primary"
                        onChange={(e) => setFilter('tipoAudiencia', e.target.value)}
                    >
                        <option value="">Tipo de Audiencia</option>
                        <option value="LEAD">Lead</option>
                        <option value="CLIENTE">Cliente</option>
                        <option value="MIXTO">Mixto</option>
                    </select>

                    <Button variant="secondary" onClick={refresh} title="Actualizar lista">
                        <span className="material-symbols-outlined text-xl">refresh</span>
                    </Button>
                </div>
            </div>

            {/* Selection Actions Bar */}
            {selectedIds.length > 0 && (
                <div className="p-4 bg-blue-50 border border-blue-200 rounded-lg flex items-center justify-between">
                    <span className="text-sm font-medium text-blue-900">
                        {selectedIds.length} segmento(s) seleccionado(s)
                    </span>
                    <div className="flex gap-2">
                        <Button
                            variant="secondary"
                            onClick={() => setSelectedIds([])}
                        >
                            Limpiar selección
                        </Button>
                        <Button
                            variant="danger"
                            onClick={handleDeleteMultiple}
                        >
                            <span className="material-symbols-outlined text-lg mr-1">delete</span>
                            Eliminar seleccionados
                        </Button>
                    </div>
                </div>
            )}

            {/* Table */}
            <div className="bg-white rounded-lg shadow-sm border border-separator">
                <SegmentTable
                    segmentos={segmentos}
                    isLoading={loading}
                    onEdit={handleEdit}
                    selectedIds={selectedIds}
                    onSelectionChange={setSelectedIds}
                    onDelete={handleDeleteSingle}
                />

                {/* Pagination Footer - Exact Leads Style */}
                <div className="p-5 border-t border-separator flex justify-between items-center text-sm">
                    <div className="text-gray-600">
                        Mostrando <span className="font-semibold">{segmentos.length > 0 ? ((currentPage * 10) + 1) : 0}</span> de <span className="font-semibold">{totalElements}</span> resultados totales
                        {totalPages > 1 && (
                            <span className="ml-2">(Página {currentPage + 1} de {totalPages})</span>
                        )}
                    </div>

                    {/* Numeric Pagination - Exact Leads Style */}
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
                                &lsaquo; Anterior
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
                                Siguiente &rsaquo;
                            </button>
                            <button
                                className="px-3 py-1.5 border border-separator rounded-md bg-white hover:bg-gray-50 text-gray-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                                onClick={() => goToPage(totalPages - 1)}
                                disabled={currentPage >= totalPages - 1 || loading}
                            >
                                Fin &raquo;
                            </button>
                        </div>
                    )}
                </div>
            </div>

            {/* Delete Modal */}
            <DeleteConfirmModal
                isOpen={showDeleteModal}
                onClose={() => setShowDeleteModal(false)}
                onConfirm={confirmDelete}
                isDeleting={isDeleting}
                selectedCount={segmentToDelete !== null ? 1 : selectedIds.length}
            />

            {/* Quick Edit Modal */}
            <QuickEditSegmentModal
                isOpen={isQuickEditOpen}
                segment={selectedSegment}
                onClose={() => setIsQuickEditOpen(false)}
                onSave={handleQuickSave}
                onAdvancedEdit={handleAdvancedEdit}
            />
        </div>
    );
};
