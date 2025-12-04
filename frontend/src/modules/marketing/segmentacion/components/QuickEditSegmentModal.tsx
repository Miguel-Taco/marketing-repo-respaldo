import React, { useState, useEffect } from 'react';
import { Button } from '../../../../shared/components/ui/Button';
import { FilterInfoDisplay } from './FilterInfoDisplay';
import { FilterDefinition } from './FilterRow';
import { segmentacionApi } from '../services/segmentacion.api';
import { Segmento, SegmentoEstado } from '../types/segmentacion.types';

interface QuickEditSegmentModalProps {
    isOpen: boolean;
    segment: Segmento | null; // Changed from segmentId to segment object
    onClose: () => void;
    onSave: (updatedSegment: Segmento) => void;
    onAdvancedEdit: (segmentId: number) => void;
}

export const QuickEditSegmentModal: React.FC<QuickEditSegmentModalProps> = ({
    isOpen,
    segment,
    onClose,
    onSave,
    onAdvancedEdit
}) => {
    const [isSaving, setIsSaving] = useState(false);
    const [isExporting, setIsExporting] = useState(false);
    const [nombre, setNombre] = useState('');
    const [descripcion, setDescripcion] = useState('');
    const [estado, setEstado] = useState<SegmentoEstado>('ACTIVO');
    const [tipoAudiencia, setTipoAudiencia] = useState('');
    const [cantidadMiembros, setCantidadMiembros] = useState(0);
    const [filters, setFilters] = useState<FilterDefinition[]>([]);

    // Load data from cache when segment changes - NO API CALL
    useEffect(() => {
        if (isOpen && segment) {
            setNombre(segment.nombre);
            setDescripcion(segment.descripcion || '');
            setEstado(segment.estado);
            setTipoAudiencia(segment.tipoAudiencia);
            setCantidadMiembros(segment.cantidadMiembros || 0);

            // Convert rules to filters
            if (segment.reglaPrincipal && segment.reglaPrincipal.reglas) {
                const loadedFilters: FilterDefinition[] = segment.reglaPrincipal.reglas.map((regla: any) => ({
                    campo: regla.campo,
                    operador: regla.operador,
                    valorTexto: regla.valorTexto
                }));
                setFilters(loadedFilters);
            } else {
                setFilters([]);
            }
        }
    }, [isOpen, segment]);

    const handleSave = async () => {
        if (!nombre.trim()) {
            alert('El nombre del segmento es obligatorio');
            return;
        }

        if (!segment) return;

        setIsSaving(true);
        try {
            // Use quick update endpoint - only updates basic fields, NO rematerialization
            const updatedSegment = await segmentacionApi.quickUpdate(segment.id, {
                nombre,
                descripcion,
                estado
            });

            // Update only this segment in cache
            onSave(updatedSegment);
            onClose();
        } catch (error) {
            console.error('Error updating segment:', error);
            alert('Error al actualizar el segmento');
        } finally {
            setIsSaving(false);
        }
    };

    const handleAdvancedEdit = () => {
        if (segment) {
            onAdvancedEdit(segment.id);
        }
    };

    const handleExport = async () => {
        if (!segment) return;

        setIsExporting(true);
        try {
            await segmentacionApi.exportSegment(segment.id, segment.nombre);
        } catch (error) {
            console.error('Error exporting segment:', error);
            alert('Error al exportar el segmento');
        } finally {
            setIsExporting(false);
        }
    };

    if (!isOpen || !segment) return null;

    return (
        <div className="fixed inset-0 z-50 overflow-y-auto">
            {/* Backdrop */}
            <div
                className="fixed inset-0 bg-black bg-opacity-50 transition-opacity"
                onClick={onClose}
            />

            {/* Modal */}
            <div className="flex min-h-full items-center justify-center p-4">
                <div
                    className="relative bg-white rounded-xl shadow-2xl max-w-2xl w-full max-h-[90vh] overflow-y-auto"
                    onClick={(e) => e.stopPropagation()}
                >
                    {/* Header */}
                    <div className="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 flex items-center justify-between z-10">
                        <div className="flex items-center gap-3">
                            <span className="material-symbols-outlined text-primary text-3xl">edit_square</span>
                            <h2 className="text-2xl font-bold text-dark">Editar Segmento</h2>
                        </div>
                        <button
                            onClick={onClose}
                            className="text-gray-400 hover:text-gray-600 transition-colors"
                        >
                            <span className="material-symbols-outlined text-3xl">close</span>
                        </button>
                    </div>

                    {/* Content */}
                    <div className="px-6 py-6 space-y-6">
                        {/* Editable Fields */}
                        <div className="space-y-4">
                            <h3 className="text-lg font-semibold text-gray-800 flex items-center gap-2">
                                <span className="material-symbols-outlined text-primary">edit</span>
                                Información Editable
                            </h3>

                            {/* Nombre */}
                            <div>
                                <label htmlFor="nombre" className="block text-sm font-medium text-gray-700 mb-1">
                                    Nombre del Segmento <span className="text-red-500">*</span>
                                </label>
                                <input
                                    type="text"
                                    id="nombre"
                                    value={nombre}
                                    onChange={(e) => setNombre(e.target.value)}
                                    className="w-full px-4 py-2 border border-separator rounded-lg focus:ring-2 focus:ring-primary focus:outline-none"
                                    placeholder="Ej: Clientes VIP de Lima"
                                />
                            </div>

                            {/* Estado */}
                            <div>
                                <label htmlFor="estado" className="block text-sm font-medium text-gray-700 mb-1">
                                    Estado
                                </label>
                                <div className="relative">
                                    <select
                                        id="estado"
                                        value={estado}
                                        onChange={(e) => setEstado(e.target.value as SegmentoEstado)}
                                        className="w-full px-4 py-2 pr-10 border border-separator rounded-lg focus:ring-2 focus:ring-primary focus:outline-none appearance-none"
                                    >
                                        <option value="ACTIVO">Activo</option>
                                        <option value="INACTIVO">Inactivo</option>
                                    </select>
                                    <span className="material-symbols-outlined absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none text-gray-400">
                                        arrow_drop_down
                                    </span>
                                </div>
                            </div>

                            {/* Descripción */}
                            <div>
                                <label htmlFor="descripcion" className="block text-sm font-medium text-gray-700 mb-1">
                                    Descripción
                                </label>
                                <textarea
                                    id="descripcion"
                                    value={descripcion}
                                    onChange={(e) => setDescripcion(e.target.value)}
                                    rows={3}
                                    className="w-full px-4 py-2 border border-separator rounded-lg focus:ring-2 focus:ring-primary focus:outline-none resize-none"
                                    placeholder="Describe el objetivo del segmento"
                                />
                            </div>
                        </div>

                        {/* Read-only Information */}
                        <div className="space-y-4 pt-4 border-t border-gray-200">
                            <h3 className="text-lg font-semibold text-gray-800 flex items-center gap-2">
                                <span className="material-symbols-outlined text-gray-500">info</span>
                                Información de Solo Lectura
                            </h3>

                            <div className="grid grid-cols-2 gap-4">
                                {/* Tipo de Audiencia */}
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">
                                        Tipo de Audiencia
                                    </label>
                                    <div className="px-4 py-2 bg-gray-50 border border-gray-200 rounded-lg text-gray-600">
                                        {tipoAudiencia}
                                    </div>
                                </div>

                                {/* Cantidad de Miembros */}
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">
                                        Miembros
                                    </label>
                                    <div className="px-4 py-2 bg-gray-50 border border-gray-200 rounded-lg text-gray-600 font-semibold">
                                        {cantidadMiembros.toLocaleString()}
                                    </div>
                                </div>
                            </div>
                        </div>

                        {/* Filters Display */}
                        <div className="pt-4 border-t border-gray-200">
                            <FilterInfoDisplay filters={filters} />
                        </div>
                    </div>

                    {/* Footer */}
                    <div className="sticky bottom-0 bg-gray-50 border-t border-gray-200 px-6 py-4 flex items-center justify-between gap-3">
                        <div className="flex gap-3">
                            <Button
                                variant="secondary"
                                onClick={handleAdvancedEdit}
                                disabled={isSaving || isExporting}
                                className="flex items-center gap-2"
                            >
                                <span className="material-symbols-outlined text-lg">tune</span>
                                Edición Avanzada
                            </Button>
                            <Button
                                variant="secondary"
                                onClick={handleExport}
                                disabled={isSaving}
                                isLoading={isExporting}
                                className="flex items-center gap-2"
                            >
                                <span className="material-symbols-outlined text-lg">download</span>
                                Exportar a Excel
                            </Button>
                        </div>

                        <div className="flex gap-3">
                            <Button
                                variant="secondary"
                                onClick={onClose}
                                disabled={isSaving || isExporting}
                            >
                                Cancelar
                            </Button>
                            <Button
                                variant="primary"
                                onClick={handleSave}
                                isLoading={isSaving}
                                disabled={isExporting}
                            >
                                <span className="material-symbols-outlined text-lg mr-1">check</span>
                                Aceptar
                            </Button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};
