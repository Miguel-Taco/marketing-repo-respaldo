import React from 'react';
import { createPortal } from 'react-dom';
import { Button } from './Button';

interface ModalProps {
    isOpen: boolean;
    title: React.ReactNode;
    children: React.ReactNode;
    onClose?: () => void;
    onConfirm?: () => void;
    confirmText?: string;
    isLoading?: boolean;
    variant?: 'primary' | 'danger';
}

export const Modal: React.FC<ModalProps> = ({
    isOpen,
    title,
    children,
    onClose,
    onConfirm,
    confirmText = 'Confirmar',
    isLoading = false,
    variant = 'primary'
}) => {
    if (!isOpen) return null;

    return createPortal(
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm">
            <div className="bg-white rounded-xl shadow-2xl w-full max-w-md animate-in fade-in zoom-in duration-200">

                {/* Header */}
                <div className="flex justify-between items-center p-5 border-b border-separator">
                    <h3 className="text-lg font-bold text-dark">{title}</h3>
                    {onClose && (
                        <button onClick={onClose} className="text-gray-400 hover:text-gray-600 transition-colors">
                            <span className="material-symbols-outlined">close</span>
                        </button>
                    )}
                </div>

                {/* Body */}
                <div className="p-6 text-gray-600">
                    {children}
                </div>

                {/* Footer */}
                <div className="flex justify-end gap-3 p-5 bg-gray-50 rounded-b-xl border-t border-separator">
                    {onClose && (
                        <Button variant="secondary" onClick={onClose} disabled={isLoading}>
                            Cancelar
                        </Button>
                    )}
                    {onConfirm && (
                        <Button
                            variant={variant === 'danger' ? 'danger' : 'primary'}
                            onClick={onConfirm}
                            isLoading={isLoading}
                        >
                            {confirmText}
                        </Button>
                    )}
                </div>
            </div>
        </div>,
        document.body
    );
};
