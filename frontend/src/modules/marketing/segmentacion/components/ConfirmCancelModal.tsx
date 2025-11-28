import React from 'react';

interface ConfirmCancelModalProps {
    isOpen: boolean;
    onConfirm: () => void;
    onCancel: () => void;
}

export const ConfirmCancelModal: React.FC<ConfirmCancelModalProps> = ({ isOpen, onConfirm, onCancel }) => {
    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-xl shadow-2xl p-6 max-w-md w-full mx-4">
                {/* Icon */}
                <div className="flex justify-center mb-4">
                    <div className="w-16 h-16 rounded-full bg-yellow-100 flex items-center justify-center">
                        <span className="material-symbols-outlined text-4xl text-yellow-600">
                            warning
                        </span>
                    </div>
                </div>

                {/* Title */}
                <h3 className="text-xl font-bold text-center text-gray-900 mb-2">
                    ¿Estás seguro de cancelar?
                </h3>

                {/* Message */}
                <p className="text-center text-gray-600 mb-6">
                    Se perderán todos los cambios no guardados. Esta acción no se puede deshacer.
                </p>

                {/* Buttons */}
                <div className="flex gap-3">
                    <button
                        onClick={onCancel}
                        className="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-gray-700 font-medium hover:bg-gray-50 transition-colors"
                    >
                        Continuar editando
                    </button>
                    <button
                        onClick={onConfirm}
                        className="flex-1 px-4 py-2 bg-red-600 text-white rounded-lg font-medium hover:bg-red-700 transition-colors"
                    >
                        Sí, cancelar
                    </button>
                </div>
            </div>
        </div>
    );
};
