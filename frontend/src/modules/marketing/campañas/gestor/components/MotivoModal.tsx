import React, { useState } from 'react';

interface MotivoModalProps {
    isOpen: boolean;
    onClose: () => void;
    onConfirm: (motivo: string) => void;
    title: string;
    placeholder?: string;
}

export const MotivoModal: React.FC<MotivoModalProps> = ({
    isOpen,
    onClose,
    onConfirm,
    title,
    placeholder = 'Ingrese el motivo...',
}) => {
    const [motivo, setMotivo] = useState('');

    if (!isOpen) return null;

    const handleConfirm = () => {
        if (motivo.trim()) {
            onConfirm(motivo);
            setMotivo('');
            onClose();
        }
    };

    const handleCancel = () => {
        setMotivo('');
        onClose();
    };

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-[60] p-4">
            <div className="bg-white rounded-lg shadow-xl max-w-md w-full">
                {/* Header */}
                <div className="flex justify-between items-center p-4 border-b border-separator">
                    <h3 className="text-lg font-semibold text-dark">{title}</h3>
                    <button
                        onClick={handleCancel}
                        className="p-1 hover:bg-gray-100 rounded-full transition-colors"
                        title="Cerrar"
                    >
                        <span className="material-symbols-outlined text-xl">close</span>
                    </button>
                </div>

                {/* Content */}
                <div className="p-4">
                    <textarea
                        value={motivo}
                        onChange={(e) => setMotivo(e.target.value)}
                        placeholder={placeholder}
                        className="w-full px-3 py-2 border border-separator rounded-lg focus:ring-2 focus:ring-primary focus:outline-none resize-none"
                        rows={4}
                        autoFocus
                    />
                </div>

                {/* Footer */}
                <div className="flex justify-end gap-2 p-4 border-t border-separator">
                    <button
                        onClick={handleCancel}
                        className="px-4 py-2 text-gray-600 hover:bg-gray-100 rounded-lg transition-colors"
                    >
                        Cancelar
                    </button>
                    <button
                        onClick={handleConfirm}
                        disabled={!motivo.trim()}
                        className="px-4 py-2 bg-primary text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                        Confirmar
                    </button>
                </div>
            </div>
        </div>
    );
};
