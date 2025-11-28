import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from '../../../../shared/components/ui/Button';
import { FilterBuilder } from '../components/FilterBuilder';
import { SegmentPreviewTable } from '../components/SegmentPreviewTable';
import { FilterDefinition } from '../components/FilterRow';
import { TemplateModal } from '../components/TemplateModal';
import { SavingProgressModal } from '../components/SavingProgressModal';
import { ConfirmCancelModal } from '../components/ConfirmCancelModal';
import { segmentacionApi } from '../services/segmentacion.api';
import { leadsApi } from '../../leads/services/leads.api';
import { useSegmentosContext } from '../context/SegmentosContext';

export const CreateSegmentPage: React.FC = () => {
    const navigate = useNavigate();
    const { addSegmento } = useSegmentosContext();
    const [isLoading, setIsLoading] = useState(false);
    const [isPreviewLoading, setIsPreviewLoading] = useState(false);
    const [isTemplateModalOpen, setIsTemplateModalOpen] = useState(false);
    const [isConfirmCancelOpen, setIsConfirmCancelOpen] = useState(false);
    const [savingProgress, setSavingProgress] = useState(0);
    const [savingMessage, setSavingMessage] = useState('');

    // Form state
    const [nombre, setNombre] = useState('');
    const [descripcion, setDescripcion] = useState('');
    const [tipoAudiencia, setTipoAudiencia] = useState<'LEAD' | 'CLIENTE' | 'MIXTO' | ''>('');
    const [filters, setFilters] = useState<FilterDefinition[]>([]);

    // Preview state
    const [previewMembers, setPreviewMembers] = useState<any[]>([]);
    const [previewCount, setPreviewCount] = useState(0);
    const [leadIds, setLeadIds] = useState<number[]>([]);

    // Function to fetch preview data
    const fetchPreview = useCallback(async () => {
        // Only fetch if we have audience type
        if (!tipoAudiencia) {
            setPreviewMembers([]);
            setPreviewCount(0);
            setLeadIds([]);
            return;
        }

        setIsPreviewLoading(true);
        try {
            // Build the rule structure from filters
            const reglaPrincipal = filters.length > 0 ? {
                tipo: 'AND' as const,
                reglas: filters.map(f => ({
                    tipo: 'SIMPLE' as const,
                    campo: f.campo,
                    operador: f.operador,
                    valorTexto: f.valorTexto,
                }))
            } : undefined;

            console.log('Sending preview request:', { nombre: nombre || 'Preview', tipoAudiencia, reglaPrincipal });

            // Call preview API
            const previewResponse = await segmentacionApi.previewTemporal({
                nombre: nombre || 'Preview',
                tipoAudiencia,
                reglaPrincipal
            });

            console.log('Preview response received:', previewResponse);
            console.log('Preview response.data:', previewResponse.data);

            // El backend devuelve { count, leadIds } directamente, no anidado en data
            const responseData = previewResponse.data || previewResponse;
            const count = responseData.count || 0;
            const ids = responseData.leadIds || [];

            console.log('Extracted count:', count, 'leadIds:', ids);

            setPreviewCount(count);
            setLeadIds(ids);

            // Fetch actual lead data for the first 10 IDs
            if (ids.length > 0) {
                const leadsToFetch = ids.slice(0, 10);
                console.log('Fetching lead details for IDs:', leadsToFetch);

                const leadPromises = leadsToFetch.map(id => leadsApi.getById(id));
                const leadResponses = await Promise.all(leadPromises);

                console.log('Lead responses:', leadResponses);

                // Transform lead data to preview format
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

                console.log('Transformed members:', members);
                setPreviewMembers(members);
            } else {
                console.log('No lead IDs to fetch');
                setPreviewMembers([]);
            }
        } catch (error: any) {
            console.error('Error fetching preview:', error);
            console.error('Error details:', error.response?.data);
            setPreviewMembers([]);
            setPreviewCount(0);
            setLeadIds([]);
        } finally {
            setIsPreviewLoading(false);
        }
    }, [tipoAudiencia, filters, nombre]);

    const handleSave = async () => {
        if (!nombre || !tipoAudiencia) {
            alert('Por favor completa los campos obligatorios: Nombre y Tipo de Audiencia');
            return;
        }

        setIsLoading(true);
        setSavingProgress(0);
        setSavingMessage('Preparando datos...');

        try {
            // Step 1: Validate and prepare (0-25%)
            setSavingProgress(5);
            setSavingMessage('Validando datos del segmento...');
            await new Promise(resolve => setTimeout(resolve, 200));

            setSavingProgress(15);
            await new Promise(resolve => setTimeout(resolve, 150));

            // Build the rule structure from filters
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
            setSavingMessage('Creando segmento...');
            await new Promise(resolve => setTimeout(resolve, 100));

            // Step 2: Create segment (25-50%)
            setSavingProgress(30);
            const response = await segmentacionApi.create({
                nombre,
                descripcion: descripcion || undefined,
                tipoAudiencia,
                reglaPrincipal
            });

            setSavingProgress(50);
            setSavingMessage('Aplicando filtros y reglas...');
            await new Promise(resolve => setTimeout(resolve, 200));

            // Get the created segment ID
            let segmentId: number;
            if (response && 'data' in response) {
                segmentId = response.data.id;
            } else if (response) {
                segmentId = (response as any).id;
            } else {
                throw new Error('No se pudo obtener el ID del segmento creado');
            }

            // Step 3: Processing members (50-85%)
            setSavingProgress(60);
            setSavingMessage('Procesando miembros del segmento...');
            await new Promise(resolve => setTimeout(resolve, 250));

            setSavingProgress(75);
            setSavingMessage('Guardando miembros en base de datos...');
            await new Promise(resolve => setTimeout(resolve, 200));

            // Step 4: Fetch complete data (85-100%)
            setSavingProgress(85);
            setSavingMessage('Finalizando creación...');

            const completeSegment = await segmentacionApi.getById(segmentId);

            setSavingProgress(95);
            await new Promise(resolve => setTimeout(resolve, 150));

            // Add to cache with complete data
            if (completeSegment && 'data' in completeSegment) {
                addSegmento(completeSegment.data);
            } else if (completeSegment) {
                addSegmento(completeSegment as any);
            }

            setSavingProgress(100);
            setSavingMessage('¡Segmento creado exitosamente!');
            await new Promise(resolve => setTimeout(resolve, 400));

            navigate('/marketing/segmentacion');
        } catch (error: any) {
            console.error('Error creating segment:', error);
            setSavingProgress(0);
            alert('Error al crear el segmento: ' + (error.message || 'Error desconocido'));
        } finally {
            setIsLoading(false);
        }
    };

    const handleCancel = () => {
        if (nombre || descripcion || filters.length > 0) {
            setIsConfirmCancelOpen(true);
        } else {
            navigate('/marketing/segmentacion');
        }
    };

    const confirmCancel = () => {
        setIsConfirmCancelOpen(false);
        navigate('/marketing/segmentacion');
    };

    const handleTemplateSelect = (template: any) => {
        // Apply template data to form
        setNombre(`${template.nombre} (Copia)`);
        setDescripcion(template.descripcion || '');
        setTipoAudiencia(template.tipoAudiencia);

        // Convert template rules to filters
        if (template.reglaPrincipal && template.reglaPrincipal.reglas) {
            const templateFilters: FilterDefinition[] = template.reglaPrincipal.reglas.map((regla: any) => ({
                campo: regla.campo,
                operador: regla.operador,
                valorTexto: regla.valorTexto
            }));
            setFilters(templateFilters);
        }
    };

    return (
        <div className="space-y-6">
            {/* Header */}
            <header className="flex flex-col md:flex-row justify-between items-start md:items-center pb-4 border-b border-gray-300">
                <h1 className="text-3xl font-bold text-dark mb-4 md:mb-0">Crear segmento</h1>
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
                        Guardar segmento
                    </Button>
                </div>
            </header>

            {/* Two-Column Layout */}
            <div className="grid grid-cols-1 lg:grid-cols-5 gap-8">

                {/* Left Column (2/5) - Form and Filters */}
                <div className="lg:col-span-2 space-y-6">

                    {/* Template Button */}
                    <button
                        type="button"
                        onClick={() => setIsTemplateModalOpen(true)}
                        className="w-full flex items-center justify-center p-3 rounded-xl bg-white border border-dashed border-gray-400 text-gray-600 font-semibold hover:bg-gray-50 transition duration-150 shadow-sm"
                    >
                        <span className="material-symbols-outlined text-lg mr-2">auto_fix_high</span>
                        Usar Plantilla
                    </button>

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
                                />
                            </div>

                            {/* Audience Type */}
                            <div>
                                <label htmlFor="audienceType" className="block text-sm font-medium text-gray-700 mb-1">
                                    Tipo de audiencia <span className="text-red-500">*</span>
                                </label>
                                <div className="relative">
                                    <select
                                        id="audienceType"
                                        value={tipoAudiencia}
                                        onChange={(e) => setTipoAudiencia(e.target.value as any)}
                                        className="w-full px-4 py-2 pr-10 border border-separator rounded-lg focus:ring-2 focus:ring-primary focus:outline-none appearance-none"
                                        required
                                    >
                                        <option value="">Selecciona un tipo de audiencia</option>
                                        <option value="LEAD">Lead</option>
                                        <option value="CLIENTE">Cliente</option>
                                        <option value="MIXTO">Mixto</option>
                                    </select>
                                    <span className="material-symbols-outlined absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none text-gray-400">
                                        arrow_drop_down
                                    </span>
                                </div>
                                <p className="text-xs text-gray-500 mt-1">Define el universo de contactos a segmentar.</p>
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
                                    placeholder="Describe el objetivo, por ejemplo: Leads que viven en Lima, tienen entre 25-35 años y son de género Femenino."
                                    className="w-full px-4 py-2 border border-separator rounded-lg focus:ring-2 focus:ring-primary focus:outline-none resize-none"
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

            {/* Template Selection Modal */}
            <TemplateModal
                isOpen={isTemplateModalOpen}
                onClose={() => setIsTemplateModalOpen(false)}
                onSelectTemplate={handleTemplateSelect}
            />

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
