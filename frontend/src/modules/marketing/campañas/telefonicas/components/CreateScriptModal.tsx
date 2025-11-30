import React, { useState, useEffect } from 'react';
import { X, Save, FileText, Settings, Eye } from 'lucide-react';
import { CreateGuionRequest, TIPOS_SECCION, TIPOS_LLAMADA, SeccionGuionDTO, GuionDTO } from '../types/guiones.types';
import { guionesApi } from '../services/guiones.api';
import { RichTextEditor } from './RichTextEditor';
import { MarkdownPreview } from './MarkdownPreview';

interface CreateScriptModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSuccess?: () => void;
    initialData?: GuionDTO | null;
    campaignId?: string; // Campaign context from URL
}

export const CreateScriptModal: React.FC<CreateScriptModalProps> = ({
    isOpen,
    onClose,
    onSuccess,
    initialData,
    campaignId,
}) => {
    const [activeTab, setActiveTab] = useState<'metadata' | 'sections' | 'preview'>('metadata');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    // Form state
    const [formData, setFormData] = useState<CreateGuionRequest>({
        idCampania: campaignId ? Number(campaignId) : undefined,
        nombre: '',
        objetivo: '',
        tipo: '',
        notasInternas: '', // Ensure empty string, not null
        secciones: Object.values(TIPOS_SECCION).map(tipo => ({
            tipoSeccion: tipo.value as SeccionGuionDTO['tipoSeccion'],
            contenido: '',
            orden: tipo.orden,
        })),
    });

    useEffect(() => {
        if (isOpen) {
            if (initialData) {
                setFormData({
                    idCampania: campaignId ? Number(campaignId) : undefined,
                    nombre: initialData.nombre,
                    objetivo: initialData.objetivo,
                    tipo: initialData.tipo,
                    notasInternas: initialData.notasInternas || '',
                    secciones: Object.values(TIPOS_SECCION).map(tipo => {
                        const existingSection = initialData.secciones?.find(s => s.tipoSeccion === tipo.value);
                        return {
                            tipoSeccion: tipo.value as SeccionGuionDTO['tipoSeccion'],
                            contenido: existingSection?.contenido || '',
                            orden: tipo.orden,
                        };
                    }),
                });
            } else {
                // Reset form if opening in create mode
                setFormData({
                    idCampania: campaignId ? Number(campaignId) : undefined,
                    nombre: '',
                    objetivo: '',
                    tipo: '',
                    notasInternas: '',
                    secciones: Object.values(TIPOS_SECCION).map(tipo => ({
                        tipoSeccion: tipo.value as SeccionGuionDTO['tipoSeccion'],
                        contenido: '',
                        orden: tipo.orden,
                    })),
                });
            }
            setActiveTab('metadata');
            setError(null);
        }
    }, [isOpen, initialData]);

    const handleInputChange = (field: keyof CreateGuionRequest, value: string) => {
        setFormData(prev => ({ ...prev, [field]: value }));
    };

    const handleSeccionChange = (tipoSeccion: string, contenido: string) => {
        setFormData(prev => ({
            ...prev,
            secciones: prev.secciones.map(seccion =>
                seccion.tipoSeccion === tipoSeccion
                    ? { ...seccion, contenido }
                    : seccion
            ),
        }));
    };

    const handleSubmit = async () => {
        try {
            setLoading(true);
            setError(null);

            // Validaciones
            if (!formData.nombre.trim()) {
                setError('El nombre del guión es requerido');
                setActiveTab('metadata');
                return;
            }
            if (!formData.objetivo.trim()) {
                setError('El objetivo es requerido');
                setActiveTab('metadata');
                return;
            }
            if (!formData.tipo) {
                setError('El tipo de llamada es requerido');
                setActiveTab('metadata');
                return;
            }

            // Filtrar secciones vacías
            const seccionesConContenido = formData.secciones.filter(s => s.contenido.trim());

            if (seccionesConContenido.length === 0) {
                setError('Debe completar al menos una sección del guión');
                setActiveTab('sections');
                return;
            }

            const requestData = {
                ...formData,
                secciones: seccionesConContenido,
            };

            if (initialData && initialData.id) {
                await guionesApi.updateGuion(initialData.id, requestData);
            } else {
                await guionesApi.createGuion(requestData);
            }

            onSuccess?.();
            onClose();
        } catch (err: any) {
            console.error(err);
            setError(err.response?.data?.message || 'Error al guardar el guión');
        } finally {
            setLoading(false);
        }
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-lg shadow-xl w-full max-w-4xl max-h-[90vh] flex flex-col">
                {/* Header */}
                <div className="flex items-center justify-between p-6 border-b">
                    <div className="flex items-center gap-3">
                        <FileText className="w-6 h-6 text-blue-600" />
                        <h2 className="text-2xl font-bold text-gray-900">
                            {initialData ? 'Editar Guión' : 'Crear Nuevo Guión'}
                        </h2>
                    </div>
                    <button
                        onClick={onClose}
                        className="text-gray-400 hover:text-gray-600 transition-colors"
                    >
                        <X className="w-6 h-6" />
                    </button>
                </div>

                {/* Tabs */}
                <div className="flex border-b">
                    <button
                        onClick={() => setActiveTab('metadata')}
                        className={`flex items-center gap-2 px-6 py-3 font-medium transition-colors ${activeTab === 'metadata'
                            ? 'text-blue-600 border-b-2 border-blue-600'
                            : 'text-gray-600 hover:text-gray-900'
                            }`}
                    >
                        <Settings className="w-4 h-4" />
                        Información General
                    </button>
                    <button
                        onClick={() => setActiveTab('sections')}
                        className={`flex items-center gap-2 px-6 py-3 font-medium transition-colors ${activeTab === 'sections'
                            ? 'text-blue-600 border-b-2 border-blue-600'
                            : 'text-gray-600 hover:text-gray-900'
                            }`}
                    >
                        <FileText className="w-4 h-4" />
                        Secciones del Guión
                    </button>
                    <button
                        onClick={() => setActiveTab('preview')}
                        className={`flex items-center gap-2 px-6 py-3 font-medium transition-colors ${activeTab === 'preview'
                            ? 'text-blue-600 border-b-2 border-blue-600'
                            : 'text-gray-600 hover:text-gray-900'
                            }`}
                    >
                        <Eye className="w-4 h-4" />
                        Vista Previa
                    </button>
                </div>

                {/* Error message */}
                {error && (
                    <div className="mx-6 mt-4 p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm">
                        {error}
                    </div>
                )}

                {/* Content */}
                <div className="flex-1 overflow-y-auto p-6">
                    {activeTab === 'metadata' && (
                        <div className="space-y-6">
                            {/* Nombre */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    Título del Guión <span className="text-red-500">*</span>
                                </label>
                                <input
                                    type="text"
                                    value={formData.nombre}
                                    onChange={(e) => handleInputChange('nombre', e.target.value)}
                                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                    placeholder="Ej: Guión de Renovación de Servicios"
                                />
                            </div>

                            {/* Objetivo */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    Objetivo de la Llamada <span className="text-red-500">*</span>
                                </label>
                                <textarea
                                    value={formData.objetivo}
                                    onChange={(e) => handleInputChange('objetivo', e.target.value)}
                                    rows={3}
                                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                    placeholder="Describe el objetivo principal de esta llamada..."
                                />
                            </div>

                            {/* Tipo */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    Tipo de Llamada <span className="text-red-500">*</span>
                                </label>
                                <select
                                    value={formData.tipo}
                                    onChange={(e) => handleInputChange('tipo', e.target.value)}
                                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                >
                                    <option value="">Seleccione un tipo...</option>
                                    {TIPOS_LLAMADA.map(tipo => (
                                        <option key={tipo.value} value={tipo.value}>
                                            {tipo.label}
                                        </option>
                                    ))}
                                </select>
                            </div>

                            {/* Notas Internas */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    Notas Internas / Tips para el Agente
                                </label>
                                <textarea
                                    value={formData.notasInternas}
                                    onChange={(e) => handleInputChange('notasInternas', e.target.value)}
                                    rows={4}
                                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                    placeholder="Información adicional, consejos, o recordatorios para los agentes..."
                                />
                            </div>
                        </div>
                    )}

                    {activeTab === 'sections' && (
                        <div className="space-y-6">
                            {Object.values(TIPOS_SECCION).map(tipo => {
                                const seccion = formData.secciones.find(s => s.tipoSeccion === tipo.value);
                                return (
                                    <div key={tipo.value} className="border border-gray-200 rounded-lg p-4 bg-white">
                                        <label className="block text-sm font-semibold text-gray-900 mb-3">
                                            {tipo.label}
                                        </label>
                                        <RichTextEditor
                                            content={seccion?.contenido || ''}
                                            onChange={(content) => handleSeccionChange(tipo.value, content)}
                                            placeholder={`Escribe el contenido para ${tipo.label.toLowerCase()}...`}
                                        />
                                    </div>
                                );
                            })}
                        </div>
                    )}

                    {activeTab === 'preview' && (
                        <MarkdownPreview data={formData} />
                    )}
                </div>

                {/* Footer */}
                <div className="flex items-center justify-end gap-3 p-6 border-t bg-gray-50">
                    <button
                        onClick={onClose}
                        className="px-6 py-2 text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
                        disabled={loading}
                    >
                        Cancelar
                    </button>
                    <button
                        onClick={handleSubmit}
                        disabled={loading}
                        className="flex items-center gap-2 px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                        <Save className="w-4 h-4" />
                        {loading ? 'Guardando...' : (initialData ? 'Guardar Cambios' : 'Crear Guión')}
                    </button>
                </div>
            </div>
        </div>
    );
};
