import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { telemarketingApi } from '../services/telemarketingApi';
import type { Contacto, Guion, ResultadoLlamadaRequest } from '../types';
import { CallResultModal } from '../components/CallResultModal';
import { Button } from '../../../../../shared/components/ui/Button';

export const CallScreenPage: React.FC = () => {
    const { id, idContacto } = useParams<{ id: string; idContacto: string }>();
    const navigate = useNavigate();
    const [contacto, setContacto] = useState<Contacto | null>(null);
    const [guion, setGuion] = useState<Guion | null>(null);
    const [loading, setLoading] = useState(true);
    const [enLlamada, setEnLlamada] = useState(false);
    const [showResultModal, setShowResultModal] = useState(false);
    const [tiempoInicio, setTiempoInicio] = useState<Date | null>(null);

    const idAgente = 10; // TODO: Get from auth context

    useEffect(() => {
        if (id && idContacto) {
            loadData();
        }
    }, [id, idContacto]);

    const loadData = async () => {
        try {
            setLoading(true);
            const contactos = await telemarketingApi.getContactosCampania(Number(id));
            const foundContacto = contactos.find(c => c.id === Number(idContacto));
            setContacto(foundContacto || null);

            const guionData = await telemarketingApi.getGuion(Number(id));
            setGuion(guionData);
        } catch (error) {
            console.error('Error cargando datos:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleIniciarLlamada = () => {
        setEnLlamada(true);
        setTiempoInicio(new Date());
    };

    const handleFinalizarLlamada = () => {
        setShowResultModal(true);
    };

    const handleSaveResult = async (data: ResultadoLlamadaRequest, abrirSiguiente: boolean) => {
        try {
            await telemarketingApi.registrarResultado(Number(id), idAgente, data);
            setShowResultModal(false);

            if (abrirSiguiente) {
                const siguiente = await telemarketingApi.getSiguienteContacto(Number(id), idAgente);
                if (siguiente) {
                    navigate(`/marketing/campanas/telefonicas/campanias/${id}/llamar/${siguiente.id}`);
                } else {
                    navigate(`/marketing/campanas/telefonicas/campanias/${id}/cola`);
                }
            } else {
                navigate(`/marketing/campanas/telefonicas/campanias/${id}/cola`);
            }
        } catch (error) {
            console.error('Error guardando resultado:', error);
        }
    };

    const getDuracionLlamada = () => {
        if (!tiempoInicio) return 0;
        return Math.floor((new Date().getTime() - tiempoInicio.getTime()) / 1000);
    };

    if (loading) {
        return (
            <div className="flex items-center justify-center h-screen">
                <span className="animate-spin h-12 w-12 border-4 border-primary border-t-transparent rounded-full"></span>
            </div>
        );
    }

    if (!contacto) {
        return (
            <div className="flex flex-col items-center justify-center h-screen">
                <h2 className="text-2xl font-bold text-gray-900 mb-4">Contacto no encontrado</h2>
                <Button variant="primary" onClick={() => navigate(`/marketing/campanas/telefonicas/campanias/${id}/cola`)}>
                    Volver a la cola
                </Button>
            </div>
        );
    }

    return (
        <div className="flex flex-col h-screen bg-gray-50">
            {/* Header */}
            <div className="bg-white border-b border-gray-200 p-6">
                <div className="flex justify-between items-start">
                    <div>
                        <h1 className="text-3xl font-bold text-gray-900">{contacto.nombreCompleto}</h1>
                        <p className="text-gray-500 mt-1">{contacto.empresa}</p>
                    </div>
                    <div className="flex gap-3">
                        {!enLlamada ? (
                            <Button variant="primary" icon="call" onClick={handleIniciarLlamada}>
                                Iniciar llamada
                            </Button>
                        ) : (
                            <Button variant="danger" icon="call_end" onClick={handleFinalizarLlamada}>
                                Finalizar llamada
                            </Button>
                        )}
                        <Button variant="secondary" onClick={() => navigate(`/marketing/campanas/telefonicas/campanias/${id}/cola`)}>
                            Volver a cola
                        </Button>
                    </div>
                </div>
            </div>

            <div className="flex flex-1 overflow-hidden">
                {/* Left Panel - Contact Info */}
                <div className="w-1/3 bg-white border-r border-gray-200 p-6 overflow-y-auto">
                    <h2 className="text-xl font-bold text-gray-900 mb-4">Información del contacto</h2>

                    <div className="space-y-4">
                        <div>
                            <label className="text-sm font-medium text-gray-600">Teléfono</label>
                            <p className="text-lg font-semibold text-gray-900">{contacto.telefono}</p>
                        </div>

                        <div>
                            <label className="text-sm font-medium text-gray-600">Email</label>
                            <p className="text-gray-900">{contacto.email}</p>
                        </div>

                        <div>
                            <label className="text-sm font-medium text-gray-600">Estado</label>
                            <p className="text-gray-900">{contacto.estadoCampania.replace('_', ' ')}</p>
                        </div>

                        <div>
                            <label className="text-sm font-medium text-gray-600">Prioridad</label>
                            <p className="text-gray-900">{contacto.prioridad}</p>
                        </div>

                        <div>
                            <label className="text-sm font-medium text-gray-600">Intentos previos</label>
                            <p className="text-gray-900">{contacto.numeroIntentos}</p>
                        </div>

                        {contacto.fechaUltimaLlamada && (
                            <div>
                                <label className="text-sm font-medium text-gray-600">Última llamada</label>
                                <p className="text-gray-900">
                                    {new Date(contacto.fechaUltimaLlamada).toLocaleString()}
                                </p>
                            </div>
                        )}

                        {contacto.notas && (
                            <div>
                                <label className="text-sm font-medium text-gray-600">Notas previas</label>
                                <p className="text-gray-700 text-sm mt-1 p-3 bg-gray-50 rounded-lg">
                                    {contacto.notas}
                                </p>
                            </div>
                        )}
                    </div>
                </div>

                {/* Right Panel - Script */}
                <div className="flex-1 p-6 overflow-y-auto">
                    <div className="max-w-3xl mx-auto">
                        <h2 className="text-2xl font-bold text-gray-900 mb-6">Guion de llamada</h2>

                        {guion ? (
                            <div className="space-y-6">
                                <div className="bg-white rounded-lg p-6 border border-gray-200">
                                    <h3 className="text-lg font-bold text-gray-900 mb-2">{guion.nombre}</h3>
                                    <p className="text-gray-600 mb-4">{guion.descripcion}</p>
                                    <div className="flex gap-2">
                                        <span className="px-3 py-1 bg-primary/10 text-primary text-sm rounded-full">{guion.tipo}</span>
                                        <span className="px-3 py-1 bg-gray-100 text-gray-700 text-sm rounded-full">
                                            {guion.objetivo}
                                        </span>
                                    </div>
                                </div>

                                {guion.pasos.map((paso, index) => (
                                    <div key={index} className="bg-white rounded-lg p-6 border border-gray-200">
                                        <div className="flex items-center gap-3 mb-3">
                                            <span className="flex items-center justify-center w-8 h-8 rounded-full bg-primary text-white font-bold">
                                                {paso.orden}
                                            </span>
                                            <h4 className="text-lg font-bold text-gray-900">{paso.titulo}</h4>
                                            <span className="ml-auto px-2 py-1 bg-gray-100 text-gray-600 text-xs rounded">
                                                {paso.tipo.replace('_', ' ')}
                                            </span>
                                        </div>
                                        <p className="text-gray-700 leading-relaxed whitespace-pre-wrap">
                                            {paso.contenido}
                                        </p>
                                        {paso.campoGuardado && (
                                            <p className="mt-3 text-sm text-gray-500 flex items-center gap-2">
                                                <span className="material-symbols-outlined text-primary text-base">save</span>
                                                Se guardará en: {paso.campoGuardado}
                                            </p>
                                        )}
                                    </div>
                                ))}
                            </div>
                        ) : (
                            <p className="text-gray-500">No hay guion disponible para esta campaña.</p>
                        )}
                    </div>
                </div>
            </div>

            <CallResultModal
                isOpen={showResultModal}
                onClose={() => setShowResultModal(false)}
                onSave={handleSaveResult}
                idContacto={contacto.id}
            />
        </div>
    );
};
