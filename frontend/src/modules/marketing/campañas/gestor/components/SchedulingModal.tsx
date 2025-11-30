import React, { useState, useEffect } from 'react';
import { Step3Scheduling } from './Step3Scheduling';
import { CampanaDetalle } from '../types/campana.types';
import { campanasApi } from '../services/campanas.api';
import { useToast } from '../../../../../shared/components/ui/Toast';

interface SchedulingModalProps {
    isOpen: boolean;
    onClose: () => void;
    campana: CampanaDetalle;
    onSuccess: () => void;
    isReschedule?: boolean;
}

export const SchedulingModal: React.FC<SchedulingModalProps> = ({
    isOpen,
    onClose,
    campana,
    onSuccess,
    isReschedule = false
}) => {
    const { showToast } = useToast();
    const [isSaving, setIsSaving] = useState(false);
    const [errors, setErrors] = useState<Record<string, string>>({});

    // Form data state - initialized with campaign data
    const [formData, setFormData] = useState({
        nombre: campana.nombre,
        tematica: campana.tematica,
        descripcion: campana.descripcion || '',
        prioridad: campana.prioridad,
        canalEjecucion: campana.canalEjecucion,
        idEncuesta: campana.idEncuesta,
        idSegmento: campana.idSegmento,
        fechaProgramadaInicio: campana.fechaProgramadaInicio || '',
        fechaProgramadaFin: campana.fechaProgramadaFin || '',
        idAgente: campana.idAgente
    });

    // Reset form when modal opens or campaign changes
    useEffect(() => {
        if (isOpen && campana) {
            setFormData({
                nombre: campana.nombre,
                tematica: campana.tematica,
                descripcion: campana.descripcion || '',
                prioridad: campana.prioridad,
                canalEjecucion: campana.canalEjecucion,
                idEncuesta: campana.idEncuesta,
                idSegmento: campana.idSegmento,
                fechaProgramadaInicio: campana.fechaProgramadaInicio || '',
                fechaProgramadaFin: campana.fechaProgramadaFin || '',
                idAgente: campana.idAgente
            });
            setErrors({});
        }
    }, [isOpen, campana]);

    const handleFieldChange = (field: string, value: any) => {
        setFormData(prev => ({ ...prev, [field]: value }));
        if (errors[field]) {
            setErrors(prev => {
                const newErrors = { ...prev };
                delete newErrors[field];
                return newErrors;
            });
        }
    };

    const validate = (): boolean => {
        const newErrors: Record<string, string> = {};

        if (!formData.fechaProgramadaInicio) {
            newErrors.fechaProgramadaInicio = 'Debe seleccionar una fecha y hora de inicio';
        } else {
            const startDate = new Date(formData.fechaProgramadaInicio);
            const now = new Date();
            if (startDate < now) {
                newErrors.fechaProgramadaInicio = 'La fecha de inicio debe ser futura';
            }
        }

        if (!formData.fechaProgramadaFin) {
            newErrors.fechaProgramadaFin = 'Debe seleccionar una fecha y hora de fin';
        } else if (formData.fechaProgramadaInicio) {
            const startDate = new Date(formData.fechaProgramadaInicio);
            const endDate = new Date(formData.fechaProgramadaFin);
            const diffInMs = endDate.getTime() - startDate.getTime();
            const diffInHours = diffInMs / (1000 * 60 * 60);

            if (endDate <= startDate) {
                newErrors.fechaProgramadaFin = 'La fecha de fin debe ser posterior a la de inicio';
            } else if (diffInHours < 1) {
                newErrors.fechaProgramadaFin = 'La campaña debe durar al menos 1 hora';
            }
        }

        if (!formData.idAgente) {
            newErrors.idAgente = 'Debe seleccionar un agente';
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = async () => {
        if (!validate()) return;

        setIsSaving(true);
        try {
            if (isReschedule) {
                const reschedulePayload = {
                    nuevaFechaInicio: formData.fechaProgramadaInicio,
                    nuevaFechaFin: formData.fechaProgramadaFin
                };
                await campanasApi.reprogramar(campana.idCampana, reschedulePayload);
                showToast('Campaña reprogramada exitosamente', 'success');
            } else {
                const schedulePayload = {
                    fechaProgramadaInicio: formData.fechaProgramadaInicio,
                    fechaProgramadaFin: formData.fechaProgramadaFin,
                    idAgente: formData.idAgente!,
                    idSegmento: formData.idSegmento || undefined
                };
                await campanasApi.programar(campana.idCampana, schedulePayload);
                showToast('Campaña programada exitosamente', 'success');
            }

            onSuccess();
            onClose();
        } catch (error: any) {
            console.error('Error scheduling campaign:', error);
            const errorMessage = error.response?.data?.message || `Error al ${isReschedule ? 'reprogramar' : 'programar'} la campaña`;
            showToast(errorMessage, 'error');
        } finally {
            setIsSaving(false);
        }
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 top-0 left-0 right-0 bottom-0 bg-black bg-opacity-50 flex items-center justify-center z-[60] p-4">
            <div className="bg-white rounded-lg shadow-xl max-w-2xl w-full flex flex-col max-h-[90vh]">
                {/* Header */}
                <div className="flex justify-between items-center p-6 border-b border-gray-200">
                    <div>
                        <h2 className="text-xl font-bold text-gray-900">
                            {isReschedule ? 'Reprogramar Campaña' : 'Programar Campaña'}
                        </h2>
                        <p className="text-sm text-gray-500 mt-1">
                            {isReschedule
                                ? 'Modifique las fechas y el agente asignado para esta campaña.'
                                : 'Establezca las fechas y asigne un agente para iniciar la campaña.'}
                        </p>
                    </div>
                    <button
                        onClick={onClose}
                        className="p-2 hover:bg-gray-100 rounded-full transition-colors"
                    >
                        <span className="material-symbols-outlined text-gray-500">close</span>
                    </button>
                </div>

                {/* Content */}
                <div className="p-6 overflow-y-auto">
                    <Step3Scheduling
                        formData={{
                            ...formData,
                            idEncuesta: formData.idEncuesta || undefined,
                            idSegmento: formData.idSegmento || undefined,
                            idAgente: formData.idAgente || undefined
                        }}
                        onChange={handleFieldChange}
                        errors={errors}
                        hideSummary={true}
                    />
                </div>

                {/* Footer */}
                <div className="flex items-center justify-end gap-3 p-6 border-t border-gray-200 bg-gray-50 rounded-b-lg">
                    <button
                        onClick={onClose}
                        className="px-4 py-2 text-sm font-semibold text-gray-700 hover:text-gray-900 hover:bg-gray-200 rounded-lg transition-colors"
                        disabled={isSaving}
                    >
                        Cancelar
                    </button>
                    <button
                        onClick={handleSubmit}
                        disabled={isSaving}
                        className="flex items-center gap-2 px-6 py-2 text-sm font-semibold text-white bg-primary rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-primary disabled:opacity-50 transition-colors shadow-sm"
                    >
                        {isSaving ? (
                            <>
                                <div className="h-4 w-4 animate-spin rounded-full border-2 border-white border-r-transparent"></div>
                                {isReschedule ? 'Reprogramando...' : 'Programando...'}
                            </>
                        ) : (
                            <>
                                <span className="material-symbols-outlined text-lg">
                                    {isReschedule ? 'update' : 'event_available'}
                                </span>
                                {isReschedule ? 'Validar y Reprogramar' : 'Validar y Programar'}
                            </>
                        )}
                    </button>
                </div>
            </div>
        </div>
    );
};
