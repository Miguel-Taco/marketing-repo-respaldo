import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { telemarketingApi } from '../services/telemarketingApi';
import type { Contacto, CampaniaTelefonica, MetricasDiarias } from '../types';
import { Button } from '../../../../../shared/components/ui/Button';
import { useCampaignsContext } from '../context/CampaignsContext';
import { useCampaignAccessControl } from '../hooks/useCampaignAccessControl';
import { useAuth } from '../../../../../shared/context/AuthContext';

export const CallQueuePage: React.FC = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { autoNext, setAutoNext } = useCampaignsContext();
    const { isChecking: checkingAccess } = useCampaignAccessControl(id);
    const [cola, setCola] = useState<Contacto[]>([]);
    const [campanias, setCampanias] = useState<CampaniaTelefonica[]>([]);
    const [metricas, setMetricas] = useState<MetricasDiarias | null>(null);
    const [loading, setLoading] = useState(true);
    const [colaPausada, setColaPausada] = useState(false);
    const { user } = useAuth();
    const idAgente = user?.agentId;

    useEffect(() => {
        if (!checkingAccess) {
            loadCola();
            if (id) {
                loadMetricas();
            }
        }
    }, [id, checkingAccess]);

    const loadCola = async () => {
        try {
            setLoading(true);
            if (id) {
                const data = await telemarketingApi.getCola(Number(id));
                setCola(data);
            } else {
                if (!idAgente) {
                    setCampanias([]);
                    return;
                }
                const data = await telemarketingApi.getCampaniasAsignadas();
                setCampanias(data);
            }
        } catch (error) {
            console.error('Error cargando cola:', error);
        } finally {
            setLoading(false);
        }
    };

    const loadMetricas = async () => {
        try {
            if (!idAgente) {
                setMetricas(null);
                return;
            }
            const data = await telemarketingApi.getMetricasDiarias(Number(id));
            setMetricas(data);
        } catch (error) {
            console.error('Error cargando métricas:', error);
        }
    };

    const handleObtenerSiguiente = async () => {
        try {
            const contacto = await telemarketingApi.getSiguienteContacto(Number(id));
            if (contacto) {
                navigate(`/marketing/campanas/telefonicas/campanias/${id}/llamar/${contacto.id}`);
            }
        } catch (error) {
            console.error('Error obteniendo siguiente contacto:', error);
        }
    };

    const handleTomarContacto = async (idContacto: number) => {
        try {
            const contacto = await telemarketingApi.tomarContacto(Number(id), idContacto);
            if (contacto) {
                navigate(`/marketing/campanas/telefonicas/campanias/${id}/llamar/${contacto.id}`);
            }
        } catch (error) {
            console.error('Error tomando contacto:', error);
        }
    };

    const handlePausarCola = async () => {
        try {
            if (!id) return;
            await telemarketingApi.pausarCola(Number(id));
            setColaPausada(true);
        } catch (error) {
            console.error('Error pausando cola:', error);
        }
    };

    const handleReanudarCola = async () => {
        try {
            if (!id) return;
            await telemarketingApi.reanudarCola(Number(id));
            setColaPausada(false);
        } catch (error) {
            console.error('Error reanudando cola:', error);
        }
    };

    const getPrioridadBadge = (prioridad: string) => {
        const colors: Record<string, string> = {
            'ALTA': 'bg-red-100 text-red-600',
            'MEDIA': 'bg-yellow-100 text-yellow-700',
            'BAJA': 'bg-green-100 text-green-700'
        };
        return colors[prioridad] || 'bg-gray-200 text-gray-600';
    };

    if (!idAgente) {
        return (
            <div className="flex items-center justify-center h-full p-6">
                <div className="max-w-lg text-center space-y-4">
                    <h2 className="text-2xl font-bold text-gray-900">No tienes un agente asignado</h2>
                    <p className="text-gray-600">Solicita al administrador que te asigne campañas telefónicas para ver las colas disponibles.</p>
                    <Button variant="primary" onClick={() => navigate('/leads')}>Ir al panel principal</Button>
                </div>
            </div>
        );
    }

    if (!id) {
        return (
            <div className="flex flex-col h-full p-6">
                <div className="mb-6">
                    <h1 className="text-4xl font-black text-gray-900">Colas de Llamadas</h1>
                    <p className="text-gray-500 mt-2">Selecciona una campaña para gestionar su cola de llamadas.</p>
                </div>

                {loading || checkingAccess ? (
                    <div className="flex items-center justify-center h-64">
                        <span className="animate-spin h-8 w-8 border-4 border-primary border-t-transparent rounded-full"></span>
                    </div>
                ) : (
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                        {campanias.map(c => (
                            <div key={c.id} className="bg-white rounded-lg p-6 border border-gray-200 shadow-sm hover:shadow-md transition-shadow">
                                <div className="flex justify-between items-start mb-4">
                                    <h3 className="text-xl font-bold text-gray-900">{c.nombre}</h3>
                                    <span className={`px-2 py-1 rounded-full text-xs font-semibold ${c.estado === 'ACTIVA' ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'
                                        }`}>
                                        {c.estado}
                                    </span>
                                </div>
                                <div className="space-y-2 mb-6">
                                    <div className="flex justify-between text-sm">
                                        <span className="text-gray-500">Leads pendientes:</span>
                                        <span className="font-medium text-gray-900">{c.leadsPendientes}</span>
                                    </div>
                                    <div className="flex justify-between text-sm">
                                        <span className="text-gray-500">Prioridad:</span>
                                        <span className="font-medium text-gray-900">{c.prioridad}</span>
                                    </div>
                                </div>
                                <Button
                                    variant="primary"
                                    className="w-full justify-center"
                                    onClick={() => navigate(`/marketing/campanas/telefonicas/campanias/${c.id}/cola`)}
                                >
                                    Entrar a la cola
                                </Button>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        );
    }

    if (checkingAccess) {
        return (
            <div className="flex items-center justify-center h-screen">
                <span className="animate-spin h-8 w-8 border-4 border-primary border-t-transparent rounded-full"></span>
            </div>
        );
    }

    return (
        <div className="flex flex-col h-full p-6">
            <header className="flex flex-wrap justify-between items-start gap-4 mb-6">
                <div className="flex flex-col gap-3">
                    <h1 className="text-4xl font-black text-gray-900">Bandeja de llamadas</h1>
                    <p className="text-gray-500">Gestiona la cola de llamadas pendientes para esta campaña.</p>
                    <div className="flex gap-3">
                        <div className="flex h-8 items-center rounded-full bg-white px-4 border border-gray-200">
                            <p className="text-sm font-medium text-gray-800">Estado: Activa</p>
                        </div>
                        <div className="flex h-8 items-center rounded-full bg-white px-4 border border-gray-200">
                            <p className="text-sm font-medium text-gray-800">Prioridad: Alta</p>
                        </div>
                    </div>
                </div>
                <Button variant="primary" onClick={() => navigate(`/marketing/campanas/telefonicas/campanias/${id}/metricas`)}>
                    Ver métricas de campaña
                </Button>
            </header>

            {/* Stats Cards */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
                <div className="flex flex-col gap-2 rounded-lg p-6 bg-white border border-gray-200">
                    <p className="text-base font-medium text-gray-800">Pendientes</p>
                    <p className="text-3xl font-bold text-gray-900">{metricas?.pendientes ?? cola.length}</p>
                </div>
                <div className="flex flex-col gap-2 rounded-lg p-6 bg-white border border-gray-200">
                    <p className="text-base font-medium text-gray-800">Realizadas hoy</p>
                    <p className="text-3xl font-bold text-gray-900">{metricas?.realizadasHoy ?? 0}</p>
                </div>
                <div className="flex flex-col gap-2 rounded-lg p-6 bg-white border border-gray-200">
                    <p className="text-base font-medium text-gray-800">Efectivas</p>
                    <p className="text-3xl font-bold text-gray-900">{metricas?.efectivasHoy ?? 0}</p>
                </div>
            </div>

            {/* Auto-next toggle and actions */}
            <div className="p-5 bg-white rounded-lg border border-gray-200 mb-6">
                <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
                    <div className="flex flex-col gap-1">
                        <p className="text-base font-bold text-gray-900">Obtener siguiente contacto automáticamente</p>
                        <p className="text-sm text-gray-500">
                            Activa para que el sistema te asigne el siguiente lead al finalizar una llamada.
                        </p>
                    </div>
                    <label className="relative flex h-[31px] w-[51px] cursor-pointer items-center rounded-full border-none bg-gray-200 p-0.5">
                        <input
                            checked={autoNext}
                            className="sr-only peer"
                            type="checkbox"
                            onChange={(e) => setAutoNext(e.target.checked)}
                        />
                        <div className={`h-full w-[27px] rounded-full bg-white transition-transform ${autoNext ? 'translate-x-5' : ''}`}></div>
                    </label>
                </div>
                <div className="flex flex-wrap gap-3 mt-4 border-t border-gray-200 pt-4">
                    <Button variant="primary" onClick={handleObtenerSiguiente} className="flex-grow">
                        Obtener siguiente contacto
                    </Button>
                    <Button
                        variant="secondary"
                        onClick={colaPausada ? handleReanudarCola : handlePausarCola}
                        className="flex-grow"
                    >
                        {colaPausada ? 'Reanudar cola' : 'Pausar cola'}
                    </Button>
                </div>
            </div>

            {/* Queue Table */}
            <div className="bg-white rounded-lg border border-gray-200 overflow-hidden flex-1">
                {loading ? (
                    <div className="flex items-center justify-center h-64">
                        <span className="animate-spin h-8 w-8 border-4 border-primary border-t-transparent rounded-full"></span>
                    </div>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="w-full text-left">
                            <thead className="bg-gray-50">
                                <tr>
                                    <th className="p-4 text-sm font-semibold text-gray-600">Lead</th>
                                    <th className="p-4 text-sm font-semibold text-gray-600">Teléfono</th>
                                    <th className="p-4 text-sm font-semibold text-gray-600">Prioridad</th>
                                    <th className="p-4 text-sm font-semibold text-gray-600">Intentos</th>
                                    <th className="p-4 text-sm font-semibold text-gray-600">Última interacción</th>
                                    <th className="p-4 text-sm font-semibold text-gray-600">Acción</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-gray-200">
                                {cola.map((contacto) => (
                                    <tr key={contacto.id} className="hover:bg-gray-50">
                                        <td className="p-4 text-sm text-gray-800 font-medium">{contacto.nombreCompleto}</td>
                                        <td className="p-4 text-sm text-gray-500">{contacto.telefono}</td>
                                        <td className="p-4">
                                            <span className={`inline-flex items-center rounded-full px-3 py-1 text-xs font-medium ${getPrioridadBadge(contacto.prioridad)}`}>
                                                {contacto.prioridad}
                                            </span>
                                        </td>
                                        <td className="p-4 text-sm text-gray-500">{contacto.numeroIntentos}</td>
                                        <td className="p-4 text-sm text-gray-500">
                                            {contacto.fechaUltimaLlamada ? new Date(contacto.fechaUltimaLlamada).toLocaleString() : '-'}
                                        </td>
                                        <td className="p-4">
                                            <button
                                                className="h-8 px-3 rounded-full bg-primary text-white text-xs font-bold hover:bg-primary/90"
                                                onClick={() => handleTomarContacto(contacto.id)}
                                            >
                                                Tomar
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        </div>
    );
};
