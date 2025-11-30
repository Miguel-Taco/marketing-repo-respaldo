import React, { useState, useEffect } from 'react';
import { CanalEjecucion, Prioridad } from '../types/campana.types';
import { campanasApi } from '../services/campanas.api';
import { segmentosApi } from '../services/segmentos.api';
import { useToast } from '../../../../../shared/components/ui/Toast';
import { StepIndicator } from './StepIndicator';
import { Step1BasicData } from './Step1BasicData';
import { Step2SegmentSelection } from './Step2SegmentSelection';
import { Step3Scheduling } from './Step3Scheduling';
import { PlantillaSelectionModal } from './PlantillaSelectionModal';
import { PlantillaCampana } from '../types/plantilla.types';

interface CreateCampanaModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSuccess?: () => void;
}

interface CampanaFormData {
    // Step 1: Datos Básicos
    nombre: string;
    tematica: string;
    descripcion: string;
    prioridad: Prioridad | '';
    canalEjecucion: CanalEjecucion | '';
    idEncuesta: number | undefined;

    // Step 2: Selección de Segmento
    idSegmento: number | undefined;

    // Step 3: Programación
    fechaProgramadaInicio: string;
    fechaProgramadaFin: string;
    idAgente: number | undefined;
}

const STEPS = ['Datos Básicos', 'Segmento', 'Programación'];

