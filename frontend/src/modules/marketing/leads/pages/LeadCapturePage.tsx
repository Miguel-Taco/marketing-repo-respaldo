import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { LeadForm } from '../components/LeadForm';
import { useLeadMutations } from '../hooks/useLeadMutations';
import { CreateLeadDTO } from '../types/lead.types';
import { Modal } from '../../../../shared/components/ui/Modal';
import { useLeadsContext } from '../context/LeadsContext';

export const LeadCapturePage: React.FC = () => {
    const navigate = useNavigate();
    const { createLead, processing } = useLeadMutations();
    const { fetchLeads } = useLeadsContext();
    const [showSuccessModal, setShowSuccessModal] = useState(false);
    const [showErrorModal, setShowErrorModal] = useState(false);
    const [errorMessage, setErrorMessage] = useState('');

    const handleSubmit = async (data: CreateLeadDTO) => {
        const result = await createLead(data);
        if (result.success) {
            // Refresh the leads list so it's ready when user navigates back
            fetchLeads(true);
            setShowSuccessModal(true);
        } else {
            setErrorMessage(result.error || 'Error desconocido al crear el lead');
            setShowErrorModal(true);
        }
    };

    const handleCloseModal = () => {
        setShowSuccessModal(false);
        navigate('/leads');
    };

    const handleCancel = () => {
        navigate('/leads');
    };

    return (
        <div className="py-8 px-4">
            <div className="mb-8">
                <h1 className="text-3xl font-bold text-dark">Registrar Nuevo Lead Manual</h1>
                <p className="text-gray-500 mt-2">
                    Los datos ingresados pasarán por el proceso de validación (Staging).
                </p>
            </div>

            <LeadForm
                isLoading={processing}
                onSubmit={handleSubmit}
                onCancel={handleCancel}
            />

            <Modal
                isOpen={showSuccessModal}
                title={<div className="flex items-center gap-2"><span className="material-symbols-outlined text-green-600">check_circle</span> Lead Creado Exitosamente</div>}
                onClose={handleCloseModal}
                onConfirm={handleCloseModal}
                confirmText="Ir a la Lista de Leads"
                variant="primary"
            >
                <div className="text-center py-4">
                    <div className="mb-4 flex justify-center">
                        <span className="material-symbols-outlined text-6xl text-green-500">celebration</span>
                    </div>
                    <p className="text-lg font-semibold text-dark mb-2">
                        ¡El lead ha sido registrado correctamente!
                    </p>
                    <p className="text-gray-500">
                        Los datos han sido guardados y el lead está siendo procesado.
                    </p>
                </div>
            </Modal>

            <Modal
                isOpen={showErrorModal}
                title={<div className="flex items-center gap-2"><span className="material-symbols-outlined text-red-600">error</span> Error al Crear Lead</div>}
                onClose={() => setShowErrorModal(false)}
                onConfirm={() => setShowErrorModal(false)}
                confirmText="Entendido"
                variant="danger"
            >
                <div className="text-center py-4">
                    <div className="mb-4 flex justify-center">
                        <span className="material-symbols-outlined text-6xl text-amber-500">warning</span>
                    </div>
                    <p className="text-lg font-semibold text-dark mb-2">
                        No se pudo crear el lead
                    </p>
                    <p className="text-gray-600">
                        {errorMessage}
                    </p>
                </div>
            </Modal>
        </div>
    );
};
