import React from 'react';
import { useParams } from 'react-router-dom';

export const MailingEditorPage: React.FC = () => {
    const { id } = useParams<{ id: string }>();

    return (
        <div className="space-y-6">
            <header className="flex justify-between items-center">
                <div>
                    <h1 className="text-3xl font-bold text-dark">Editar Campaña de Mailing</h1>
                    <p className="text-gray-500 mt-1">ID: {id}</p>
                </div>
            </header>

            <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-100">
                <p className="text-gray-500">Editor en desarrollo...</p>
            </div>
        </div>
    );
};