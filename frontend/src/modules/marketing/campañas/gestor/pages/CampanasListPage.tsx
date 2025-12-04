import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useCampanas } from '../hooks/useCampanas';
import { CampanaTable } from '../components/CampanaTable';
import { CampanaDetailModal } from '../components/CampanaDetailModal';
import { CreateCampanaModal } from '../components/CreateCampanaModal';
import { SegmentosTab } from '../components/SegmentosTab';
import { HistorialTab } from '../components/HistorialTab';
import { PlantillasTab } from '../components/PlantillasTab';
import { CreatePlantillaModal } from '../components/CreatePlantillaModal';
import { Button } from '../../../../../shared/components/ui/Button';
import { Tabs } from '../../../../../shared/components/ui/Tabs';
import { campanasApi } from '../services/campanas.api';
import { Modal } from '../../../../../shared/components/ui/Modal';

export const CampanasListPage: React.FC = () => {
    const {
        campanas,
        loading,
        refresh,
        setFilter,
        totalPages,
        totalElements,
        currentPage,
        filtros,
    } = useCampanas();

    const navigate = useNavigate();
    const { tab } = useParams<{ tab: string }>();

    // Map URL params to internal tab values
    const getTabFromUrl = (urlTab?: string) => {
        switch (urlTab) {
            case 'segmentos': return 'segments';
            case 'plantillas': return 'templates';
            case 'historial': return 'history';
            default: return 'campaigns';
        }
    };

    // Map internal tab values to URL params
    const getUrlFromTab = (tabValue: string) => {
        switch (tabValue) {
            case 'segments': return 'segmentos';
            case 'templates': return 'plantillas';
            case 'history': return 'historial';
            default: return '';
        }
    };

    const activeTab = getTabFromUrl(tab);

    const handleTabChange = (value: string) => {
        const path = getUrlFromTab(value);
        navigate(path ? `/marketing/campanas/${path}` : '/marketing/campanas');
    };

    const [searchTerm, setSearchTerm] = useState(filtros.nombre || '');
    const [campanaToDelete, setCampanaToDelete] = useState<number | null>(null);
    const [isDeleting, setIsDeleting] = useState(false);
    const [historialTotalElements, setHistorialTotalElements] = useState(0);
    const [plantillasTotalElements, setPlantillasTotalElements] = useState(0);

    const [plantillasRefreshTrigger, setPlantillasRefreshTrigger] = useState(0);

    // Modal state
    const [selectedCampanaId, setSelectedCampanaId] = useState<number | null>(null);
    const [isDetailModalOpen, setIsDetailModalOpen] = useState(false);
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [isCreatePlantillaModalOpen, setIsCreatePlantillaModalOpen] = useState(false);

    // Search handler with debounce
    const handleSearch = (e: React.ChangeEvent<HTMLInputElement>) => {
        const value = e.target.value;
        setSearchTerm(value);

        const timeoutId = setTimeout(() => {
            setFilter('nombre', value);
        }, 500);

        return () => clearTimeout(timeoutId);
    };

    // Pagination handlers
    const goToPage = (page: number) => {
        if (page >= 0 && page < totalPages) {
            setFilter('page', page);
        }
    };

    // Delete handler
    const handleDeleteClick = (id: number) => {
        setCampanaToDelete(id);
    };

    const handleConfirmDelete = async () => {
        if (!campanaToDelete) return;

        setIsDeleting(true);
        try {
            await campanasApi.delete(campanaToDelete);
            await refresh();
            setCampanaToDelete(null);
        } catch (error) {
            console.error('Error al eliminar campaña:', error);
            alert('Hubo un error al eliminar la campaña. Por favor intenta de nuevo.');
        } finally {
            setIsDeleting(false);
        }
    };

    // View details handler - Open modal
    const handleEdit = (id: number) => {
        setSelectedCampanaId(id);
        setIsDetailModalOpen(true);
    };

    // Handler when modal closes
    const handleModalClose = () => {
        setIsDetailModalOpen(false);
        setSelectedCampanaId(null);
    };

    // Handler when campaign is updated in modal
    const handleCampanaUpdate = () => {
        refresh(); // Refresh the list
    };

    const getTitle = () => {
        switch (activeTab) {
            case 'segments': return 'Gestión de Segmentos';
            case 'history': return 'Historial Global de Campañas';
            case 'templates': return 'Plantillas de Campaña';
            default: return 'Panel de Campañas';
        }
    };

    return (
        <div className="space-y-6">
            {/* Header */}
            <header className="flex justify-between items-center">
                <div>
                    <h1 className="text-3xl font-bold text-dark">{getTitle()}</h1>
                    {activeTab === 'campaigns' && (
                        <p className="text-gray-500 mt-1">{totalElements} Campañas</p>
                    )}
                    {activeTab === 'history' && (
                        <p className="text-gray-500 mt-1">{historialTotalElements} Registros</p>
                    )}
                    {activeTab === 'templates' && (
                        <p className="text-gray-500 mt-1">{plantillasTotalElements} Plantillas</p>
                    )}
                </div>
                <div className="flex items-center space-x-3">
                    {activeTab === 'campaigns' && (
                        <Button variant="primary" onClick={() => setIsCreateModalOpen(true)}>
                            <span className="material-symbols-outlined text-lg mr-1.5">add</span>
                            Crear Nueva Campaña
                        </Button>
                    )}
                    {activeTab === 'templates' && (
                        <Button variant="primary" onClick={() => setIsCreatePlantillaModalOpen(true)}>
                            <span className="material-symbols-outlined text-lg mr-1.5">add</span>
                            Nueva Plantilla
                        </Button>
                    )}
                </div>
            </header>

            {/* Tabs */}
            <Tabs
                items={[
                    { label: 'Campañas', value: 'campaigns' },
                    { label: 'Segmentos', value: 'segments' },
                    { label: 'Plantillas', value: 'templates' },
                    { label: 'Historial', value: 'history' },
                ]}
                activeValue={activeTab}
                onChange={handleTabChange}
            />

            {/* Content based on active tab */}
            {activeTab === 'campaigns' && (
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
                                onChange={handleSearch}
                            />
                        </div>

                        {/* Filters */}
                        <div className="flex items-center gap-2">
                            <select
                                className="border border-separator rounded-lg px-3 py-2 text-sm text-gray-600 focus:outline-none focus:border-primary"
                                value={filtros.prioridad || ''}
                                onChange={(e) => setFilter('prioridad', e.target.value)}
                            >
                                <option value="">Todas las prioridades</option>
                                <option value="Alta">Alta</option>
                                <option value="Media">Media</option>
                                <option value="Baja">Baja</option>
                            </select>

                            <select
                                className="border border-separator rounded-lg px-3 py-2 text-sm text-gray-600 focus:outline-none focus:border-primary"
                                value={filtros.estado || ''}
                                onChange={(e) => setFilter('estado', e.target.value)}
                            >
                                <option value="">Todos los estados</option>
                                <option value="Borrador">Borrador</option>
                                <option value="Programada">Programada</option>
                                <option value="Vigente">Vigente</option>
                                <option value="Pausada">Pausada</option>
                                <option value="Finalizada">Finalizada</option>
                                <option value="Cancelada">Cancelada</option>
                            </select>

                            <select
                                className="border border-separator rounded-lg px-3 py-2 text-sm text-gray-600 focus:outline-none focus:border-primary"
                                value={filtros.canalEjecucion || ''}
                                onChange={(e) => setFilter('canalEjecucion', e.target.value)}
                            >
                                <option value="">Todos los canales</option>
                                <option value="Mailing">Mailing</option>
                                <option value="Llamadas">Llamadas</option>
                            </select>

                            <Button variant="secondary" onClick={refresh} title="Actualizar lista">
                                <span className="material-symbols-outlined text-xl">refresh</span>
                            </Button>
                        </div>
                    </div>

                    {/* Table */}
                    <CampanaTable
                        campanas={campanas}
                        isLoading={loading}
                        onEdit={handleEdit}
                        onDelete={handleDeleteClick}
                    />

                    {/* Pagination Footer */}
                    <div className="p-5 border-t border-separator flex justify-between items-center text-sm">
                        <div className="text-gray-600">
                            Mostrando{' '}
                            <span className="font-semibold">
                                {campanas.length > 0
                                    ? `${currentPage * 10 + 1}-${Math.min(currentPage * 10 + campanas.length, totalElements)}`
                                    : '0'}
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
            )}

            {activeTab === 'segments' && (
                <SegmentosTab />
            )}

            {activeTab === 'templates' && (
                <PlantillasTab
                    onTotalElementsChange={setPlantillasTotalElements}
                    refreshTrigger={plantillasRefreshTrigger}
                />
            )}

            {activeTab === 'history' && (
                <HistorialTab onTotalElementsChange={setHistorialTotalElements} />
            )}

            {/* Campaign Detail Modal */}
            <CampanaDetailModal
                isOpen={isDetailModalOpen}
                onClose={handleModalClose}
                campanaId={selectedCampanaId}
                onUpdate={handleCampanaUpdate}
            />

            {/* Create Campaign Modal */}
            <CreateCampanaModal
                isOpen={isCreateModalOpen}
                onClose={() => setIsCreateModalOpen(false)}
                onSuccess={() => {
                    setIsCreateModalOpen(false);
                    refresh();
                }}
            />

            {/* Create Plantilla Modal */}
            <CreatePlantillaModal
                isOpen={isCreatePlantillaModalOpen}
                onClose={() => setIsCreatePlantillaModalOpen(false)}
                onSuccess={() => {
                    setIsCreatePlantillaModalOpen(false);
                    setPlantillasRefreshTrigger(prev => prev + 1);
                }}
            />

            {/* Delete Confirmation Modal */}
            <Modal
                isOpen={!!campanaToDelete}
                title="Eliminar Campaña"
                onClose={() => setCampanaToDelete(null)}
                onConfirm={handleConfirmDelete}
                confirmText="Eliminar"
                variant="danger"
                isLoading={isDeleting}
            >
                <p>¿Estás seguro de que deseas eliminar esta campaña? Esta acción no se puede deshacer.</p>
            </Modal>
        </div>
    );
};