export const CreateCampanaModal: React.FC<CreateCampanaModalProps> = ({
    isOpen,
    onClose,
    onSuccess,
}) => {
    const [currentStep, setCurrentStep] = useState(1);
    const [formData, setFormData] = useState<CampanaFormData>({
        nombre: '',
        tematica: '',
        descripcion: '',
        prioridad: '',
        canalEjecucion: '',
        idEncuesta: undefined,
        idSegmento: undefined,
        fechaProgramadaInicio: '',
        fechaProgramadaFin: '',
        idAgente: undefined,
    });
    const [errors, setErrors] = useState<Record<string, string>>({});
    const [isSaving, setIsSaving] = useState(false);
    const [isSaved, setIsSaved] = useState(false);
    const [draftId, setDraftId] = useState<number | null>(null);
    const [selectedSegmentName, setSelectedSegmentName] = useState<string>('');
    const [selectedSegmentSize, setSelectedSegmentSize] = useState<number>(0);
    const [isTemplateModalOpen, setIsTemplateModalOpen] = useState(false);
    const { showToast } = useToast();

    // Load segment name and size when idSegmento changes
    useEffect(() => {
        const loadSegmentInfo = async () => {
            if (formData.idSegmento) {
                try {
                    const segment = await segmentosApi.getById(formData.idSegmento);
                    setSelectedSegmentName(segment.nombre);
                    setSelectedSegmentSize(segment.cantidadMiembros || 0);
                } catch (error) {
                    console.error('Error loading segment info:', error);
                }
            }
        };
        loadSegmentInfo();
    }, [formData.idSegmento]);

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

    const handleTemplateSelect = (plantilla: PlantillaCampana) => {
        setFormData(prev => ({
            ...prev,
            nombre: `${plantilla.nombre} (Copia)`,
            tematica: plantilla.tematica,
            descripcion: plantilla.descripcion || '',
            canalEjecucion: plantilla.canalEjecucion || '',
            idEncuesta: plantilla.idEncuesta,
            idSegmento: plantilla.idSegmento,
        }));
        setIsTemplateModalOpen(false);
        showToast('Datos de plantilla aplicados', 'success');
    };

    const validateStep1 = (): boolean => {
        const newErrors: Record<string, string> = {};

        if (!formData.nombre || formData.nombre.trim().length < 3) {
            newErrors.nombre = 'El nombre debe tener al menos 3 caracteres';
        }
        if (formData.nombre && formData.nombre.length > 100) {
            newErrors.nombre = 'El nombre no puede exceder 100 caracteres';
        }

        if (!formData.tematica || formData.tematica.trim().length < 3) {
            newErrors.tematica = 'La temática debe tener al menos 3 caracteres';
        }
        if (formData.tematica && formData.tematica.length > 100) {
            newErrors.tematica = 'La temática no puede exceder 100 caracteres';
        }

        if (formData.descripcion && formData.descripcion.length > 500) {
            newErrors.descripcion = 'La descripción no puede exceder 500 caracteres';
        }

        if (!formData.canalEjecucion) {
            newErrors.canalEjecucion = 'Debe seleccionar un canal de ejecución';
        }

        if (!formData.prioridad) {
            newErrors.prioridad = 'Debe seleccionar una prioridad';
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const validateStep2 = (): boolean => {
        const newErrors: Record<string, string> = {};

        if (!formData.idSegmento) {
            newErrors.idSegmento = 'Debe seleccionar un segmento';
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const validateStep3 = (): boolean => {
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

            // Calculate difference in milliseconds
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

    const handleCancel = () => {
        setFormData({
            nombre: '',
            tematica: '',
            descripcion: '',
            prioridad: '',
            canalEjecucion: '',
            idEncuesta: undefined,
            idSegmento: undefined,
            fechaProgramadaInicio: '',
            fechaProgramadaFin: '',
            idAgente: undefined,
        });
        setCurrentStep(1);
        setErrors({});
        setIsSaved(false);
        setDraftId(null);
        onClose();
    };

    const handleSubmit = async (): Promise<void> => {
        if (!validateStep1() || !validateStep2() || !validateStep3()) {
            showToast('Por favor complete todos los campos requeridos', 'error');
            return;
        }

        setIsSaving(true);
        try {
            let campaignId = draftId;

            if (campaignId) {
                // Update existing draft
                const updateData = {
                    nombre: formData.nombre,
                    tematica: formData.tematica,
                    descripcion: formData.descripcion || undefined,
                    prioridad: formData.prioridad as Prioridad,
                    canalEjecucion: formData.canalEjecucion as CanalEjecucion,
                    idEncuesta: formData.idEncuesta || undefined,
                    idSegmento: formData.idSegmento,
                    idAgente: formData.idAgente,
                };
                await campanasApi.update(campaignId, updateData);
            } else {
                // Create new campaign
                const campaignData = {
                    nombre: formData.nombre,
                    tematica: formData.tematica,
                    descripcion: formData.descripcion || undefined,
                    prioridad: formData.prioridad as Prioridad,
                    canalEjecucion: formData.canalEjecucion as CanalEjecucion,
                    idEncuesta: formData.idEncuesta || undefined,
                    idSegmento: formData.idSegmento,
                    idAgente: formData.idAgente,
                };
                const createdCampaign = await campanasApi.create(campaignData);
                campaignId = createdCampaign.idCampana;
            }

            // Program the campaign
            if (campaignId) {
                await campanasApi.programar(campaignId, {
                    fechaProgramadaInicio: formData.fechaProgramadaInicio,
                    fechaProgramadaFin: formData.fechaProgramadaFin,
                    idAgente: formData.idAgente!,
                    idSegmento: formData.idSegmento,
                });
            }

            showToast('Campaña creada y programada exitosamente', 'success');

            if (onSuccess) {
                onSuccess();
            }

            setTimeout(() => {
                handleCancel();
            }, 1500);
        } catch (error: any) {
            console.error('Error creating campaign:', error);
            const errorMessage = error.response?.data?.message || 'Error al crear la campaña';
            showToast(errorMessage, 'error');
        } finally {
            setIsSaving(false);
        }
    };

    const handleNext = (): void => {
        if (currentStep === 1 && validateStep1()) {
            setCurrentStep(2);
        } else if (currentStep === 2 && validateStep2()) {
            setCurrentStep(3);
        } else if (currentStep === 3 && validateStep3()) {
            handleSubmit();
        }
    };

    const handlePrevious = (): void => {
        if (currentStep > 1) {
            setCurrentStep(currentStep - 1);
        }
    };

    const handleSaveAsDraft = async (): Promise<void> => {
        if (!validateStep1()) {
            showToast('Por favor complete los datos básicos requeridos', 'error');
            setCurrentStep(1);
            return;
        }

        setIsSaving(true);
        try {
            if (draftId) {
                // Update existing draft
                const updateData = {
                    nombre: formData.nombre,
                    tematica: formData.tematica,
                    descripcion: formData.descripcion || undefined,
                    prioridad: formData.prioridad as Prioridad,
                    canalEjecucion: formData.canalEjecucion as CanalEjecucion,
                    idEncuesta: formData.idEncuesta || undefined,
                    idSegmento: formData.idSegmento || undefined,
                };
                await campanasApi.update(draftId, updateData);
            } else {
                // Create new draft
                const draftData = {
                    nombre: formData.nombre,
                    tematica: formData.tematica,
                    descripcion: formData.descripcion || undefined,
                    prioridad: formData.prioridad as Prioridad,
                    canalEjecucion: formData.canalEjecucion as CanalEjecucion,
                    idEncuesta: formData.idEncuesta || undefined,
                    idSegmento: formData.idSegmento || undefined,
                };
                const createdDraft = await campanasApi.create(draftData);
                setDraftId(createdDraft.idCampana);
            }

            showToast('Campaña guardada como borrador exitosamente', 'success');
            setIsSaved(true);

            if (onSuccess) {
                onSuccess();
            }
        } catch (error: any) {
            console.error('Error saving draft:', error);
            const errorMessage = error.response?.data?.message || 'Error al guardar la campaña como borrador';
            showToast(errorMessage, 'error');
        } finally {
            setIsSaving(false);
        }
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 top-0 left-0 right-0 bottom-0 bg-black bg-opacity-50 flex items-center justify-center z-[60] p-4">
            <div className="bg-white rounded-lg shadow-xl max-w-4xl w-full max-h-[90vh] overflow-hidden flex flex-col">
                {/* Header */}
                <div className="flex justify-between items-center p-6 border-b border-separator">
                    <div>
                        <h2 className="text-2xl font-bold text-dark">Crear Nueva Campaña</h2>
                        <p className="text-sm text-gray-600 mt-1">
                            Complete los siguientes campos para configurar su campaña.
                        </p>
                    </div>
                    <div className="flex items-center gap-3">
                        {currentStep === 1 && (
                            <button
                                type="button"
                                onClick={() => setIsTemplateModalOpen(true)}
                                className="flex items-center px-4 py-2 text-sm font-medium text-primary bg-blue-50 rounded-lg hover:bg-blue-100 transition-colors"
                            >
                                <span className="material-symbols-outlined text-lg mr-2">auto_fix_high</span>
                                Usar Plantilla
                            </button>
                        )}
                        <button
                            onClick={handleCancel}
                            className="p-2 hover:bg-gray-100 rounded-full transition-colors"
                            title="Cerrar"
                        >
                            <span className="material-symbols-outlined text-2xl">close</span>
                        </button>
                    </div>
                </div>

                {/* Content */}
                <div className="flex-1 overflow-y-auto p-6">
                    <StepIndicator currentStep={currentStep} steps={STEPS} />

                    {currentStep === 1 && (
                        <Step1BasicData
                            formData={formData}
                            onChange={handleFieldChange}
                            errors={errors}
                        />
                    )}

                    {currentStep === 2 && (
                        <Step2SegmentSelection
                            formData={formData}
                            onChange={handleFieldChange}
                            errors={errors}
                        />
                    )}

                    {currentStep === 3 && (
                        <Step3Scheduling
                            formData={formData}
                            selectedSegmentName={selectedSegmentName}
                            selectedSegmentSize={selectedSegmentSize}
                            onChange={handleFieldChange}
                            errors={errors}
                        />
                    )}
                </div>

                {/* Footer */}
                <div className="flex items-center justify-between p-6 border-t border-separator">
                    <button
                        onClick={handleCancel}
                        className="px-4 py-2 text-sm font-semibold text-gray-600 hover:text-gray-800 focus:outline-none"
                        disabled={isSaving}
                    >
                        Cancelar
                    </button>

                    <div className="flex gap-3">
                        {currentStep > 1 && (
                            <button
                                onClick={handlePrevious}
                                disabled={isSaving}
                                className="flex items-center gap-2 px-6 py-2.5 text-sm font-semibold text-gray-700 border border-gray-300 rounded-full hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-primary disabled:opacity-50"
                            >
                                <span className="material-symbols-outlined">arrow_back</span>
                                Anterior
                            </button>
                        )}

                        {/* Save as Draft button - Between Anterior and Siguiente */}
                        {currentStep >= 2 && currentStep < 3 && (
                            <button
                                onClick={handleSaveAsDraft}
                                disabled={isSaving || isSaved}
                                className="flex items-center gap-2 px-6 py-2.5 text-sm font-semibold text-white bg-primary rounded-full hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-primary disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                                {isSaving ? (
                                    <>
                                        <div className="inline-block h-4 w-4 animate-spin rounded-full border-2 border-solid border-white border-r-transparent"></div>
                                        Guardando...
                                    </>
                                ) : isSaved ? (
                                    <>
                                        <span className="material-symbols-outlined">check</span>
                                        Guardado
                                    </>
                                ) : (
                                    <>
                                        <span className="material-symbols-outlined">save</span>
                                        Guardar
                                    </>
                                )}
                            </button>
                        )}

                        <button
                            onClick={handleNext}
                            disabled={isSaving}
                            className="flex items-center gap-2 px-6 py-2.5 text-sm font-semibold text-white bg-primary rounded-full hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-primary disabled:opacity-50"
                        >
                            {currentStep === 3 ? (
                                isSaving ? (
                                    <>
                                        <div className="inline-block h-4 w-4 animate-spin rounded-full border-2 border-solid border-white border-r-transparent"></div>
                                        Programando...
                                    </>
                                ) : (
                                    'Validar y Programar'
                                )
                            ) : (
                                <>
                                    {`Siguiente: ${STEPS[currentStep]}`}
                                    <span className="material-symbols-outlined">arrow_forward</span>
                                </>
                            )}
                        </button>
                    </div>
                </div>
            </div>

            <PlantillaSelectionModal
                isOpen={isTemplateModalOpen}
                onClose={() => setIsTemplateModalOpen(false)}
                onSelect={handleTemplateSelect}
            />
        </div>
    );
};
