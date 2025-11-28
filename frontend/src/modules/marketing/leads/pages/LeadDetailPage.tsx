import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { leadsApi } from '../services/leads.api';
import { Lead, LeadState } from '../types/lead.types';
import { LeadInfoCards } from '../components/LeadInfoCards';
import { Button } from '../../../../shared/components/ui/Button';
import { LeadStatusBadge } from '../components/LeadStatusBadge';
import { useLeadMutations } from '../hooks/useLeadMutations';
import { Modal } from '../../../../shared/components/ui/Modal';

export const LeadDetailPage: React.FC = () => {
    const { id } = useParams();
    const navigate = useNavigate();

    // Datos
    const [lead, setLead] = useState<Lead | null>(null);
    const [loading, setLoading] = useState(true);

    // Estado del Formulario
    const [selectedState, setSelectedState] = useState<LeadState>('NUEVO');
    const [motivo, setMotivo] = useState('');

    // Estado UI
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
    const { cualificarLead, eliminarLead, processing } = useLeadMutations();

    // 1. Carga Inicial
    const loadLead = () => {
        if (id) {
            setLoading(true);
            leadsApi.getById(Number(id))
                .then(res => {
                    setLead(res.data);
                    setSelectedState(res.data.estado);
                    setMotivo(''); // Limpiar motivo al cargar
                })
                .catch(() => navigate('/leads'))
                .finally(() => setLoading(false));
        }
    };

    useEffect(() => { loadLead(); }, [id, navigate]);

    // 2. Lógica "Dirty State" (¿Ha cambiado algo?)
    const hasChanges = lead ? (selectedState !== lead.estado) : false;
    const isSaveDisabled = !hasChanges || (selectedState === 'CALIFICADO' && motivo.trim().length < 5);

    // 3. Guardar
    const handleConfirmSave = async () => {
        if (!lead || !id) return;

        const result = await cualificarLead(Number(id), selectedState as 'CALIFICADO' | 'DESCARTADO', motivo);

        if (result.success) {
            setIsModalOpen(false);
            loadLead(); // Recargar datos para ver el nuevo historial
        } else {
            alert('Error: ' + result.error);
            setIsModalOpen(false);
        }
    };

    // Helper para formatear fecha
    const formatDate = (dateString: string) => {
        try {
            return new Date(dateString).toLocaleDateString('es-ES', {
                day: '2-digit',
                month: 'short',
                hour: '2-digit',
                minute: '2-digit'
            });
        } catch (e) {
            return dateString;
        }
    };

    if (loading || !lead) return <div className="p-10 text-center">Cargando detalle...</div>;

    return (
        <div className="space-y-6">
            {/* Header */}
            <header className="flex justify-between items-center">
                <div>
                    <h1 className="text-3xl font-bold text-dark">Detalle del Lead</h1>
                    <p className="text-gray-500 mt-1">{lead.nombreCompleto} (ID: {lead.id})</p>
                </div>
                <div className="flex space-x-3">
                    <Button variant="secondary" onClick={() => navigate('/leads')}>Volver</Button>
                </div>
            </header>

            <div className="grid grid-cols-1 lg:grid-cols-5 gap-6">
                {/* Izquierda: Info Cards */}
                <div className="lg:col-span-3">
                    <LeadInfoCards lead={lead} />
                </div>

                {/* Derecha: Acciones e Historial */}
                <div className="lg:col-span-2 space-y-6">

                    {/* Tarjeta de Gestión */}
                    <div className="bg-white rounded-lg shadow-card border border-separator p-5">
                        <h3 className="text-lg font-semibold text-dark border-b border-separator pb-4 mb-4">Gestión de Estado</h3>

                        <div className="space-y-4">
                            <div>
                                <label className="block text-sm font-medium mb-2 text-gray-600">Estado Actual</label>
                                <div className="mb-4"><LeadStatusBadge estado={lead.estado} /></div>

                                <label className="block text-sm font-medium mb-2 text-gray-600">Cambiar Estado a:</label>
                                <select
                                    className="w-full border border-separator rounded-lg px-3 py-2 bg-white focus:ring-2 focus:ring-primary focus:outline-none"
                                    value={selectedState}
                                    onChange={(e) => setSelectedState(e.target.value as LeadState)}
                                >
                                    <option value="NUEVO">NUEVO</option>
                                    <option value="CALIFICADO">CALIFICADO</option>
                                    <option value="DESCARTADO">DESCARTADO</option>
                                </select>
                            </div>

                            {/* Campo de Motivo (Solo si cambió el estado) */}
                            {hasChanges && (
                                <div className="animate-in slide-in-from-top-2 duration-200">
                                    <label className="block text-sm font-medium mb-2 text-gray-600">
                                        Motivo del cambio <span className="text-red-500">*</span>
                                    </label>
                                    <textarea
                                        className="w-full border border-separator rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-primary focus:outline-none"
                                        rows={3}
                                        placeholder="Explica por qué cambias el estado..."
                                        value={motivo}
                                        onChange={(e) => setMotivo(e.target.value)}
                                    />
                                </div>
                            )}

                            <Button
                                variant="primary"
                                className="w-full mt-2"
                                disabled={isSaveDisabled}
                                onClick={() => setIsModalOpen(true)}
                            >
                                Guardar Cambios
                            </Button>
                        </div>
                    </div>

                    {/* Tarjeta de Historial */}
                    <div className="bg-white rounded-lg shadow-card border border-separator overflow-hidden">
                        <div className="p-5 border-b border-separator bg-gray-50">
                            <h3 className="text-lg font-semibold text-dark">Historial de Cambios</h3>
                        </div>
                        <div className="overflow-x-auto">
                            <table className="w-full text-left text-sm">
                                <thead className="bg-gray-100 border-b border-separator">
                                    <tr>
                                        <th className="p-3 font-semibold text-gray-600">Fecha</th>
                                        <th className="p-3 font-semibold text-gray-600">Estado</th>
                                        <th className="p-3 font-semibold text-gray-600">Motivo</th>
                                    </tr>
                                </thead>
                                <tbody className="divide-y divide-separator">
                                    {lead.historial && lead.historial.length > 0 ? (
                                        lead.historial.map((h, idx) => (
                                            <tr key={idx} className="hover:bg-gray-50">
                                                <td className="p-3 text-gray-500 whitespace-nowrap">
                                                    {formatDate(h.fecha)}
                                                </td>
                                                <td className="p-3">
                                                    <LeadStatusBadge estado={h.estado} />
                                                </td>
                                                <td className="p-3 text-gray-600 italic">
                                                    "{h.motivo || 'Sin motivo'}"
                                                </td>
                                            </tr>
                                        ))
                                    ) : (
                                        <tr>
                                            <td colSpan={3} className="p-8 text-center text-gray-400">
                                                No hay historial registrado
                                            </td>
                                        </tr>
                                    )}
                                </tbody>
                            </table>
                        </div>
                    </div>

                    {/* ZONA DE PELIGRO */}
                    <div className="bg-white rounded-lg shadow-card border border-red-200 p-5 mt-6">
                        <h3 className="text-lg font-semibold text-red-600 border-b border-red-100 pb-4 mb-4">Zona de Peligro</h3>
                        <p className="text-sm text-gray-600 mb-4">
                            Esta acción eliminará permanentemente el lead y todo su historial. No se puede deshacer.
                        </p>
                        <Button
                            variant="danger"
                            className="w-full"
                            onClick={() => setIsDeleteModalOpen(true)}
                        >
                            Eliminar Lead
                        </Button>
                    </div>

                </div>
            </div>

            {/* Modal de Confirmación de Cambio de Estado */}
            <Modal
                isOpen={isModalOpen}
                title="Confirmar Cambio de Estado"
                onClose={() => setIsModalOpen(false)}
                onConfirm={handleConfirmSave}
                isLoading={processing}
                confirmText="Sí, Cambiar Estado"
            >
                <p>
                    Estás a punto de cambiar el estado de <strong>{lead.estado}</strong> a <strong>{selectedState}</strong>.
                </p>
                {motivo && (
                    <div className="mt-3 p-3 bg-gray-100 rounded-lg text-sm italic text-gray-600 border-l-4 border-primary">
                        "{motivo}"
                    </div>
                )}
                <p className="mt-4 text-sm text-gray-500">Esta acción quedará registrada en el historial.</p>
            </Modal>

            {/* Modal de Confirmación de Eliminación */}
            <Modal
                isOpen={isDeleteModalOpen}
                title="¿Eliminar Lead Permanentemente?"
                onClose={() => setIsDeleteModalOpen(false)}
                onConfirm={async () => {
                    const result = await eliminarLead(Number(id));
                    if (result.success) {
                        navigate('/leads'); // Volver al listado tras borrar
                    } else {
                        alert('Error: ' + result.error);
                        setIsDeleteModalOpen(false);
                    }
                }}
                confirmText="Sí, Eliminar"
                variant="danger"
                isLoading={processing}
            >
                <p>Estás a punto de eliminar a <strong>{lead.nombreCompleto}</strong>.</p>
                <p className="mt-2 text-sm text-red-500">Esta acción es irreversible.</p>
            </Modal>
        </div>
    );
};
