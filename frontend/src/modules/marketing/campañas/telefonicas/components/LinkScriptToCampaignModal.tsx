import React, { useState, useEffect } from 'react';
import { X, Link, AlertCircle } from 'lucide-react';
import { GuionDTO } from '../types/guiones.types';
import { telemarketingApi } from '../services/telemarketingApi';
import { useAuth } from '../../../../../shared/context/AuthContext';
import { guionesApi } from '../services/guiones.api';
import { CampaniaTelefonica } from '../types';

interface LinkScriptToCampaignModalProps {
    isOpen: boolean;
    onClose: () => void;
    guion: GuionDTO | null;
    onSuccess?: () => void;
}

export const LinkScriptToCampaignModal: React.FC<LinkScriptToCampaignModalProps> = ({
    isOpen,
    onClose,
    guion,
    onSuccess,
}) => {
    const [campanias, setCampanias] = useState<CampaniaTelefonica[]>([]);
    const { user } = useAuth();
    const idAgente = user?.agentId;
    const [selectedCampania, setSelectedCampania] = useState<number | null>(null);
    const [loading, setLoading] = useState(false);
    const [linking, setLinking] = useState(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (isOpen) {
            loadCampanias();
            setSelectedCampania(null);
            setError(null);
        }
    }, [isOpen, idAgente]);

    const loadCampanias = async () => {
        try {
            setLoading(true);
            if (!idAgente) {
                setCampanias([]);
                setError('No hay agente asignado para cargar campa?as');
                setLoading(false);
                return;
            }
            const data = await telemarketingApi.getCampaniasAsignadas();
            setCampanias(data);
        } catch (err) {
            console.error('Error cargando campañas:', err);
            setError('No se pudieron cargar las campañas disponibles');
        } finally {
            setLoading(false);
        }
    };

    const handleSubmit = async () => {
        if (!selectedCampania || !guion?.id) return;

        try {
            setLinking(true);
            setError(null);
            await guionesApi.vincularGuionACampana(selectedCampania, guion.id);
            onSuccess?.();
            onClose();
        } catch (err: any) {
            console.error('Error vinculando guión:', err);
            setError(err.response?.data?.message || 'Error al vincular el guión a la campaña');
        } finally {
            setLinking(false);
        }
    };

    if (!isOpen || !guion) return null;

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-lg shadow-xl w-full max-w-md">
                {/* Header */}
                <div className="flex items-center justify-between p-6 border-b">
                    <div className="flex items-center gap-3">
                        <Link className="w-6 h-6 text-blue-600" />
                        <h2 className="text-xl font-bold text-gray-900">Vincular a Campaña</h2>
                    </div>
                    <button
                        onClick={onClose}
                        className="text-gray-400 hover:text-gray-600 transition-colors"
                    >
                        <X className="w-6 h-6" />
                    </button>
                </div>

                {/* Content */}
                <div className="p-6 space-y-4">
                    <div className="bg-blue-50 p-4 rounded-lg border border-blue-100">
                        <p className="text-sm text-blue-800 font-medium">
                            Estás vinculando el guión:
                        </p>
                        <p className="text-lg font-bold text-blue-900 mt-1">
                            {guion.nombre}
                        </p>
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            Selecciona la campaña destino
                        </label>
                        {loading ? (
                            <div className="animate-pulse h-10 bg-gray-100 rounded-lg"></div>
                        ) : (
                            <select
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                value={selectedCampania || ''}
                                onChange={(e) => setSelectedCampania(Number(e.target.value))}
                            >
                                <option value="">Seleccionar campaña...</option>
                                {campanias.map(campania => (
                                    <option key={campania.id} value={campania.id}>
                                        {campania.nombre}
                                    </option>
                                ))}
                            </select>
                        )}
                    </div>

                    {error && (
                        <div className="flex items-start gap-2 p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm">
                            <AlertCircle className="w-5 h-5 flex-shrink-0" />
                            <p>{error}</p>
                        </div>
                    )}
                </div>

                {/* Footer */}
                <div className="flex items-center justify-end gap-3 p-6 border-t bg-gray-50">
                    <button
                        onClick={onClose}
                        className="px-4 py-2 text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
                        disabled={linking}
                    >
                        Cancelar
                    </button>
                    <button
                        onClick={handleSubmit}
                        disabled={!selectedCampania || linking}
                        className="flex items-center gap-2 px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                        {linking ? 'Vinculando...' : 'Vincular Guión'}
                    </button>
                </div>
            </div>
        </div>
    );
};
