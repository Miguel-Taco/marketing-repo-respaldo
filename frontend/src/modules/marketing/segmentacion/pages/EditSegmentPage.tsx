import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Button } from '../../../../shared/components/ui/Button';
import { FilterBuilder } from '../components/FilterBuilder';
import { SegmentPreviewTable } from '../components/SegmentPreviewTable';
import { FilterDefinition } from '../components/FilterRow';
import { SavingProgressModal } from '../components/SavingProgressModal';
import { ConfirmCancelModal } from '../components/ConfirmCancelModal';
import { segmentacionApi } from '../services/segmentacion.api';
import { leadsApi } from '../../leads/services/leads.api';
import { useSegmentosContext } from '../context/SegmentosContext';
import { SegmentoEstado } from '../types/segmentacion.types';

export const EditSegmentPage: React.FC = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { updateSegmento } = useSegmentosContext();
    const [isLoading, setIsLoading] = useState(false);
    const [isLoadingData, setIsLoadingData] = useState(true);
    const [isPreviewLoading, setIsPreviewLoading] = useState(false);
    const [isConfirmCancelOpen, setIsConfirmCancelOpen] = useState(false);
    const [savingProgress, setSavingProgress] = useState(0);
    const [savingMessage, setSavingMessage] = useState('');

    // Form state
    const [nombre, setNombre] = useState('');
    const [descripcion, setDescripcion] = useState('');
    const [tipoAudiencia, setTipoAudiencia] = useState<'LEAD' | 'CLIENTE' | 'MIXTO' | ''>('');
    const [estado, setEstado] = useState<SegmentoEstado>('ACTIVO');
    const [filters, setFilters] = useState<FilterDefinition[]>([]);

    // Preview state
    const [previewMembers, setPreviewMembers] = useState<any[]>([]);
    const [previewCount, setPreviewCount] = useState(0);
    const [leadIds, setLeadIds] = useState<number[]>([]);

    // Load segment data on mount
    useEffect(() => {
        const loadSegmentData = async () => {
            if (!id) return;

            setIsLoadingData(true);
            try {
                console.log('Loading segment with ID:', id);
                // http.get already returns .data, not the full axios response
                const segment = await segmentacionApi.getById(parseInt(id));
                console.log('Segment data:', segment);

                setNombre(segment.nombre);
                setDescripcion(segment.descripcion || '');
                setTipoAudiencia(segment.tipoAudiencia);
                setEstado(segment.estado);

                // Convert rules to filters
                if (segment.reglaPrincipal && segment.reglaPrincipal.reglas) {
                    const loadedFilters: FilterDefinition[] = segment.reglaPrincipal.reglas.map((regla: any) => ({
                        campo: regla.campo,
                        operador: regla.operador,
                        valorTexto: regla.valorTexto
                    }));
                    setFilters(loadedFilters);
                }
            } catch (error: any) {
                console.error('Error loading segment:', error);
                console.error('Error details:', error);
                alert('Error al cargar el segmento: ' + (error.message || 'Error desconocido'));
                navigate('/marketing/segmentacion');
            } finally {
                setIsLoadingData(false);
            }
        };

        loadSegmentData();
    }, [id, navigate]);

    // Function to fetch preview data
    const fetchPreview = useCallback(async () => {
        if (!tipoAudiencia) {
            setPreviewMembers([]);
            setPreviewCount(0);
            setLeadIds([]);
            return;
        }

        setIsPreviewLoading(true);
        try {
            const reglaPrincipal = filters.length > 0 ? {
                tipo: 'AND' as const,
                reglas: filters.map(f => ({
                    tipo: 'SIMPLE' as const,
                    campo: f.campo,
                    operador: f.operador,
                    valorTexto: f.valorTexto,
                }))
            } : undefined;

            const previewResponse = await segmentacionApi.previewTemporal({
                nombre: nombre || 'Preview',
                tipoAudiencia,
                reglaPrincipal
            });

            const responseData = previewResponse.data || previewResponse;
            const count = responseData.count || 0;
            const ids = responseData.leadIds || [];

            setPreviewCount(count);
            setLeadIds(ids);

            if (ids.length > 0) {
                const leadsToFetch = ids.slice(0, 10);
                const leadPromises = leadsToFetch.map(id => leadsApi.getById(id));
                const leadResponses = await Promise.all(leadPromises);

                const members = leadResponses.map(response => {
                    const lead = response.data;
                    return {
                        id: lead.id,
                        nombre: lead.nombreCompleto,
                        edad: lead.demograficos?.edad || 0,
                        correo: lead.contacto?.email || '',
                        telefono: lead.contacto?.telefono || '',
                        direccion: lead.demograficos?.distritoNombre
                            ? `${lead.demograficos.distritoNombre}, ${lead.demograficos.provinciaNombre || ''}`
                            : 'No especificado'
                    };
                });

                setPreviewMembers(members);
            } else {
                setPreviewMembers([]);
            }
        } catch (error: any) {
            console.error('Error fetching preview:', error);
            setPreviewMembers([]);
            setPreviewCount(0);
            setLeadIds([]);
        } finally {
            setIsPreviewLoading(false);
        }
    }, [tipoAudiencia, filters, nombre]);

    const handleSave = async () => {
        if (!nombre || !tipoAudiencia || !id) {
            alert('Por favor completa los campos obligatorios');
            return;
        }

        setIsLoading(true);
        setSavingProgress(0);
        setSavingMessage('Preparando actualización...');

        try {
            setSavingProgress(10);
            setSavingMessage('Validando cambios...');
            await new Promise(resolve => setTimeout(resolve, 200));

            const reglaPrincipal = filters.length > 0 ? {
                tipo: 'AND' as const,
                reglas: filters.map(f => ({
                    tipo: 'SIMPLE' as const,
                    campo: f.campo,
                    operador: f.operador,
                    valorTexto: f.valorTexto,
                }))
            } : undefined;

            setSavingProgress(25);
            setSavingMessage('Actualizando segmento...');
            await new Promise(resolve => setTimeout(resolve, 100));

            setSavingProgress(40);
            const updatedSegment = await segmentacionApi.update(parseInt(id), {
                nombre,
                descripcion: descripcion || undefined,
                tipoAudiencia,
                estado,
                reglaPrincipal
            });

            setSavingProgress(60);
            setSavingMessage('Recalculando miembros con nuevos filtros...');
            await new Promise(resolve => setTimeout(resolve, 300));

            setSavingProgress(85);
            setSavingMessage('Finalizando actualización...');
            await new Promise(resolve => setTimeout(resolve, 200));

            updateSegmento(updatedSegment);

            setSavingProgress(100);
            setSavingMessage('¡Segmento actualizado exitosamente!');
            await new Promise(resolve => setTimeout(resolve, 400));

            navigate('/marketing/segmentacion');
        } catch (error: any) {
            console.error('Error updating segment:', error);
            setSavingProgress(0);
            alert('Error al actualizar el segmento: ' + (error.message || 'Error desconocido'));
        } finally {
            setIsLoading(false);
        }
    };

    const handleRecoverSegment = async () => {
        if (!id) return;

        try {
            await segmentacionApi.update(parseInt(id), { estado: 'ACTIVO' });
            setEstado('ACTIVO');
            alert('Segmento recuperado exitosamente');
        } catch (error) {
            console.error('Error recovering segment:', error);
            alert('Error al recuperar el segmento');
        }
    };

    const handleCancel = () => {
        setIsConfirmCancelOpen(true);
    };

    const confirmCancel = () => {
        setIsConfirmCancelOpen(false);
        navigate('/marketing/segmentacion');
    };

    if (isLoadingData) {
        return (
            <div className="flex items-center justify-center h-96">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4"></div>
                    <p className="text-gray-600">Cargando segmento...</p>
                </div>
            </div>
        );
    }

    return (
        <div className="space-y-6">
            {/* Header */}
            <header className="flex flex-col md:flex-row justify-between items-start md:items-center pb-4 border-b border-gray-300">
                <h1 className="text-3xl font-bold text-dark mb-4 md:mb-0">Editar segmento</h1>
                <div className="flex space-x-3 w-full md:w-auto">
                    <button
                        onClick={handleCancel}
                        className="text-gray-600 px-5 py-2 text-sm font-semibold rounded-full hover:text-primary transition duration-150 border border-transparent hover:border-gray-300"
                    >
                        Cancelar
                    </button>
                    <Button
                        variant="primary"
                        onClick={handleSave}
                        isLoading={isLoading}
                        className="w-full md:w-auto"
                    >
                        <span className="material-symbols-outlined text-lg mr-1">save</span>
                        Guardar cambios
                    </Button>
                </div>
            </header>

            {/* Deleted Segment Banner */}
            {estado === 'ELIMINADO' && (
                <div className="bg-red-50 border border-red-200 rounded-xl p-4 flex items-center justify-between">
                    <div className="flex items-center">
                        <span className="material-symbols-outlined text-red-500 mr-3">warning</span>
                        <div>
                            <p className="font-semibold text-red-800">Este segmento está eliminado</p>
                            <p className="text-sm text-red-600">Recupéralo para poder editarlo y usarlo nuevamente</p>
                        </div>
                    </div>
                    <Button
                        variant="primary"
                        onClick={handleRecoverSegment}
                        className="bg-red-600 hover:bg-red-700"
                    >
                        <span className="material-symbols-outlined text-lg mr-1">restore</span>
                        Recuperar
                    </Button>
                </div>
            )}

            {/* Two-Column Layout */}
            <div className="grid grid-cols-1 lg:grid-cols-5 gap-8">

                {/* Left Column (2/5) - Form and Filters */}
                <div className="lg:col-span-2 space-y-6">

                    {/* General Data Form */}
                    <div className="bg-white p-6 rounded-xl shadow-lg border border-separator">
                        <h2 className="text-xl font-semibold text-dark mb-6">Datos generales del segmento</h2>
                        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">

                            {/* Segment Name */}
                            <div>
                                <label htmlFor="segmentName" className="block text-sm font-medium text-gray-700 mb-1">
                                    Nombre del segmento <span className="text-red-500">*</span>
                                </label>
                                <input
                                    type="text"
                                    id="segmentName"
                                    value={nombre}
                                    onChange={(e) => setNombre(e.target.value)}
                                    placeholder="Ej: Clientes VIP de Lima"
                                    className="w-full px-4 py-2 border border-separator rounded-lg focus:ring-2 focus:ring-primary focus:outline-none"
                                    required
                                    disabled={estado === 'ELIMINADO'}
                                />
                            </div>

                            {/* Status */}
                            <div>
                                <label htmlFor="status" className="block text-sm font-medium text-gray-700 mb-1">
                                    Estado
                                </label>
                                <div className="relative">
                                    <select
                                        id="status"
                                        value={estado}
                                        onChange={(e) => setEstado(e.target.value as SegmentoEstado)}
                                        className="w-full px-4 py-2 pr-10 border border-separator rounded-lg focus:ring-2 focus:ring-primary focus:outline-none appearance-none"
                                        disabled={estado === 'ELIMINADO'}
                                    >
                                        <option value="ACTIVO">Activo</option>
                                        <option value="INACTIVO">Inactivo</option>
                                    </select>
                                    <span className="material-symbols-outlined absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none text-gray-400">
                                        arrow_drop_down
                                    </span>
                                </div>
                                <p className="text-xs text-gray-500 mt-1">
                                    {estado === 'ACTIVO' ? 'El segmento está activo y disponible' :
                                        estado === 'INACTIVO' ? 'El segmento está pausado' :
                                            'El segmento está eliminado'}
                                </p>
                            </div>

                            {/* Audience Type (Read-only) */}
                            <div>
                                <label htmlFor="audienceType" className="block text-sm font-medium text-gray-700 mb-1">
                                    Tipo de audiencia
                                </label>
                                <input
                                    type="text"
                                    id="audienceType"
                                    value={tipoAudiencia}
                                    className="w-full px-4 py-2 border border-separator rounded-lg bg-gray-50 text-gray-600"
                                    disabled
                                />
                                <p className="text-xs text-gray-500 mt-1">El tipo de audiencia no se puede modificar</p>
                            </div>

                            {/* Description */}
                            <div className="lg:col-span-2">
                                <label htmlFor="description" className="block text-sm font-medium text-gray-700 mb-1">
                                    Descripción (Opcional)
                                </label>
                                <textarea
                                    id="description"
                                    value={descripcion}
                                    onChange={(e) => setDescripcion(e.target.value)}
                                    rows={2}
                                    placeholder="Describe el objetivo del segmento"
                                    className="w-full px-4 py-2 border border-separator rounded-lg focus:ring-2 focus:ring-primary focus:outline-none resize-none"
                                    disabled={estado === 'ELIMINADO'}
                                />
                            </div>

                        </div>
                    </div>

                    {/* Filter Builder */}
                    <FilterBuilder
                        filters={filters}
                        onFiltersChange={setFilters}
                        onPreview={fetchPreview}
                        isPreviewLoading={isPreviewLoading}
                        disabled={estado === 'ELIMINADO'}
                    />

                </div>

                {/* Right Column (3/5) - Preview */}
                <div className="lg:col-span-3">
                    <SegmentPreviewTable
                        members={previewMembers}
                        totalCount={previewCount}
                        isLoading={isPreviewLoading}
                    />
                </div>
            </div>

            {/* Saving Progress Modal */}
            <SavingProgressModal
                isOpen={isLoading}
                progress={savingProgress}
                message={savingMessage}
            />

            {/* Confirm Cancel Modal */}
            <ConfirmCancelModal
                isOpen={isConfirmCancelOpen}
                onConfirm={confirmCancel}
                onCancel={() => setIsConfirmCancelOpen(false)}
            />
        </div>
    );
};
