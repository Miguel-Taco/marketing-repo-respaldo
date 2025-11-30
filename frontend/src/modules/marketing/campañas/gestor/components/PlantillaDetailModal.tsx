import React, { useState, useEffect } from 'react';
import { PlantillaCampana, CrearPlantillaRequest } from '../types/plantilla.types';
import { CanalEjecucion } from '../types/campana.types';
import { plantillasApi } from '../services/plantillas.api';
import { segmentosApi } from '../services/segmentos.api';
import { encuestasApi } from '../services/encuestas.api';
import { useToast } from '../../../../../shared/components/ui/Toast';
import { EditableTextField } from './EditableTextField';
import { EditableSelectField } from './EditableSelectField';
import { EditableAsyncSelect } from './EditableAsyncSelect';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';

interface PlantillaDetailModalProps {
    isOpen: boolean;
    onClose: () => void;
    plantilla: PlantillaCampana | null;
    onUpdate?: () => void;
}

export const PlantillaDetailModal: React.FC<PlantillaDetailModalProps> = ({
    isOpen,
    onClose,
    plantilla,
    onUpdate,
}) => {
    const { showToast } = useToast();
    const [isEditing, setIsEditing] = useState(false);
    const [editedData, setEditedData] = useState<CrearPlantillaRequest | null>(null);
    const [validationErrors, setValidationErrors] = useState<Record<string, string>>({});
    const [isSaving, setIsSaving] = useState(false);

    useEffect(() => {
        if (plantilla && !isEditing) {
            setEditedData({
                nombre: plantilla.nombre,
                tematica: plantilla.tematica,
                descripcion: plantilla.descripcion,
                canalEjecucion: plantilla.canalEjecucion,
                idSegmento: plantilla.idSegmento,
                idEncuesta: plantilla.idEncuesta,
            });
        }
    }, [plantilla, isEditing]);

    if (!isOpen || !plantilla) return null;

    const validateForm = (data: CrearPlantillaRequest): Record<string, string> => {
        const errors: Record<string, string> = {};

        if (!data.nombre || data.nombre.trim().length < 3) {
            errors.nombre = 'El nombre debe tener al menos 3 caracteres';
        }
        if (!data.tematica || data.tematica.trim().length < 3) {
            errors.tematica = 'La temática debe tener al menos 3 caracteres';
        }

        return errors;
    };

    const handleSave = async () => {
        if (!plantilla || !editedData) return;

        const errors = validateForm(editedData);
        setValidationErrors(errors);

        if (Object.keys(errors).length > 0) {
            showToast('Por favor, corrija los errores en el formulario', 'error');
            return;
        }

        setIsSaving(true);
        try {
            await plantillasApi.update(plantilla.idPlantilla, editedData);
            if (onUpdate) onUpdate();
            showToast('Plantilla actualizada exitosamente', 'success');
            setIsEditing(false);
            setValidationErrors({});
        } catch (error) {
            console.error('Error updating plantilla:', error);
            showToast('Error al actualizar la plantilla', 'error');
        } finally {
            setIsSaving(false);
        }
    };

    const handleDelete = async () => {
        if (!plantilla) return;

        if (!window.confirm('¿Estás seguro de que deseas eliminar esta plantilla? Esta acción no se puede deshacer.')) {
            return;
        }

        try {
            await plantillasApi.delete(plantilla.idPlantilla);
            if (onUpdate) onUpdate();
            showToast('Plantilla eliminada exitosamente', 'success');
            onClose();
        } catch (error) {
            console.error('Error deleting plantilla:', error);
            showToast('Error al eliminar la plantilla', 'error');
        }
    };

    const handleCancel = () => {
        setIsEditing(false);
        setValidationErrors({});
        if (plantilla) {
            setEditedData({
                nombre: plantilla.nombre,
                tematica: plantilla.tematica,
                descripcion: plantilla.descripcion,
                canalEjecucion: plantilla.canalEjecucion,
                idSegmento: plantilla.idSegmento,
                idEncuesta: plantilla.idEncuesta,
            });
        }
    };

    const formatDate = (isoDate: string) => {
        return format(new Date(isoDate), "d 'de' MMMM, yyyy HH:mm", { locale: es });
    };

    return (
        <div className="fixed inset-0 top-0 left-0 right-0 bottom-0 bg-black bg-opacity-50 flex items-center justify-center z-[60] p-4">
            <div className="bg-white rounded-lg shadow-xl max-w-5xl w-full max-h-[90vh] overflow-hidden flex flex-col">
                {/* Header */}
                <div className="flex justify-between items-center p-6 border-b border-separator">
                    <h2 className="text-2xl font-bold text-dark">Detalle de Plantilla</h2>
                    <button
                        onClick={onClose}
                        className="p-2 hover:bg-gray-100 rounded-full transition-colors"
                        title="Cerrar"
                    >
                        <span className="material-symbols-outlined text-2xl">close</span>
                    </button>
                </div>

                {/* Content */}
                <div className="flex-1 overflow-y-auto p-6">
                    <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                        {/* Columna Izquierda (2/3) - Información */}
                        <div className="lg:col-span-2 space-y-6">
                            {/* Info Básica */}
                            <div className="card p-6 border border-separator rounded-lg shadow-sm">
                                <h3 className="text-lg font-semibold text-dark mb-4">Información Básica</h3>
                                <div className="space-y-3">
                                    <EditableTextField
                                        label="Nombre"
                                        value={isEditing && editedData ? editedData.nombre : plantilla.nombre}
                                        isEditing={isEditing}
                                        onChange={(value) => editedData && setEditedData({ ...editedData, nombre: value })}
                                        error={validationErrors.nombre}
                                        required
                                    />
                                    <EditableTextField
                                        label="Temática"
                                        value={isEditing && editedData ? editedData.tematica : plantilla.tematica}
                                        isEditing={isEditing}
                                        onChange={(value) => editedData && setEditedData({ ...editedData, tematica: value })}
                                        error={validationErrors.tematica}
                                        required
                                    />
                                    <EditableTextField
                                        label="Descripción"
                                        value={isEditing && editedData ? editedData.descripcion || '' : (plantilla.descripcion || '')}
                                        isEditing={isEditing}
                                        onChange={(value) => editedData && setEditedData({ ...editedData, descripcion: value })}
                                        multiline
                                        rows={3}
                                    />
                                </div>
                            </div>

                            {/* Configuración */}
                            <div className="card p-6 border border-separator rounded-lg shadow-sm">
                                <h3 className="text-lg font-semibold text-dark mb-4">Configuración</h3>
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                    <EditableSelectField
                                        label="Canal de Ejecución"
                                        value={(isEditing && editedData ? editedData.canalEjecucion : plantilla.canalEjecucion) || ''}
                                        options={[
                                            { value: 'Mailing' as CanalEjecucion, label: 'Mailing' },
                                            { value: 'Llamadas' as CanalEjecucion, label: 'Llamadas' },
                                        ]}
                                        isEditing={isEditing}
                                        onChange={(value) => editedData && setEditedData({ ...editedData, canalEjecucion: value as CanalEjecucion })}
                                        renderDisplay={(value) => value ? <span className="font-medium text-dark">{value}</span> : <span className="text-gray-500">Sin asignar</span>}
                                    />

                                    <EditableAsyncSelect
                                        label="Segmento Sugerido"
                                        value={isEditing && editedData ? editedData.idSegmento : plantilla.idSegmento}
                                        isEditing={isEditing}
                                        onChange={(value) => editedData && setEditedData({ ...editedData, idSegmento: value })}
                                        loadOptions={async () => {
                                            const segmentos = await segmentosApi.getActivos();
                                            return segmentos.map(s => ({
                                                value: s.id,
                                                label: s.nombre
                                            }));
                                        }}
                                        loadLabel={async (id) => {
                                            const segmento = await segmentosApi.getById(id);
                                            return segmento.nombre;
                                        }}
                                        allowClear
                                        placeholder="Sin asignar"
                                    />

                                    <div className="md:col-span-2">
                                        <EditableAsyncSelect
                                            label="Encuesta (Opcional)"
                                            value={isEditing && editedData ? editedData.idEncuesta : plantilla.idEncuesta}
                                            isEditing={isEditing}
                                            onChange={(value) => editedData && setEditedData({ ...editedData, idEncuesta: value })}
                                            loadOptions={async () => {
                                                const encuestas = await encuestasApi.getDisponibles();
                                                return encuestas.map(e => ({
                                                    value: e.idEncuesta,
                                                    label: e.titulo
                                                }));
                                            }}
                                            loadLabel={async (id) => {
                                                const encuesta = await encuestasApi.getById(id);
                                                return encuesta.titulo;
                                            }}
                                            allowClear
                                            placeholder="Sin asignar"
                                        />
                                    </div>
                                </div>
                            </div>
                        </div>

                        {/* Columna Derecha (1/3) - Acciones y Metadatos */}
                        <div className="space-y-6">
                            {/* Acciones */}
                            <div className="card p-6 border border-separator rounded-lg shadow-sm">
                                <h3 className="text-lg font-semibold text-dark mb-4">Acciones</h3>
                                <div className="flex flex-col gap-3">
                                    {isEditing ? (
                                        <>
                                            <button
                                                onClick={handleSave}
                                                disabled={isSaving}
                                                className="w-full px-4 py-2 bg-green-500 text-white rounded-full hover:bg-green-600 flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
                                            >
                                                {isSaving ? (
                                                    <span className="material-symbols-outlined animate-spin">refresh</span>
                                                ) : (
                                                    <span className="material-symbols-outlined">save</span>
                                                )}
                                                {isSaving ? 'Guardando...' : 'Guardar'}
                                            </button>
                                            <button
                                                onClick={handleCancel}
                                                disabled={isSaving}
                                                className="w-full px-4 py-2 bg-gray-500 text-white rounded-full hover:bg-gray-600 flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
                                            >
                                                <span className="material-symbols-outlined">close</span>
                                                Cancelar
                                            </button>
                                        </>
                                    ) : (
                                        <>
                                            <button
                                                onClick={() => setIsEditing(true)}
                                                className="w-full px-4 py-2 bg-primary text-white rounded-full hover:bg-blue-700 flex items-center justify-center gap-2"
                                            >
                                                <span className="material-symbols-outlined">edit</span>
                                                Editar
                                            </button>
                                            <button
                                                onClick={handleDelete}
                                                className="w-full px-4 py-2 bg-red-500 text-white rounded-full hover:bg-red-600 flex items-center justify-center gap-2"
                                            >
                                                <span className="material-symbols-outlined">delete</span>
                                                Eliminar
                                            </button>
                                        </>
                                    )}
                                </div>
                            </div>

                            {/* Metadatos */}
                            <div className="card p-6 border border-separator rounded-lg shadow-sm">
                                <h3 className="text-lg font-semibold text-dark mb-4">Detalles del Sistema</h3>
                                <div className="space-y-4 text-sm text-gray-600">
                                    <div>
                                        <label className="block font-medium text-gray-700">ID Plantilla</label>
                                        <div>#{plantilla.idPlantilla}</div>
                                    </div>
                                    <div>
                                        <label className="block font-medium text-gray-700">Fecha de Creación</label>
                                        <div>{formatDate(plantilla.fechaCreacion)}</div>
                                    </div>
                                    <div>
                                        <label className="block font-medium text-gray-700">Última Modificación</label>
                                        <div>{formatDate(plantilla.fechaModificacion)}</div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};
