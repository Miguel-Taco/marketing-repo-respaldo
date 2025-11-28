import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useLeads } from '../hooks/useLeads';
import { LeadTable } from '../components/LeadTable';
import { DeleteLeadsModal } from '../components/DeleteLeadsModal';
import { ChangeStatusModal } from '../components/ChangeStatusModal';
import { leadsApi } from '../services/leads.api';
import { Button } from '../../../../shared/components/ui/Button';
import { Tabs } from '../../../../shared/components/ui/Tabs';

export const LeadsListPage: React.FC = () => {
    const { leads, loading, refresh, setFilter, totalPages, totalElements, currentPage } = useLeads();
    const navigate = useNavigate();
    const [searchTerm, setSearchTerm] = useState('');
    const [selectedIds, setSelectedIds] = useState<number[]>([]);

    // Estados para modales
    const [deleteModalOpen, setDeleteModalOpen] = useState(false);
    const [changeStatusModalOpen, setChangeStatusModalOpen] = useState(false);
    const [showExportMenu, setShowExportMenu] = useState(false);

    // Estados de acción
    const [leadToDelete, setLeadToDelete] = useState<number | null>(null);
    const [isDeleting, setIsDeleting] = useState(false);
    const [isChangingStatus, setIsChangingStatus] = useState(false);
    const [isExporting, setIsExporting] = useState(false);

    // Manejo de búsqueda con debounce simple
    const handleSearch = (e: React.ChangeEvent<HTMLInputElement>) => {
        const value = e.target.value;
        setSearchTerm(value);

        // Debounce simple - actualiza después de 500ms
        const timeoutId = setTimeout(() => {
            setFilter('search', value);
        }, 500);

        return () => clearTimeout(timeoutId);
    };

    // Funciones de paginación
    const goToPage = (page: number) => {
        if (page >= 0 && page < totalPages) {
            setFilter('page', page);
        }
    };

    // Manejadores de eliminación
    const handleDeleteSingle = (id: number) => {
        setLeadToDelete(id);
        setDeleteModalOpen(true);
    };

    const handleDeleteMultiple = () => {
        if (selectedIds.length > 0) {
            setLeadToDelete(null); // null indica eliminación múltiple
            setDeleteModalOpen(true);
        }
    };

    const confirmDelete = async () => {
        setIsDeleting(true);
        try {
            if (leadToDelete !== null) {
                // Eliminar un solo lead
                await leadsApi.delete(leadToDelete);
            } else {
                // Eliminar múltiples leads
                await leadsApi.deleteBatch(selectedIds);
            }

            // Refrescar la lista y limpiar selección
            await refresh();
            setSelectedIds([]);
            setDeleteModalOpen(false);
        } catch (error) {
            console.error('Error al eliminar lead(s):', error);
            alert('Hubo un error al eliminar los leads. Por favor intenta de nuevo.');
        } finally {
            setIsDeleting(false);
            setLeadToDelete(null);
        }
    };

    // Manejador de cambio de estado
    const confirmChangeStatus = async (nuevoEstado: string, motivo?: string) => {
        setIsChangingStatus(true);
        // Si no hay motivo, usar uno por defecto
        const motivoFinal = motivo || "Actualización masiva de estado desde el listado";

        try {
            const response: any = await leadsApi.updateStatusBatch(selectedIds, nuevoEstado, motivoFinal);

            // Refrescar la lista y limpiar selección
            await refresh();
            setSelectedIds([]);
            setChangeStatusModalOpen(false);

            // Mostrar mensaje de éxito
            const responseData = response.data;
            if (responseData?.data) {
                const { actualizados, total } = responseData.data;
                alert(`Se actualizaron ${actualizados} de ${total} lead(s) correctamente`);
            }
        } catch (error) {
            console.error('Error al cambiar estado de lead(s):', error);
            alert('Hubo un error al cambiar el estado de los leads. Por favor intenta de nuevo.');
        } finally {
            setIsChangingStatus(false);
        }
    };

    // Manejadores de exportación
    const handleExportAll = async () => {
        setShowExportMenu(false);
        setIsExporting(true);
        try {
            // Get current filter values from the selects
            const estadoSelect = document.querySelector('select')?.nextElementSibling as HTMLSelectElement;
            const fuenteSelect = document.querySelector('select') as HTMLSelectElement;

            await leadsApi.exportAll(
                estadoSelect?.value || undefined,
                searchTerm || undefined,
                fuenteSelect?.value || undefined
            );
        } catch (error) {
            console.error('Error al exportar:', error);
            alert('Error al exportar los leads');
        } finally {
            setIsExporting(false);
        }
    };

    const handleExportSelected = async () => {
        if (selectedIds.length === 0) return;
        setShowExportMenu(false);
        setIsExporting(true);
        try {
            await leadsApi.exportSelected(selectedIds);
        } catch (error) {
            console.error('Error al exportar:', error);
            alert('Error al exportar los leads seleccionados');
        } finally {
            setIsExporting(false);
        }
    };


    // Obtener los objetos lead seleccionados
    const selectedLeads = leads.filter(lead => selectedIds.includes(lead.id));

    return (
        <div className="space-y-6">
            {/* Nivel 2: Header Principal */}
            <header className="flex justify-between items-center">
                <div>
                    <h1 className="text-3xl font-bold text-dark">Gestión de Leads</h1>
                    <p className="text-gray-500 mt-1">Administra, cualifica y procesa todos los prospectos entrantes.</p>
                </div>
                <div className="flex space-x-3">
                    <Button variant="secondary" onClick={() => navigate('/leads/import')}>
                        <span className="material-symbols-outlined text-lg mr-1.5">upload</span>
                        Importar Leads
                    </Button>
                    <Button variant="primary" onClick={() => navigate('/leads/new')}>
                        <span className="material-symbols-outlined text-lg mr-1.5">add</span>
                        Registrar Lead
                    </Button>
                </div>
            </header>

            {/* Nivel 2.5: Tabs de Navegación */}
            <Tabs
                items={[
                    { label: 'Listado de Leads', value: 'list' },
                    { label: 'Monitor de Importaciones', value: 'import' }
                ]}
                activeValue="list"
                onChange={(val) => val === 'import' && navigate('/leads/import')}
            />

            {/* Nivel 3: Contenido Principal (Card) */}
            <div className="bg-white rounded-lg shadow-card border border-separator">

                {/* Barra de Herramientas (Filtros y Búsqueda) */}
                <div className="p-5 border-b border-separator flex flex-col sm:flex-row justify-between items-center gap-4">

                    {/* Buscador */}
                    <div className="relative w.full sm:w-96">
                        <span className="material-symbols-outlined absolute left-3 top-2.5 text-gray-400">search</span>
                        <input
                            type="text"
                            placeholder="Buscar por nombre, email o teléfono..."
                            className="w-full pl-10 pr-4 py-2 border border-separator rounded-lg focus:ring-2 focus:ring-primary focus:outline-none transition-all"
                            value={searchTerm}
                            onChange={handleSearch}
                        />
                    </div>

                    {/* Filtros Rápidos */}
                    <div className="flex items-center gap-2">
                        <select
                            className="border border-separator rounded-lg px-3 py-2 text-sm text-gray-600 focus:outline-none focus:border-primary"
                            onChange={(e) => setFilter('fuenteTipo', e.target.value)}
                        >
                            <option value="">Todas las Fuentes</option>
                            <option value="WEB">Formulario Web</option>
                            <option value="IMPORTACION">Importación Masiva</option>
                        </select>

                        <select
                            className="border border-separator rounded-lg px-3 py-2 text-sm text-gray-600 focus:outline-none focus:border-primary"
                            onChange={(e) => setFilter('estado', e.target.value)}
                        >
                            <option value="">Todos los Estados</option>
                            <option value="NUEVO">Nuevos</option>
                            <option value="CALIFICADO">Calificados</option>
                            <option value="DESCARTADO">Descartados</option>
                        </select>

                        <Button variant="secondary" onClick={refresh} title="Actualizar lista">
                            <span className="material-symbols-outlined text-xl">refresh</span>
                        </Button>

                        {/* Botón de Exportar con Dropdown */}
                        <div className="relative">
                            <Button
                                variant="secondary"
                                onClick={() => setShowExportMenu(!showExportMenu)}
                                disabled={isExporting}
                                title="Exportar leads"
                            >
                                <span className="material-symbols-outlined text-xl mr-1.5">download</span>
                                Exportar
                            </Button>

                            {showExportMenu && (
                                <div className="absolute right-0 mt-2 w-64 bg-white rounded-lg shadow-lg border border-separator z-10">
                                    <button
                                        onClick={handleExportAll}
                                        className="w-full text-left px-4 py-3 hover:bg-gray-50 transition-colors flex items-center gap-3 border-b border-separator"
                                    >
                                        <span className="material-symbols-outlined text-primary">file_download</span>
                                        <div>
                                            <div className="font-medium text-dark">Exportar Todos</div>
                                            <div className="text-xs text-gray-500">Exporta todos los leads filtrados</div>
                                        </div>
                                    </button>
                                    <button
                                        onClick={handleExportSelected}
                                        disabled={selectedIds.length === 0}
                                        className={`w-full text-left px-4 py-3 hover:bg-gray-50 transition-colors flex items-center gap-3 ${selectedIds.length === 0 ? 'opacity-50 cursor-not-allowed' : ''
                                            }`}
                                    >
                                        <span className="material-symbols-outlined text-primary">checklist</span>
                                        <div>
                                            <div className="font-medium text-dark">
                                                Exportar Seleccionados ({selectedIds.length})
                                            </div>
                                            <div className="text-xs text-gray-500">Exporta solo los leads marcados</div>
                                        </div>
                                    </button>
                                </div>
                            )}
                        </div>
                    </div>
                </div>

                {/* Barra de acciones de selección */}
                {selectedIds.length > 0 && (
                    <div className="p-4 bg-blue-50 border-b border-blue-200 flex items-center justify-between">
                        <span className="text-sm font-medium text-blue-900">
                            {selectedIds.length} lead(s) seleccionado(s)
                        </span>
                        <div className="flex gap-2">
                            <Button
                                variant="secondary"
                                onClick={() => setSelectedIds([])}
                            >
                                Limpiar selección
                            </Button>
                            <Button
                                variant="primary"
                                onClick={() => setChangeStatusModalOpen(true)}
                            >
                                <span className="material-symbols-outlined text-lg mr-1.5">sync_alt</span>
                                Cambiar Estado
                            </Button>
                            <Button
                                variant="danger"
                                onClick={handleDeleteMultiple}
                            >
                                <span className="material-symbols-outlined text-lg mr-1.5">delete</span>
                                Eliminar seleccionados
                            </Button>
                        </div>
                    </div>
                )}

                {/* Tabla de Datos */}
                <LeadTable
                    leads={leads}
                    isLoading={loading}
                    onViewDetail={(id) => navigate(`/leads/${id}`)}
                    selectedIds={selectedIds}
                    onSelectionChange={setSelectedIds}
                    onDelete={handleDeleteSingle}
                />

                {/* Paginación (Footer) */}
                <div className="p-5 border-t border-separator flex flex-col sm:flex-row justify-between items-center gap-4 text-sm">
                    <div className="text-gray-600">
                        Mostrando <span className="font-semibold">{leads.length}</span> de{' '}
                        <span className="font-semibold">{totalElements}</span> resultados totales
                        {totalPages > 0 && (
                            <span className="text-gray-400 ml-2">
                                (Página {currentPage + 1} de {totalPages})
                            </span>
                        )}
                    </div>

                    {/* Controles de Paginación */}
                    <div className="flex items-center space-x-2">
                        <button
                            className="px-3 py-1.5 border border-separator rounded-md bg-white hover:bg-gray-50 text-gray-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                            onClick={() => goToPage(0)}
                            disabled={currentPage === 0 || loading}
                        >
                            &laquo; Inicio
                        </button>
                        <button
                            className="px-3 py-1.5 border border-separator rounded-md bg-white hover:bg-gray-50 text-gray-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                            onClick={() => goToPage(currentPage - 1)}
                            disabled={currentPage === 0 || loading}
                        >
                            &lsaquo; Anterior
                        </button>

                        {/* Números de página */}
                        <div className="hidden sm:flex items-center space-x-1">
                            {Array.from({ length: Math.min(totalPages, 5) }, (_, i) => {
                                let pageNum: number;
                                if (totalPages <= 5) {
                                    pageNum = i;
                                } else if (currentPage < 3) {
                                    pageNum = i;
                                } else if (currentPage > totalPages - 3) {
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
                </div>

            </div>

            {/* Modal de confirmación de eliminación */}
            <DeleteLeadsModal
                isOpen={deleteModalOpen}
                leadCount={leadToDelete !== null ? 1 : selectedIds.length}
                onClose={() => {
                    setDeleteModalOpen(false);
                    setLeadToDelete(null);
                }}
                onConfirm={confirmDelete}
                isLoading={isDeleting}
            />

            {/* Modal de cambio de estado */}
            <ChangeStatusModal
                isOpen={changeStatusModalOpen}
                selectedLeads={selectedLeads}
                onClose={() => setChangeStatusModalOpen(false)}
                onConfirm={confirmChangeStatus}
                isLoading={isChangingStatus}
            />
        </div>
    );
};
