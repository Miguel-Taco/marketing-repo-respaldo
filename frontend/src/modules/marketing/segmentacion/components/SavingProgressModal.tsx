import React from 'react';

interface SavingProgressModalProps {
    isOpen: boolean;
    progress: number;
    message: string;
}

export const SavingProgressModal: React.FC<SavingProgressModalProps> = ({ isOpen, progress, message }) => {
    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-xl shadow-2xl p-8 max-w-md w-full mx-4">
                {/* Icon */}
                <div className="flex justify-center mb-6">
                    <div className="relative">
                        <div className="w-20 h-20 rounded-full border-4 border-primary-light flex items-center justify-center">
                            <span className="material-symbols-outlined text-4xl text-primary animate-pulse">
                                save
                            </span>
                        </div>
                        {/* Spinning border */}
                        <div className="absolute inset-0 rounded-full border-4 border-transparent border-t-primary animate-spin"></div>
                    </div>
                </div>

                {/* Title */}
                <h3 className="text-xl font-bold text-center text-gray-900 mb-2">
                    Guardando Segmento
                </h3>

                {/* Message */}
                <p className="text-center text-gray-600 mb-6">{message}</p>

                {/* Progress Bar */}
                <div className="mb-4">
                    <div className="flex justify-between text-sm text-gray-600 mb-2">
                        <span>Progreso</span>
                        <span className="font-semibold">{progress}%</span>
                    </div>
                    <div className="w-full bg-gray-200 rounded-full h-3 overflow-hidden">
                        <div
                            className="bg-gradient-to-r from-primary to-primary-dark h-full rounded-full transition-all duration-300 ease-out"
                            style={{ width: `${progress}%` }}
                        >
                            <div className="h-full w-full bg-white opacity-20 animate-pulse"></div>
                        </div>
                    </div>
                </div>

                {/* Steps indicator */}
                <div className="space-y-2 text-sm">
                    <div className={`flex items-center ${progress >= 33 ? 'text-primary' : 'text-gray-400'}`}>
                        <span className="material-symbols-outlined text-lg mr-2">
                            {progress >= 33 ? 'check_circle' : 'radio_button_unchecked'}
                        </span>
                        <span>Validando datos del segmento</span>
                    </div>
                    <div className={`flex items-center ${progress >= 66 ? 'text-primary' : 'text-gray-400'}`}>
                        <span className="material-symbols-outlined text-lg mr-2">
                            {progress >= 66 ? 'check_circle' : 'radio_button_unchecked'}
                        </span>
                        <span>Aplicando filtros y reglas</span>
                    </div>
                    <div className={`flex items-center ${progress >= 100 ? 'text-primary' : 'text-gray-400'}`}>
                        <span className="material-symbols-outlined text-lg mr-2">
                            {progress >= 100 ? 'check_circle' : 'radio_button_unchecked'}
                        </span>
                        <span>Finalizando creación</span>
                    </div>
                </div>
            </div>
        </div>
    );
};
