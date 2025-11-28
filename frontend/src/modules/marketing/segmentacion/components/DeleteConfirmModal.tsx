import React from 'react';
import { Modal } from '../../../../shared/components/ui/Modal';

interface DeleteConfirmModalProps {
    isOpen: boolean;
    onClose: () => void;
    onConfirm: () => void;
    selectedCount: number;
    isDeleting?: boolean;
}

export const DeleteConfirmModal: React.FC<DeleteConfirmModalProps> = ({
    isOpen,
    onClose,
    onConfirm,
    selectedCount,
    isDeleting = false
}) => {
    return (
        <Modal
            isOpen={isOpen}
            onClose={onClose}
            onConfirm={onConfirm}
            title="Confirmar eliminación"
            confirmText="Eliminar"
            variant="danger"
            isLoading={isDeleting}
        >
            <div className="space-y-3">
                <p className="text-gray-700">
                    ¿Estás seguro de que deseas eliminar {selectedCount} segmento{selectedCount > 1 ? 's' : ''}?
                </p>
                <p className="text-sm text-gray-500">
                    Los segmentos eliminados se moverán a "Eliminados recientemente" y podrán ser recuperados.
                </p>
            </div>
        </Modal>
    );
};
