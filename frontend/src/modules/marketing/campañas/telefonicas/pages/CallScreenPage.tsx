import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { telemarketingApi } from '../services/telemarketingApi';
import type { Contacto, Guion, ResultadoLlamadaRequest } from '../types';
import { CallResultModal } from '../components/CallResultModal';
import { Button } from '../../../../../shared/components/ui/Button';
import { generateMarkdownFromScript } from '../utils/markdownGenerator';
import { MarkdownViewer } from '../components/MarkdownViewer';
import { useCampaignsContext } from '../context/CampaignsContext';

export const CallScreenPage: React.FC = () => {
    const { id, idContacto } = useParams<{ id: string; idContacto: string }>();
    const navigate = useNavigate();
    const { autoNext } = useCampaignsContext();
    const [contacto, setContacto] = useState<Contacto | null>(null);
    const [guion, setGuion] = useState<Guion | null>(null);
    const [loading, setLoading] = useState(true);
    const [enLlamada, setEnLlamada] = useState(false);
    const [showResultModal, setShowResultModal] = useState(false);
    const [tiempoInicio, setTiempoInicio] = useState<Date | null>(null);
    const [tiempoTranscurrido, setTiempoTranscurrido] = useState(0);

    const idAgente = 1; // TODO: Get from auth context (using existing agent ID from database)

    useEffect(() => {
        if (id && idContacto) {
            loadData();
        }
    }, [id, idContacto]);

    // Timer effect - updates every second during call
    useEffect(() => {
        let interval: number;
        if (enLlamada && tiempoInicio) {
            interval = window.setInterval(() => {
                setTiempoTranscurrido(Math.floor((new Date().getTime() - tiempoInicio.getTime()) / 1000));
            }, 1000);
        } else {
            setTiempoTranscurrido(0);
        }
        return () => window.clearInterval(interval);
    }, [enLlamada, tiempoInicio]);

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

    const formatTime = (seconds: number): string => {
        const mins = Math.floor(seconds / 60);
        const secs = seconds % 60;
        return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    };

    const handleSaveResult = async (data: ResultadoLlamadaRequest, abrirSiguiente: boolean) => {
        try {
            const requestCompleto: ResultadoLlamadaRequest = {
                ...data,
                inicio: tiempoInicio!,
                fin: new Date(),
                duracionSegundos: getDuracionLlamada(),
                idLead: contacto?.idLead || data.idLead,
                idContactoCola: contacto?.id
            };

            await telemarketingApi.registrarResultado(Number(id), idAgente, requestCompleto);
            setShowResultModal(false);
            setEnLlamada(false);
            setTiempoInicio(null);

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
            alert('Error al guardar el resultado. Por favor, intente nuevamente.');
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
                    <div className="flex items-center gap-3">
                        {/* Timer - visible during call */}
                        {enLlamada && (
                            <div className="flex items-center gap-2 px-4 py-2 bg-red-50 border-2 border-red-200 rounded-lg">
                                <span className="material-symbols-outlined text-red-600 animate-pulse">schedule</span>
                                <div className="text-center">
                                    <p className="text-xs font-medium text-red-700">Tiempo transcurrido</p>
                                    <p className="text-2xl font-bold text-red-600 tabular-nums">
                                        {formatTime(tiempoTranscurrido)}
                                    </p>
                                </div>
                            </div>
                        )}
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
                <div className="flex-1 p-6 overflow-y-auto bg-white">
                    <div className="max-w-3xl mx-auto">
                        <h2 className="text-2xl font-bold text-gray-900 mb-6">Guion de llamada</h2>

                        {guion ? (
                            <div className="space-y-6">
                                {/* Metadata Section - Same as ViewScriptModal */}
                                <div className="space-y-4">
                                    <div className="flex items-start gap-3 p-4 bg-blue-50 rounded-lg">
                                        <span className="material-symbols-outlined text-blue-600 mt-0.5">flag</span>
                                        <div className="flex-1">
                                            <h3 className="text-sm font-semibold text-blue-900 mb-1">
                                                Objetivo
                                            </h3>
                                            <p className="text-sm text-blue-800">
                                                {guion.objetivo}
                                            </p>
                                        </div>
                                    </div>

                                    {guion.descripcion && (
                                        <div className="flex items-start gap-3 p-4 bg-amber-50 rounded-lg">
                                            <span className="material-symbols-outlined text-amber-600 mt-0.5">sticky_note_2</span>
                                            <div className="flex-1">
                                                <h3 className="text-sm font-semibold text-amber-900 mb-1">
                                                    Descripción
                                                </h3>
                                                <p className="text-sm text-amber-800">
                                                    {guion.descripcion}
                                                </p>
                                            </div>
                                        </div>
                                    )}

                                    <div className="flex items-center gap-2 text-sm text-gray-600">
                                        <span className="material-symbols-outlined text-base">label</span>
                                        <span>
                                            {guion.pasos?.length || 0} paso{guion.pasos?.length !== 1 ? 's' : ''}
                                        </span>
                                    </div>
                                </div>

                                {/* Divider */}
                                <div className="border-t border-gray-200 my-6" />

                                {/* Script Content with Markdown */}
                                <div className="bg-gray-50 rounded-lg p-6">
                                    <div className="prose prose-lg max-w-none prose-headings:text-gray-900 prose-p:text-gray-700 prose-strong:text-gray-900">
                                        {guion.pasos?.map((paso, index) => (
                                            <div key={index} className="mb-6">
                                                <h3 className="text-lg font-bold text-gray-900 mb-2">
                                                    {paso.orden}. {paso.tipoSeccion || 'Paso'}
                                                </h3>
                                                <div
                                                    className="text-gray-700 leading-relaxed"
                                                    dangerouslySetInnerHTML={{ __html: paso.contenido }}
                                                />
                                            </div>
                                        ))}
                                    </div>
                                </div>
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
                duracionSegundos={tiempoTranscurrido}
                autoNext={autoNext}
            />
        </div>
    );
};
