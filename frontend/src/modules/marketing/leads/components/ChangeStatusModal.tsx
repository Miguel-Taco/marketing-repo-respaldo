import React, { useState } from 'react';
import { Modal } from '../../../../shared/components/ui/Modal';
import { Lead } from '../types/lead.types';

interface ChangeStatusModalProps {
    isOpen: boolean;
    onClose: () => void;
    onConfirm: (nuevoEstado: string, motivo?: string) => void;
    selectedLeads: Lead[];
    isLoading?: boolean;
}

const ESTADOS = [
    { value: 'CALIFICADO', label: 'Calificado', color: 'bg-green-100 text-green-800' },
    { value: 'DESCARTADO', label: 'Descartado', color: 'bg-red-100 text-red-800' },
];

export const ChangeStatusModal: React.FC<ChangeStatusModalProps> = ({
    isOpen,
    onClose,
    onConfirm,
    selectedLeads,
    isLoading = false
}) => {
    const [selectedEstado, setSelectedEstado] = useState<string>('');
    const [motivo, setMotivo] = useState<string>('');

    const handleConfirm = () => {
        if (!selectedEstado) return;
        onConfirm(selectedEstado, motivo || undefined);
    };

    const handleClose = () => {
        setSelectedEstado('');
        setMotivo('');
        onClose();
    };

    // Verificar si todos los leads seleccionados ya tienen un estado específico
    const isStateDisabled = (estadoValue: string) => {
        if (selectedLeads.length === 0) return false;
        return selectedLeads.every(lead => lead.estado === estadoValue);
    };

    return (
        <Modal isOpen={isOpen} onClose={handleClose} title="Cambiar Estado de Leads">
            <div className="space-y-4">
                {/* Info */}
                <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                    <div className="flex items-start">
                        <span className="material-symbols-outlined text-blue-600 mr-3">info</span>
                        <div>
                            <p className="text-sm font-medium text-blue-900">
                                Se cambiarán {selectedLeads.length} lead{selectedLeads.length !== 1 ? 's' : ''}
                            </p>
                            <p className="text-sm text-blue-700 mt-1">
                                Selecciona el nuevo estado que deseas aplicar a todos los leads seleccionados.
                            </p>
                        </div>
                    </div>
                </div>

                {/* Selector de Estado */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                        Nuevo Estado <span className="text-red-500">*</span>
                    </label>
                    <div className="grid grid-cols-2 gap-3">
                        {ESTADOS.map((estado) => {
                            const isDisabled = isStateDisabled(estado.value);
                            return (
                                <button
                                    key={estado.value}
                                    type="button"
                                    onClick={() => !isDisabled && setSelectedEstado(estado.value)}
                                    disabled={isDisabled}
                                    className={`p-3 rounded-lg border-2 transition-all text-left ${selectedEstado === estado.value
                                            ? 'border-primary bg-primary-50'
                                            : isDisabled
                                                ? 'border-gray-100 bg-gray-50 opacity-50 cursor-not-allowed'
                                                : 'border-gray-200 hover:border-gray-300'
                                        }`}
                                >
                                    <div className="flex items-center gap-2">
                                        <div className={`w-3 h-3 rounded-full ${selectedEstado === estado.value ? 'bg-primary' : isDisabled ? 'bg-gray-400' : 'bg-gray-300'
                                            }`} />
                                        <span className={`font-medium ${isDisabled ? 'text-gray-400' : 'text-dark'}`}>
                                            {estado.label}
                                        </span>
                                    </div>
                                    {isDisabled && (
                                        <p className="text-xs text-gray-500 mt-1 ml-5">
                                            Todos los leads ya tienen este estado
                                        </p>
                                    )}
                                </button>
                            );
                        })}
                    </div>
                </div>

                {/* Motivo (Opcional) */}
                <div>
                    <label htmlFor="motivo" className="block text-sm font-medium text-gray-700 mb-2">
                        Motivo del cambio (opcional)
                    </label>
                    <textarea
                        id="motivo"
                        rows={3}
                        value={motivo}
                        onChange={(e) => setMotivo(e.target.value)}
                        placeholder="Ej: Campaña de verano - Leads cualificados"
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent resize-none"
                    />
                    <p className="text-xs text-gray-500 mt-1">
                        Este motivo quedará registrado en el historial de cada lead
                    </p>
                </div>

                {/* Botones de Acción */}
                <div className="flex justify-end gap-3 pt-4 border-t">
                    <button
                        onClick={handleClose}
                        className="px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-lg transition-colors"
                        disabled={isLoading}
                    >
                        Cancelar
                    </button>
                    <button
                        onClick={handleConfirm}
                        disabled={!selectedEstado || isLoading}
                        className="px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary-dark transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
                    >
                        {isLoading && (
                            <div className="inline-block h-4 w-4 animate-spin rounded-full border-2 border-solid border-white border-r-transparent" />
                        )}
                        {isLoading ? 'Actualizando...' : 'Confirmar Cambio'}
                    </button>
                </div>
            </div>
        </Modal>
    );
};
