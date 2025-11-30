import React, { useState, useEffect } from 'react';
import { useCampanaDetail } from '../hooks/useCampanaDetail';
import { useCampanaHistorial } from '../hooks/useCampanaHistorial';
import { CampanaStatusBadge } from './CampanaStatusBadge';
import { CampanaPriorityBadge } from './CampanaPriorityBadge';
import { EditableTextField } from './EditableTextField';
import { EditableSelectField } from './EditableSelectField';
import { EditableAsyncSelect } from './EditableAsyncSelect';
import { MotivoModal } from './MotivoModal';
import { SchedulingModal } from './SchedulingModal';
import { EstadoCampanaEnum, TipoAccion, Prioridad, CanalEjecucion, UpdateCampanaDTO } from '../types/campana.types';
import { campanasApi } from '../services/campanas.api';
import { segmentosApi } from '../services/segmentos.api';
import { encuestasApi } from '../services/encuestas.api';
import { useToast } from '../../../../../shared/components/ui/Toast';

interface CampanaDetailModalProps {
    isOpen: boolean;
    onClose: () => void;
    campanaId: number | null;
    onUpdate?: () => void;
}

export const CampanaDetailModal: React.FC<CampanaDetailModalProps> = ({
    isOpen,
    onClose,
    campanaId,
    onUpdate,
}) => {
    const { campana, loading, refresh } = useCampanaDetail(campanaId);
    const { historial, loading: historialLoading, refresh: refreshHistorial } = useCampanaHistorial(campanaId);
    const { showToast } = useToast();

    // Edit mode state
    const [isEditing, setIsEditing] = useState(false);
    const [editedData, setEditedData] = useState<UpdateCampanaDTO | null>(null);
    const [validationErrors, setValidationErrors] = useState<Record<string, string>>({});
    const [isSaving, setIsSaving] = useState(false);

    // Motivo modal state
    const [motivoModal, setMotivoModal] = useState<{
        isOpen: boolean;
        title: string;
        action: (motivo: string) => Promise<void>;
    }>({ isOpen: false, title: '', action: async () => { } });

    // Scheduling modal state
    const [schedulingModal, setSchedulingModal] = useState<{
        isOpen: boolean;
        isReschedule: boolean;
    }>({ isOpen: false, isReschedule: false });

    // Initialize edit data when campaign loads
    useEffect(() => {
        if (campana && !editedData) {
            setEditedData({
                nombre: campana.nombre,
                tematica: campana.tematica,
                descripcion: campana.descripcion || '',
                prioridad: campana.prioridad,
                canalEjecucion: campana.canalEjecucion,
                idAgente: campana.idAgente || undefined,
                idSegmento: campana.idSegmento || undefined,
                idEncuesta: campana.idEncuesta || undefined,
            });
        }
    }, [campana, editedData]);

    if (!isOpen || !campanaId) return null;

    const handleAction = async (action: () => Promise<any>, successMessage: string, shouldCloseModal = false) => {
        try {
            await action();
            await refresh();
            await refreshHistorial(); // Refresh history after action
            if (onUpdate) onUpdate();
            showToast(successMessage, 'success');

            // Auto-close modal if delete was successful
            if (shouldCloseModal) {
                setTimeout(() => onClose(), 500);
            }
        } catch (error) {
            console.error('Error performing action:', error);
            showToast('Error al ejecutar la acción', 'error');
        }
    };

    const handleMotivoAction = (title: string, action: (motivo: string) => Promise<void>) => {
        setMotivoModal({ isOpen: true, title, action });
    };

    const handleMotivoConfirm = async (motivo: string) => {
        await motivoModal.action(motivo);
    };

    const handleSchedulingSuccess = async () => {
        await refresh();
        await refreshHistorial();
        if (onUpdate) onUpdate();
    };

    // Edit mode functions
    const validateForm = (data: UpdateCampanaDTO): Record<string, string> => {
        const errors: Record<string, string> = {};

        if (!data.nombre || data.nombre.trim().length < 3) {
            errors.nombre = 'El nombre debe tener al menos 3 caracteres';
        }
        if (data.nombre && data.nombre.length > 100) {
            errors.nombre = 'El nombre no puede exceder 100 caracteres';
        }

        if (!data.tematica || data.tematica.trim().length < 3) {
            errors.tematica = 'La temática debe tener al menos 3 caracteres';
        }
        if (data.tematica && data.tematica.length > 100) {
            errors.tematica = 'La temática no puede exceder 100 caracteres';
        }

        if (data.descripcion && data.descripcion.length > 500) {
            errors.descripcion = 'La descripción no puede exceder 500 caracteres';
        }

        return errors;
    };

    const handleEdit = () => {
        if (!campana) return;
        setIsEditing(true);
    };

    const handleSave = async () => {
        if (!campana || !editedData) return;

        // Validate form
        const errors = validateForm(editedData);
        setValidationErrors(errors);

        if (Object.keys(errors).length > 0) {
            showToast('Por favor, corrija los errores en el formulario', 'error');
            return;
        }

        setIsSaving(true);
        try {
            await campanasApi.update(campana.idCampana, editedData);
            await refresh();
            if (onUpdate) onUpdate();
            showToast('Campaña actualizada exitosamente', 'success');
            setIsEditing(false);
            setValidationErrors({});
        } catch (error) {
            console.error('Error updating campaign:', error);
            showToast('Error al actualizar la campaña', 'error');
        } finally {
            setIsSaving(false);
        }
    };

    const handleCancel = () => {
        if (!campana) return;

        // Restore original data
        setEditedData({
            nombre: campana.nombre,
            tematica: campana.tematica,
            descripcion: campana.descripcion || '',
            prioridad: campana.prioridad,
            canalEjecucion: campana.canalEjecucion,
            idAgente: campana.idAgente || undefined,
            idSegmento: campana.idSegmento || undefined,
            idEncuesta: campana.idEncuesta || undefined,
        });
        setIsEditing(false);
        setValidationErrors({});
    };

    // Check if campaign can be edited
    const canEdit = campana && (
        campana.estado === EstadoCampanaEnum.BORRADOR ||
        campana.estado === EstadoCampanaEnum.PAUSADA
    );

    const formatDate = (isoDate: string | null) => {
        if (!isoDate) return 'Sin programar';
        const date = new Date(isoDate);
        return date.toLocaleDateString('es-ES', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
        });
    };

    const getActionIcon = (tipoAccion: TipoAccion): string => {
        switch (tipoAccion) {
            case TipoAccion.CREACION:
                return 'add_circle';
            case TipoAccion.EDICION:
                return 'edit';
            case TipoAccion.PROGRAMACION:
                return 'schedule';
            case TipoAccion.REPROGRAMACION:
                return 'update';
            case TipoAccion.ACTIVACION:
                return 'play_arrow';
            case TipoAccion.PAUSA:
                return 'pause';
            case TipoAccion.REANUDACION:
                return 'play_circle';
            case TipoAccion.CANCELACION:
                return 'cancel';
            case TipoAccion.FINALIZACION:
                return 'check_circle';
            case TipoAccion.ARCHIVO:
                return 'archive';
            case TipoAccion.DUPLICACION:
                return 'content_copy';
            case TipoAccion.ERROR_EJECUCION:
                return 'error';
            default:
                return 'info';
        }
    };

    // Botones de acción según estado
    const renderActions = () => {
        if (!campana) return null;

        switch (campana.estado) {
            case EstadoCampanaEnum.BORRADOR:
                return (
                    <>
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
                                    onClick={handleEdit}
                                    className="w-full px-4 py-2 bg-primary text-white rounded-full hover:bg-blue-700 flex items-center justify-center gap-2"
                                >
                                    <span className="material-symbols-outlined">edit</span>
                                    Editar
                                </button>
                                <button
                                    onClick={() => setSchedulingModal({ isOpen: true, isReschedule: false })}
                                    className="w-full px-4 py-2 bg-primary text-white rounded-full hover:bg-blue-700 flex items-center justify-center gap-2"
                                >
                                    <span className="material-symbols-outlined">schedule</span>
                                    Programar
                                </button>
                                <button
                                    onClick={() => handleAction(
                                        () => campanasApi.delete(campana.idCampana),
                                        'Campaña eliminada exitosamente',
                                        true // Auto-close modal on success
                                    )}
                                    className="w-full px-4 py-2 bg-red-500 text-white rounded-full hover:bg-red-600 flex items-center justify-center gap-2"
                                >
                                    <span className="material-symbols-outlined">delete</span>
                                    Eliminar
                                </button>
                            </>
                        )}
                    </>
                );

            case EstadoCampanaEnum.PROGRAMADA:
                return (
                    <>
                        <button
                            onClick={() => setSchedulingModal({ isOpen: true, isReschedule: true })}
                            className="w-full px-4 py-2 bg-primary text-white rounded-full hover:bg-blue-700 flex items-center justify-center gap-2"
                        >
                            <span className="material-symbols-outlined">update</span>
                            Reprogramar
                        </button>
                        <button
                            onClick={() => handleMotivoAction(
                                'Motivo de Cancelación',
                                async (motivo) => {
                                    await handleAction(
                                        () => campanasApi.cancelar(campana.idCampana, motivo),
                                        'Campaña cancelada exitosamente'
                                    );
                                }
                            )}
                            className="w-full px-4 py-2 bg-red-500 text-white rounded-full hover:bg-red-600 flex items-center justify-center gap-2"
                        >
                            <span className="material-symbols-outlined">cancel</span>
                            Cancelar
                        </button>
                    </>
                );

            case EstadoCampanaEnum.VIGENTE:
                return (
                    <>
                        <button
                            onClick={() => handleMotivoAction(
                                'Motivo de Pausa',
                                async (motivo) => {
                                    await handleAction(
                                        () => campanasApi.pausar(campana.idCampana, motivo),
                                        'Campaña pausada exitosamente'
                                    );
                                }
                            )}
                            className="w-full px-4 py-2 bg-primary text-white rounded-full hover:bg-blue-700 flex items-center justify-center gap-2"
                        >
                            <span className="material-symbols-outlined">pause</span>
                            Pausar
                        </button>
                        <button
                            onClick={() => handleMotivoAction(
                                'Motivo de Cancelación',
                                async (motivo) => {
                                    await handleAction(
                                        () => campanasApi.cancelar(campana.idCampana, motivo),
                                        'Campaña cancelada exitosamente'
                                    );
                                }
                            )}
                            className="w-full px-4 py-2 bg-red-500 text-white rounded-full hover:bg-red-600 flex items-center justify-center gap-2"
                        >
                            <span className="material-symbols-outlined">cancel</span>
                            Cancelar
                        </button>
                    </>
                );

            case EstadoCampanaEnum.PAUSADA:
                return (
                    <>
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
                                    onClick={() => handleAction(
                                        () => campanasApi.reanudar(campana.idCampana),
                                        'Campaña reanudada exitosamente'
                                    )}
                                    className="w-full px-4 py-2 bg-primary text-white rounded-full hover:bg-blue-700 flex items-center justify-center gap-2"
                                >
                                    <span className="material-symbols-outlined">play_arrow</span>
                                    Reanudar
                                </button>
                                <button
                                    onClick={() => setSchedulingModal({ isOpen: true, isReschedule: true })}
                                    className="w-full px-4 py-2 bg-primary text-white rounded-full hover:bg-blue-700 flex items-center justify-center gap-2"
                                >
                                    <span className="material-symbols-outlined">update</span>
                                    Reprogramar
                                </button>
                                <button
                                    onClick={handleEdit}
                                    className="w-full px-4 py-2 bg-primary text-white rounded-full hover:bg-blue-700 flex items-center justify-center gap-2"
                                >
                                    <span className="material-symbols-outlined">edit</span>
                                    Editar
                                </button>
                                <button
                                    onClick={() => handleMotivoAction(
                                        'Motivo de Cancelación',
                                        async (motivo) => {
                                            await handleAction(
                                                () => campanasApi.cancelar(campana.idCampana, motivo),
                                                'Campaña cancelada exitosamente'
                                            );
                                        }
                                    )}
                                    className="w-full px-4 py-2 bg-red-500 text-white rounded-full hover:bg-red-600 flex items-center justify-center gap-2"
                                >
                                    <span className="material-symbols-outlined">cancel</span>
                                    Cancelar
                                </button>
                            </>
                        )}
                    </>
                );

            case EstadoCampanaEnum.FINALIZADA:
            case EstadoCampanaEnum.CANCELADA:
                return (
                    <>
                        <button
                            onClick={() => handleAction(
                                () => campanasApi.duplicar(campana.idCampana),
                                'Campaña duplicada exitosamente'
                            )}
                            className="w-full px-4 py-2 bg-primary text-white rounded-full hover:bg-blue-700 flex items-center justify-center gap-2"
                        >
                            <span className="material-symbols-outlined">content_copy</span>
                            Duplicar
                        </button>
                        <button
                            onClick={() => handleAction(
                                () => campanasApi.archivar(campana.idCampana),
                                'Campaña archivada exitosamente'
                            )}
                            className="w-full px-4 py-2 bg-primary text-white rounded-full hover:bg-blue-700 flex items-center justify-center gap-2"
                        >
                            <span className="material-symbols-outlined">archive</span>
                            Archivar
                        </button>
                    </>
                );

            default:
                return null;
        }
    };

    return (
        <div className="fixed inset-0 top-0 left-0 right-0 bottom-0 bg-black bg-opacity-50 flex items-center justify-center z-[60] p-4">
            <div className="bg-white rounded-lg shadow-xl max-w-6xl w-full max-h-[90vh] overflow-hidden flex flex-col">
                {/* Header */}
                <div className="flex justify-between items-center p-6 border-b border-separator">
                    <h2 className="text-2xl font-bold text-dark">Detalle de Campaña</h2>
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
                    {loading ? (
                        <div className="flex items-center justify-center p-12">
                            <div className="text-center">
                                <div className="inline-block h-8 w-8 animate-spin rounded-full border-4 border-solid border-primary border-r-transparent"></div>
                                <p className="mt-4 text-gray-600">Cargando detalles...</p>
                            </div>
                        </div>
                    ) : campana ? (
                        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                            {/* Columna Izquierda (2/3) - Información */}
                            <div className="lg:col-span-2 space-y-6">
                                {/* Info Básica */}
                                <div className="card p-6">
                                    <h3 className="text-lg font-semibold text-dark mb-4">Información Básica</h3>
                                    <div className="space-y-3">
                                        <EditableTextField
                                            label="Nombre"
                                            value={isEditing && editedData ? editedData.nombre : campana.nombre}
                                            isEditing={isEditing}
                                            onChange={(value) => editedData && setEditedData({ ...editedData, nombre: value })}
                                            error={validationErrors.nombre}
                                            required
                                        />
                                        <EditableTextField
                                            label="Temática"
                                            value={isEditing && editedData ? editedData.tematica : campana.tematica}
                                            isEditing={isEditing}
                                            onChange={(value) => editedData && setEditedData({ ...editedData, tematica: value })}
                                            error={validationErrors.tematica}
                                            required
                                        />
                                        <EditableTextField
                                            label="Descripción"
                                            value={isEditing && editedData ? editedData.descripcion || '' : (campana.descripcion || '')}
                                            isEditing={isEditing}
                                            onChange={(value) => editedData && setEditedData({ ...editedData, descripcion: value })}
                                            error={validationErrors.descripcion}
                                            multiline
                                            rows={3}
                                        />
                                        <div className="grid grid-cols-3 gap-4 pt-2">
                                            <div>
                                                <label className="text-sm text-gray-600">Estado</label>
                                                <div className="mt-1">
                                                    <CampanaStatusBadge estado={campana.estado} />
                                                </div>
                                            </div>
                                            <EditableSelectField
                                                label="Prioridad"
                                                value={isEditing && editedData ? editedData.prioridad : campana.prioridad}
                                                options={[
                                                    { value: 'Alta' as Prioridad, label: 'Alta' },
                                                    { value: 'Media' as Prioridad, label: 'Media' },
                                                    { value: 'Baja' as Prioridad, label: 'Baja' },
                                                ]}
                                                isEditing={isEditing}
                                                onChange={(value) => editedData && setEditedData({ ...editedData, prioridad: value })}
                                                renderDisplay={(value) => <CampanaPriorityBadge prioridad={value} />}
                                            />
                                            <EditableSelectField
                                                label="Canal"
                                                value={isEditing && editedData ? editedData.canalEjecucion : campana.canalEjecucion}
                                                options={[
                                                    { value: 'Mailing' as CanalEjecucion, label: 'Mailing' },
                                                    { value: 'Llamadas' as CanalEjecucion, label: 'Llamadas' },
                                                ]}
                                                isEditing={isEditing}
                                                onChange={(value) => editedData && setEditedData({ ...editedData, canalEjecucion: value })}
                                                renderDisplay={(value) => <span className="font-medium text-dark">{value}</span>}
                                            />
                                        </div>
                                    </div>
                                </div>

                                {/* Segmento y Encuesta */}
                                <div className="card p-6">
                                    <h3 className="text-lg font-semibold text-dark mb-4">Segmento y Encuesta</h3>
                                    <div className="grid grid-cols-2 gap-4">
                                        <EditableAsyncSelect
                                            label="Segmento"
                                            value={isEditing && editedData ? editedData.idSegmento : campana.idSegmento}
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
                                        <EditableAsyncSelect
                                            label="Encuesta"
                                            value={isEditing && editedData ? editedData.idEncuesta : campana.idEncuesta}
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

                                {/* Agente y Fechas */}
                                <div className="card p-6">
                                    <h3 className="text-lg font-semibold text-dark mb-4">Agente y Fechas Programadas</h3>
                                    <div className="space-y-3">
                                        <div>
                                            <label className="text-sm text-gray-600">Agente Responsable</label>
                                            <div className="text-dark">
                                                {campana.nombreAgente ? campana.nombreAgente : (campana.idAgente ? `Agente #${campana.idAgente}` : 'Sin asignar')}
                                            </div>
                                        </div>
                                        <div className="grid grid-cols-2 gap-4">
                                            <div>
                                                <label className="text-sm text-gray-600">Fecha Inicio</label>
                                                <div className="text-dark">{formatDate(campana.fechaProgramadaInicio)}</div>
                                            </div>
                                            <div>
                                                <label className="text-sm text-gray-600">Fecha Fin</label>
                                                <div className="text-dark">{formatDate(campana.fechaProgramadaFin)}</div>
                                            </div>
                                        </div>
                                        <div className="grid grid-cols-2 gap-4 pt-2 text-sm text-gray-600">
                                            <div>
                                                <label>Creada</label>
                                                <div>{formatDate(campana.fechaCreacion)}</div>
                                            </div>
                                            <div>
                                                <label>Modificada</label>
                                                <div>{formatDate(campana.fechaModificacion)}</div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            {/* Columna Derecha (1/3) - Acciones e Historial */}
                            <div className="space-y-6">
                                {/* Acciones */}
                                <div className="card p-6">
                                    <h3 className="text-lg font-semibold text-dark mb-4">Acciones</h3>
                                    <div className="flex flex-col gap-3">
                                        {renderActions()}
                                    </div>
                                </div>

                                {/* Historial */}
                                <div className="card p-6">
                                    <h3 className="text-lg font-semibold text-dark mb-4">Historial</h3>
                                    {historialLoading ? (
                                        <div className="text-center py-4">
                                            <div className="inline-block h-6 w-6 animate-spin rounded-full border-2 border-solid border-primary border-r-transparent"></div>
                                        </div>
                                    ) : historial.length > 0 ? (
                                        <div className="space-y-4 max-h-96 overflow-y-auto">
                                            {historial.map((item) => (
                                                <div key={item.idHistorial} className="flex gap-3 pb-3 border-b border-gray-200 last:border-0">
                                                    <div className="flex-shrink-0 mt-1">
                                                        <span className="material-symbols-outlined text-primary text-xl">
                                                            {getActionIcon(item.tipoAccion)}
                                                        </span>
                                                    </div>
                                                    <div className="flex-1 min-w-0">
                                                        <div className="font-medium text-dark text-sm">
                                                            {item.tipoAccion.replace('_', ' ')}
                                                        </div>
                                                        <div className="text-xs text-gray-500 mt-1">
                                                            {formatDate(item.fechaAccion)}
                                                        </div>
                                                        {item.descripcionDetalle && (
                                                            <div className="text-sm text-gray-600 mt-1">
                                                                {item.descripcionDetalle}
                                                            </div>
                                                        )}
                                                    </div>
                                                </div>
                                            ))}
                                        </div>
                                    ) : (
                                        <div className="text-center text-gray-500 text-sm py-4">
                                            Sin historial
                                        </div>
                                    )}
                                </div>
                            </div>
                        </div>
                    ) : (
                        <div className="text-center py-12 text-gray-500">
                            No se encontraron detalles de la campaña
                        </div>
                    )}
                </div>
            </div>

            {/* Motivo Modal */}
            <MotivoModal
                isOpen={motivoModal.isOpen}
                onClose={() => setMotivoModal({ ...motivoModal, isOpen: false })}
                onConfirm={handleMotivoConfirm}
                title={motivoModal.title}
                placeholder="Ingrese el motivo de la acción..."
            />

            {/* Scheduling Modal */}
            {campana && (
                <SchedulingModal
                    isOpen={schedulingModal.isOpen}
                    onClose={() => setSchedulingModal({ ...schedulingModal, isOpen: false })}
                    campana={campana}
                    onSuccess={handleSchedulingSuccess}
                    isReschedule={schedulingModal.isReschedule}
                />
            )}
        </div>
    );
};
