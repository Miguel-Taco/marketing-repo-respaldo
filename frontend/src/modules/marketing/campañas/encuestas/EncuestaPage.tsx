import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from '../../../../shared/components/ui/Button';
import { Badge } from '../../../../shared/components/ui/Badge';
import { Tabs } from '../../../../shared/components/ui/Tabs';
import { Input } from '../../../../shared/components/ui/Input';
import { Select } from '../../../../shared/components/ui/Select';
import { encuestasApi } from './services/encuestas.api';
import { Encuesta } from './types';

export const EncuestaPage: React.FC = () => {
    const navigate = useNavigate();
    const [encuestas, setEncuestas] = useState<Encuesta[]>([]);
    const [loading, setLoading] = useState(true);
    const [filterEstado, setFilterEstado] = useState<string>('Todas');
    const [searchTerm, setSearchTerm] = useState('');
    const [activeTab, setActiveTab] = useState('Encuestas');

    // Pagination state
    const [currentPage, setCurrentPage] = useState(0);
    const pageSize = 10;

    // Selection state
    const [selectedIds, setSelectedIds] = useState<Set<number>>(new Set());

    useEffect(() => {
        loadEncuestas();
    }, []);

    const loadEncuestas = async () => {
        try {
            const data = await encuestasApi.getAll();
            setEncuestas(data);
        } catch (error) {
            console.error('Error loading encuestas:', error);
        } finally {
            setLoading(false);
        }
    };

    // Filtering
    const filteredEncuestas = encuestas.filter(encuesta => {
        const matchesEstado = filterEstado === 'Todas' || encuesta.estado === filterEstado.toUpperCase();
        const matchesSearch = encuesta.titulo.toLowerCase().includes(searchTerm.toLowerCase());
        return matchesEstado && matchesSearch;
    });

    // Pagination Logic
    const totalElements = filteredEncuestas.length;
    const totalPages = Math.ceil(totalElements / pageSize);
    const paginatedEncuestas = filteredEncuestas.slice(
        currentPage * pageSize,
        (currentPage + 1) * pageSize
    );

    const handlePageChange = (newPage: number) => {
        if (newPage >= 0 && newPage < totalPages) {
            setCurrentPage(newPage);
        }
    };

    // Selection Logic
    const handleSelectAll = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.checked) {
            const newSelected = new Set(selectedIds);
            paginatedEncuestas.forEach(enc => newSelected.add(enc.idEncuesta));
            setSelectedIds(newSelected);
        } else {
            const newSelected = new Set(selectedIds);
            paginatedEncuestas.forEach(enc => newSelected.delete(enc.idEncuesta));
            setSelectedIds(newSelected);
        }
    };

    const handleSelectRow = (id: number) => {
        const newSelected = new Set(selectedIds);
        if (newSelected.has(id)) {
            newSelected.delete(id);
        } else {
            newSelected.add(id);
        }
        setSelectedIds(newSelected);
    };

    const clearSelection = () => {
        setSelectedIds(new Set());
    };

    const isAllSelected = paginatedEncuestas.length > 0 && paginatedEncuestas.every(enc => selectedIds.has(enc.idEncuesta));

    // Reset pagination when filters change
    useEffect(() => {
        setCurrentPage(0);
        clearSelection();
    }, [filterEstado, searchTerm]);

    const totalEncuestas = encuestas.length;
    const encuestasActivas = encuestas.filter(e => e.estado === 'ACTIVA').length;
    const totalRespuestas = encuestas.reduce((acc, curr) => acc + (curr.totalRespuestas || 0), 0);

    return (
        <div className="space-y-6">
            {/* Header Principal */}
            <header className="flex justify-between items-center">
                <div>
                    <h1 className="text-3xl font-bold text-dark">Encuestas</h1>
                    <p className="text-gray-500 mt-1">{totalEncuestas} Encuestas</p>
                </div>
                <div className="flex items-center space-x-3">
                    <Button variant="primary" onClick={() => navigate('/encuestas/new')}>
                        <span className="material-symbols-outlined text-lg mr-1.5">add</span>
                        Nueva Encuesta
                    </Button>
                </div>
            </header>

            {/* Tabs de Navegación */}
            <Tabs
                items={[
                    { label: 'Encuestas', value: 'Encuestas' },
                    { label: 'Analíticas', value: 'Analíticas' }
                ]}
                activeValue={activeTab}
                onChange={setActiveTab}
            />

            {/* Stats Cards */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-100 flex flex-col items-center justify-center">
                    <span className="text-4xl font-bold text-gray-900">{totalEncuestas}</span>
                    <span className="text-sm text-gray-500 mt-1">Total Encuestas</span>
                </div>
                <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-100 flex flex-col items-center justify-center">
                    <span className="text-4xl font-bold text-gray-900">{encuestasActivas}</span>
                    <span className="text-sm text-gray-500 mt-1">Encuestas Activas</span>
                </div>
                <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-100 flex flex-col items-center justify-center">
                    <span className="text-4xl font-bold text-gray-900">{totalRespuestas.toLocaleString()}</span>
                    <span className="text-sm text-gray-500 mt-1">Total Respuestas</span>
                </div>
            </div>

            {/* Filters & Search */}
            <div className="bg-white p-4 rounded-lg shadow-sm border border-gray-100 flex flex-col md:flex-row justify-between items-center gap-4">
                {/* Search - Left */}
                <div className="w-full md:w-1/3 relative">
                    <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none">
                        search
                    </span>
                    <Input
                        placeholder="Buscar por título..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        className="w-full pl-10"
                    />
                </div>

                {/* Filter - Right */}
                <div className="w-full md:w-1/4">
                    <Select
                        options={[
                            { value: 'Todas', label: 'Todos los Estados' },
                            { value: 'ACTIVA', label: 'Activa' },
                            { value: 'BORRADOR', label: 'Borrador' },
                            { value: 'ARCHIVADA', label: 'Archivada' }
                        ]}
                        value={filterEstado}
                        onChange={(e) => setFilterEstado(e.target.value)}
                    />
                </div>
            </div>

            {/* Selection Bar */}
            {selectedIds.size > 0 && (
                <div className="p-4 bg-blue-50 border border-blue-200 rounded-lg flex items-center justify-between animate-fade-in">
                    <span className="text-sm font-medium text-blue-900">
                        {selectedIds.size} encuesta(s) seleccionada(s)
                    </span>
                    <div className="flex gap-2">
                        <Button
                            variant="secondary"
                            onClick={clearSelection}
                        >
                            Limpiar selección
                        </Button>
                        <Button
                            variant="danger"
                            onClick={() => console.log('Delete multiple')}
                        >
                            <span className="material-symbols-outlined text-lg mr-1">delete</span>
                            Eliminar seleccionados
                        </Button>
                    </div>
                </div>
            )}

            {/* Custom Table */}
            <div className="bg-white rounded-lg shadow-sm border border-separator overflow-hidden">
                <div className="overflow-x-auto">
                    <table className="w-full text-left border-collapse">
                        <thead className="bg-table-header">
                            <tr>
                                <th className="p-4 w-12">
                                    <input
                                        type="checkbox"
                                        className="rounded border-gray-300 text-blue-600 focus:ring-blue-500 h-4 w-4 cursor-pointer"
                                        checked={isAllSelected}
                                        onChange={handleSelectAll}
                                    />
                                </th>
                                <th className="p-4 text-sm font-semibold text-dark tracking-wide">Título / Descripción</th>
                                <th className="p-4 text-sm font-semibold text-dark tracking-wide">Estado</th>
                                <th className="p-4 text-sm font-semibold text-dark tracking-wide">Respuestas</th>
                                <th className="p-4 text-sm font-semibold text-dark tracking-wide">Fecha Creación</th>
                                <th className="p-4 text-sm font-semibold text-dark tracking-wide">Acciones</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-separator">
                            {loading ? (
                                <tr>
                                    <td colSpan={6} className="p-8 text-center text-gray-500">Cargando encuestas...</td>
                                </tr>
                            ) : paginatedEncuestas.length === 0 ? (
                                <tr>
                                    <td colSpan={6} className="p-8 text-center text-gray-500">No se encontraron encuestas</td>
                                </tr>
                            ) : (
                                paginatedEncuestas.map((encuesta) => (
                                    <tr key={encuesta.idEncuesta} className={`hover:bg-gray-50 transition-colors ${selectedIds.has(encuesta.idEncuesta) ? 'bg-blue-50/30' : ''}`}>
                                        <td className="p-4">
                                            <input
                                                type="checkbox"
                                                className="rounded border-gray-300 text-blue-600 focus:ring-blue-500 h-4 w-4 cursor-pointer"
                                                checked={selectedIds.has(encuesta.idEncuesta)}
                                                onChange={() => handleSelectRow(encuesta.idEncuesta)}
                                            />
                                        </td>
                                        <td className="p-4">
                                            <div className="flex flex-col">
                                                <span className="text-sm font-medium text-gray-900">
                                                    {encuesta.titulo}
                                                </span>
                                                <span className="text-xs text-gray-500 mt-0.5">
                                                    {encuesta.descripcion || 'Sin descripción'}
                                                </span>
                                            </div>
                                        </td>
                                        <td className="p-4">
                                            <Badge variant={
                                                encuesta.estado === 'ACTIVA' ? 'success' :
                                                    encuesta.estado === 'BORRADOR' ? 'default' : 'default'
                                            }>
                                                {encuesta.estado.charAt(0) + encuesta.estado.slice(1).toLowerCase()}
                                            </Badge>
                                        </td>
                                        <td className="p-4 text-sm text-gray-600">
                                            {(encuesta.totalRespuestas || 0).toLocaleString()}
                                        </td>
                                        <td className="p-4 text-sm text-gray-600">
                                            {new Date(encuesta.fechaModificacion).toLocaleDateString('es-ES', { day: '2-digit', month: 'short', year: 'numeric' })}
                                        </td>
                                        <td className="p-4">
                                            <div className="flex items-center gap-2">
                                                {encuesta.estado === 'BORRADOR' ? (
                                                    <button
                                                        className="font-medium text-blue-600 hover:underline"
                                                        onClick={() => navigate(`/encuestas/edit/${encuesta.idEncuesta}`)}
                                                    >
                                                        Editar
                                                    </button>
                                                ) : (
                                                    <button
                                                        className="font-medium text-blue-600 hover:underline"
                                                        onClick={() => console.log('View detail', encuesta.idEncuesta)}
                                                    >
                                                        Ver Detalle
                                                    </button>
                                                )}
                                                <button className="p-1.5 text-red-500 hover:bg-red-50 rounded transition-colors">
                                                    <span className="material-symbols-outlined text-xl">delete</span>
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>

                {/* Pagination Footer - Exact Leads Style */}
                <div className="p-5 border-t border-separator flex justify-between items-center text-sm">
                    <div className="text-gray-600">
                        Mostrando <span className="font-semibold">{paginatedEncuestas.length > 0 ? ((currentPage * pageSize) + 1) : 0}</span> de <span className="font-semibold">{totalElements}</span> resultados totales
                        {totalPages > 1 && (
                            <span className="ml-2">(Página {currentPage + 1} de {totalPages})</span>
                        )}
                    </div>

                    {/* Numeric Pagination */}
                    {totalPages > 1 && (
                        <div className="flex items-center gap-1">
                            <button
                                className="px-3 py-1.5 border border-separator rounded-md bg-white hover:bg-gray-50 text-gray-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                                onClick={() => handlePageChange(0)}
                                disabled={currentPage === 0}
                            >
                                « Inicio
                            </button>
                            <button
                                className="px-3 py-1.5 border border-separator rounded-md bg-white hover:bg-gray-50 text-gray-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                                onClick={() => handlePageChange(currentPage - 1)}
                                disabled={currentPage === 0}
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
                                                ? 'bg-blue-600 text-white border-blue-600'
                                                : 'bg-white hover:bg-gray-50 text-gray-600 border-separator'
                                                }`}
                                            onClick={() => handlePageChange(pageNum)}
                                        >
                                            {pageNum + 1}
                                        </button>
                                    );
                                })}
                            </div>

                            <button
                                className="px-3 py-1.5 border border-separator rounded-md bg-white hover:bg-gray-50 text-gray-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                                onClick={() => handlePageChange(currentPage + 1)}
                                disabled={currentPage >= totalPages - 1}
                            >
                                Siguiente &rsaquo;
                            </button>
                            <button
                                className="px-3 py-1.5 border border-separator rounded-md bg-white hover:bg-gray-50 text-gray-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                                onClick={() => handlePageChange(totalPages - 1)}
                                disabled={currentPage >= totalPages - 1}
                            >
                                Fin &raquo;
                            </button>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};
