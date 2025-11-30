import React, { useState, useEffect } from 'react';
import { plantillasApi } from '../services/plantillas.api';
import { segmentosApi } from '../services/segmentos.api';
import { encuestasApi } from '../services/encuestas.api';
import { CrearPlantillaRequest } from '../types/plantilla.types';
import { CanalEjecucion } from '../types/campana.types';
import { Segmento } from '../../../../../shared/types/segmento.types';
import { EncuestaDisponible } from '../../../../../shared/types/encuesta.types';
import { useToast } from '../../../../../shared/components/ui/Toast';
import { Modal } from '../../../../../shared/components/ui/Modal';

interface CreatePlantillaModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSuccess: () => void;
}

export const CreatePlantillaModal: React.FC<CreatePlantillaModalProps> = ({ isOpen, onClose, onSuccess }) => {
    const [formData, setFormData] = useState<CrearPlantillaRequest>({
        nombre: '',
        tematica: '',
        descripcion: '',
        canalEjecucion: undefined,
        idSegmento: undefined,
        idEncuesta: undefined,
    });
    const [segmentos, setSegmentos] = useState<Segmento[]>([]);
    const [encuestas, setEncuestas] = useState<EncuestaDisponible[]>([]);
    const [errors, setErrors] = useState<Record<string, string>>({});
    const [isSaving, setIsSaving] = useState(false);
    const { showToast } = useToast();

    useEffect(() => {
        if (isOpen) {
            loadData();
        }
    }, [isOpen]);

    const loadData = async () => {
        try {
            const [segmentosData, encuestasData] = await Promise.all([
                segmentosApi.getActivos(),
                encuestasApi.getDisponibles()
            ]);
            setSegmentos(segmentosData);
            setEncuestas(encuestasData);
        } catch (error) {
            console.error('Error loading data:', error);
            showToast('Error al cargar datos de selección', 'error');
        }
    };

    const validateForm = (): boolean => {
        const newErrors: Record<string, string> = {};

        if (!formData.nombre.trim()) {
            newErrors.nombre = 'El nombre es obligatorio';
        }
        if (!formData.tematica.trim()) {
            newErrors.tematica = 'La temática es obligatoria';
        }


        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleCreate = async () => {
        if (!validateForm()) return;

        setIsSaving(true);
        try {
            await plantillasApi.create(formData);
            showToast('Plantilla creada exitosamente', 'success');
            resetForm();
            onSuccess();
        } catch (error) {
            console.error('Error creating plantilla:', error);
            showToast('Hubo un error al crear la plantilla', 'error');
        } finally {
            setIsSaving(false);
        }
    };

    const resetForm = () => {
        setFormData({
            nombre: '',
            tematica: '',
            descripcion: '',
            canalEjecucion: undefined,
            idSegmento: undefined,
            idEncuesta: undefined,
        });
        setErrors({});
    };

    const handleClose = () => {
        resetForm();
        onClose();
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 top-0 left-0 right-0 bottom-0 bg-black bg-opacity-50 flex items-center justify-center z-[60] p-4">
            <div className="bg-white rounded-lg shadow-xl max-w-4xl w-full max-h-[90vh] overflow-hidden flex flex-col">
                {/* Header */}
                <div className="flex justify-between items-center p-6 border-b border-separator">
                    <div>
                        <h2 className="text-2xl font-bold text-dark">Nueva Plantilla</h2>
                        <p className="text-sm text-gray-600 mt-1">
                            Complete los siguientes campos para configurar su plantilla.
                        </p>
                    </div>
                    <button
                        onClick={handleClose}
                        className="p-2 hover:bg-gray-100 rounded-full transition-colors"
                        title="Cerrar"
                    >
                        <span className="material-symbols-outlined text-2xl">close</span>
                    </button>
                </div>

                {/* Content */}
                <div className="flex-1 overflow-y-auto p-6">
                    <div className="space-y-6">
                        {/* Row 1: Nombre and Temática */}
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    Nombre de la Plantilla <span className="text-red-500">*</span>
                                </label>
                                <input
                                    type="text"
                                    className={`w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-primary focus:outline-none ${errors.nombre ? 'border-red-500' : 'border-separator'}`}
                                    placeholder="Ej: Plantilla de Ventas Verano 2024"
                                    value={formData.nombre}
                                    onChange={(e) => setFormData({ ...formData, nombre: e.target.value })}
                                />
                                {errors.nombre && <p className="mt-1 text-sm text-red-500">{errors.nombre}</p>}
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    Temática <span className="text-red-500">*</span>
                                </label>
                                <input
                                    type="text"
                                    className={`w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-primary focus:outline-none ${errors.tematica ? 'border-red-500' : 'border-separator'}`}
                                    placeholder="Ej: Promoción de productos de temporada"
                                    value={formData.tematica}
                                    onChange={(e) => setFormData({ ...formData, tematica: e.target.value })}
                                />
                                {errors.tematica && <p className="mt-1 text-sm text-red-500">{errors.tematica}</p>}
                            </div>
                        </div>

                        {/* Row 2: Descripción */}
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                Descripción
                            </label>
                            <textarea
                                className="w-full px-3 py-2 border border-separator rounded-lg focus:ring-2 focus:ring-primary focus:outline-none"
                                rows={4}
                                placeholder="Describa el propósito y contenido de esta plantilla..."
                                value={formData.descripcion}
                                onChange={(e) => setFormData({ ...formData, descripcion: e.target.value })}
                            />
                        </div>

                        {/* Row 3: Canal and Segmento */}
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                            {/* Canal de Ejecución */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    Canal de Ejecución
                                </label>
                                <select
                                    className="w-full px-3 py-2 border border-separator rounded-lg focus:ring-2 focus:ring-primary focus:outline-none"
                                    value={formData.canalEjecucion || ''}
                                    onChange={(e) => setFormData({ ...formData, canalEjecucion: e.target.value as CanalEjecucion })}
                                >
                                    <option value="">Sin asignar</option>
                                    <option value="Mailing">Mailing</option>
                                    <option value="Llamadas">Llamadas</option>
                                </select>
                            </div>

                            {/* Segmento */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    Segmento Sugerido
                                </label>
                                <select
                                    className="w-full px-3 py-2 border border-separator rounded-lg focus:ring-2 focus:ring-primary focus:outline-none"
                                    value={formData.idSegmento || ''}
                                    onChange={(e) => setFormData({ ...formData, idSegmento: e.target.value ? Number(e.target.value) : undefined })}
                                >
                                    <option value="">Sin asignar</option>
                                    {segmentos.map((segmento) => (
                                        <option key={segmento.id} value={segmento.id}>
                                            {segmento.nombre}
                                        </option>
                                    ))}
                                </select>
                            </div>
                        </div>

                        {/* Row 4: Encuesta */}
                        <div className="w-full md:w-1/2">
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                Encuesta (Opcional)
                            </label>
                            <select
                                className="w-full px-3 py-2 border border-separator rounded-lg focus:ring-2 focus:ring-primary focus:outline-none"
                                value={formData.idEncuesta || ''}
                                onChange={(e) => setFormData({ ...formData, idEncuesta: e.target.value ? Number(e.target.value) : undefined })}
                            >
                                <option value="">Sin asignar</option>
                                {encuestas.map((encuesta) => (
                                    <option key={encuesta.idEncuesta} value={encuesta.idEncuesta}>
                                        {encuesta.titulo}
                                    </option>
                                ))}
                            </select>
                        </div>
                    </div>
                </div>

                {/* Footer */}
                <div className="flex items-center justify-end p-6 border-t border-separator gap-3">
                    <button
                        onClick={handleClose}
                        className="px-4 py-2 text-sm font-semibold text-gray-600 hover:text-gray-800 focus:outline-none"
                        disabled={isSaving}
                    >
                        Cancelar
                    </button>
                    <button
                        onClick={handleCreate}
                        disabled={isSaving}
                        className="flex items-center gap-2 px-6 py-2.5 text-sm font-semibold text-white bg-primary rounded-full hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-primary disabled:opacity-50"
                    >
                        {isSaving ? (
                            <>
                                <div className="inline-block h-4 w-4 animate-spin rounded-full border-2 border-solid border-white border-r-transparent"></div>
                                Creando...
                            </>
                        ) : (
                            <>
                                <span className="material-symbols-outlined">save</span>
                                Crear Plantilla
                            </>
                        )}
                    </button>
                </div>
            </div>
        </div>
    );
};
