import React from 'react';
import { Modal } from '../../../../shared/components/ui/Modal';

interface DeleteLeadsModalProps {
    isOpen: boolean;
    leadCount: number;
    onClose: () => void;
    onConfirm: () => void;
    isLoading?: boolean;
}

export const DeleteLeadsModal: React.FC<DeleteLeadsModalProps> = ({
    isOpen,
    leadCount,
    onClose,
    onConfirm,
    isLoading = false
}) => {
    return (
        <Modal
            isOpen={isOpen}
            title="Confirmar Eliminación"
            onClose={onClose}
            onConfirm={onConfirm}
            confirmText="Eliminar"
            isLoading={isLoading}
            variant="danger"
        >
            <div className="space-y-3">
                <p className="text-gray-700">
                    ¿Estás seguro de que deseas eliminar{' '}
                    <span className="font-bold text-dark">
                        {leadCount} {leadCount === 1 ? 'lead' : 'leads'}
                    </span>
                    ?
                </p>
                <div className="bg-red-50 border border-red-200 rounded-lg p-3">
                    <div className="flex items-start">
                        <span className="material-symbols-outlined text-red-600 text-xl mr-2">warning</span>
                        <p className="text-sm text-red-700">
                            Esta acción es <strong>irreversible</strong>. Todos los datos asociados a{' '}
                            {leadCount === 1 ? 'este lead' : 'estos leads'} serán eliminados permanentemente.
                        </p>
                    </div>
                </div>
            </div>
        </Modal>
    );
};
