import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { telemarketingApi } from '../services/telemarketingApi';
import type { Contacto } from '../types';
import { Button } from '../../../../../shared/components/ui/Button';
import { downloadCSV } from '../../../../../shared/utils/exportUtils';
import { useAuth } from '../../../../../shared/context/AuthContext';
import { LoadingSpinner } from '../../../../../shared/components/ui/LoadingSpinner';
import { LoadingDots } from '../../../../../shared/components/ui/LoadingDots';
import { useCachedCampaignData } from '../context/CampaignCacheContext';

export const CampaignLeadsPage: React.FC = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const [contactosGlobales, setContactosGlobales] = useState<Contacto[]>([]);
    const [loadingGlobal, setLoadingGlobal] = useState(false);
    const [searchTerm, setSearchTerm] = useState('');
    const [estadoFilter, setEstadoFilter] = useState<string>('');
    const { user, hasRole } = useAuth();
    const isAdmin = hasRole('ADMIN');
    const idAgente = user?.agentId;

    // Usar caché para leads de campaña específica
    const { data: contactosCampania = [], loading: loadingCampania } = useCachedCampaignData<Contacto[]>(
        id ? Number(id) : undefined,
        'leads'
    );

    // Determinar qué datos usar
    const contactos = id ? contactosCampania : contactosGlobales;
    const loading = id ? loadingCampania : loadingGlobal;

    useEffect(() => {
        // Solo cargar leads globales si no hay ID de campaña
        if (!id && idAgente) {
            loadContactosGlobales();
        }
    }, [id, idAgente]);

    const loadContactosGlobales = async () => {
        try {
            setLoadingGlobal(true);
            if (!idAgente) {
                setContactosGlobales([]);
                return;
            }
            const campanias = await telemarketingApi.getCampaniasAsignadas();
            const allContactsPromises = campanias.map(c =>
                telemarketingApi.getContactosCampania(c.id).then(contacts =>
                    contacts.map(contact => ({ ...contact, nombreCampania: c.nombre, idCampania: c.id }))
                )
            );
            const results = await Promise.all(allContactsPromises);
            setContactosGlobales(results.flat());
        } catch (error) {
            console.error('Error cargando contactos globales:', error);
        } finally {
            setLoadingGlobal(false);
        }
    };

    const handleExport = () => {
        const columns = [
            { key: 'nombreCompleto' as keyof Contacto, label: 'Nombre' },
            { key: 'telefono' as keyof Contacto, label: 'Teléfono' },
            { key: 'email' as keyof Contacto, label: 'Email' },
            { key: 'estadoCampania' as keyof Contacto, label: 'Estado' },
            { key: 'prioridad' as keyof Contacto, label: 'Prioridad' },
            ...(id ? [] : [{ key: 'nombreCampania' as keyof Contacto, label: 'Campaña' }])
        ];

        const filename = id ? `leads_campania_${id}` : 'mis_leads';
        downloadCSV(filteredContactos, filename, columns);
    };

    if (!id && !isAdmin && !idAgente) {
        return (
            <div className="flex flex-col items-center justify-center h-full p-10 text-center space-y-4">
                <h2 className="text-2xl font-bold text-gray-900">No tienes un agente asignado</h2>
                <p className="text-gray-600">Solicita acceso a campañas telefónicas para revisar tus leads.</p>
                <Button variant="primary" onClick={() => navigate('/leads')}>
                    Volver al panel
                </Button>
            </div>
        );
    }

    const filteredContactos = contactos.filter(c => {
        if (estadoFilter && c.estadoCampania !== estadoFilter) return false;
        if (searchTerm) {
            const term = searchTerm.toLowerCase();
            return c.nombreCompleto.toLowerCase().includes(term) ||
                c.telefono.includes(term) ||
                c.email?.toLowerCase().includes(term);
        }
        return true;
    });

    const getEstadoBadge = (estado: string) => {
        const colors: Record<string, string> = {
            'NO_CONTACTADO': 'bg-blue-100 text-blue-800',
            'EN_SEGUIMIENTO': 'bg-yellow-100 text-yellow-800',
            'CERRADO': 'bg-green-100 text-green-800',
            'NO_INTERESADO': 'bg-gray-200 text-gray-800'
        };
        return colors[estado] || 'bg-gray-200 text-gray-800';
    };

    const getPrioridadBadge = (prioridad: string) => {
        const colors: Record<string, string> = {
            'ALTA': 'bg-red-100 text-red-800',
            'MEDIA': 'bg-yellow-100 text-yellow-800',
            'BAJA': 'bg-green-100 text-green-800'
        };
        return colors[prioridad] || 'bg-gray-200 text-gray-800';
    };

    return (
        <div className="flex flex-col h-full">
            <div className="mb-4">
                <h1 className="text-2xl font-bold text-gray-900">
                    {id ? 'Lista de Leads' : 'Mis Leads (Todas las campañas)'}
                </h1>
            </div>

            {/* Filtros y búsqueda */}
            <div className="bg-white rounded-lg p-5 mb-4 shadow-sm border border-gray-200">
                <div className="mb-3">
                    <label className="flex flex-col min-w-40 h-10 w-full">
                        <div className="flex w-full flex-1 items-stretch rounded-full h-full">
                            <div className="text-gray-500 flex bg-gray-100 items-center justify-center pl-3 rounded-l-full">
                                <span className="material-symbols-outlined">search</span>
                            </div>
                            <input
                                className="form-input flex w-full min-w-0 flex-1 rounded-r-full border-none bg-gray-100 text-gray-900 h-full pl-2 text-base"
                                placeholder="Buscar por nombre, teléfono, email, empresa..."
                                value={searchTerm}
                                onChange={(e) => setSearchTerm(e.target.value)}
                            />
                        </div>
                    </label>
                </div>
                <div className="flex flex-wrap items-center gap-x-3 gap-y-2">
                    <div className="flex gap-2 flex-wrap items-center">
                        <button
                            className={`h-7 px-2.5 rounded-full text-xs font-semibold ${estadoFilter === 'NO_CONTACTADO'
                                ? 'border border-primary text-primary'
                                : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                                }`}
                            onClick={() => setEstadoFilter('NO_CONTACTADO')}
                        >
                            No contactado
                        </button>
                        <button
                            className={`h-7 px-2.5 rounded-full text-xs font-semibold ${estadoFilter === 'EN_SEGUIMIENTO'
                                ? 'border border-primary text-primary'
                                : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                                }`}
                            onClick={() => setEstadoFilter('EN_SEGUIMIENTO')}
                        >
                            En seguimiento
                        </button>
                        <button
                            className={`h-7 px-2.5 rounded-full text-xs font-semibold ${estadoFilter === 'CERRADO'
                                ? 'border border-primary text-primary'
                                : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                                }`}
                            onClick={() => setEstadoFilter('CERRADO')}
                        >
                            Cerrado
                        </button>
                        <button
                            className={`h-7 px-2.5 rounded-full text-xs font-semibold ${estadoFilter === 'NO_INTERESADO'
                                ? 'border border-primary text-primary'
                                : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                                }`}
                            onClick={() => setEstadoFilter('NO_INTERESADO')}
                        >
                            No interesado
                        </button>
                    </div>
                </div>
            </div>

            {/* Tabla de contactos */}
            <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden flex-1">
                <div className="p-6 flex justify-between items-center border-b border-gray-200">
                    <h3 className="text-lg font-bold text-gray-900">
                        Lista de Leads ({filteredContactos.length})
                    </h3>
                    <Button variant="secondary" icon="ios_share" onClick={handleExport}>
                        Exportar mis leads
                    </Button>
                </div>
                {loading ? (
                    <div className="flex flex-col items-center justify-center h-64 gap-4">
                        <LoadingSpinner size="lg" />
                        <LoadingDots text="Cargando lista de leads" className="text-gray-600 font-medium" />
                    </div>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="w-full text-sm text-left">
                            <thead className="bg-gray-50 text-xs text-gray-600 uppercase">
                                <tr>
                                    <th className="px-2 py-1 font-semibold">Nombre / Empresa</th>
                                    {!id && <th className="px-2 py-1 font-semibold">Campaña</th>}
                                    <th className="px-2 py-1 font-semibold">Teléfono</th>
                                    <th className="px-2 py-1 font-semibold">Email</th>
                                    <th className="px-2 py-1 font-semibold">Estado</th>
                                    <th className="px-2 py-1 font-semibold">Prioridad</th>
                                    <th className="px-2 py-1 font-semibold">Última llamada</th>
                                    <th className="px-2 py-1 font-semibold text-center">Intentos</th>
                                    <th className="px-2 py-1 font-semibold text-right">Acción</th>
                                </tr>
                            </thead>
                            <tbody>
                                {filteredContactos.map((contacto) => (
                                    <tr
                                        key={contacto.id}
                                        className="border-b border-gray-200 hover:bg-gray-50"
                                    >
                                        <td className="px-2 py-1.5 font-medium text-gray-900 whitespace-nowrap">
                                            {contacto.nombreCompleto}
                                            <br />
                                            <span className="text-gray-500 font-normal">{contacto.empresa}</span>
                                        </td>
                                        {!id && (
                                            <td className="px-2 py-1.5 text-gray-600 text-xs">
                                                {contacto.nombreCampania}
                                            </td>
                                        )}
                                        <td className="px-2 py-1.5 text-gray-600">{contacto.telefono}</td>
                                        <td className="px-2 py-1.5 text-gray-600">{contacto.email}</td>
                                        <td className="px-2 py-1.5">
                                            <span className={`px-2.5 py-0.5 rounded-full text-xs font-semibold ${getEstadoBadge(contacto.estadoCampania)}`}>
                                                {contacto.estadoCampania.replace('_', ' ')}
                                            </span>
                                        </td>
                                        <td className="px-2 py-1.5">
                                            <span className={`px-2.5 py-0.5 rounded-full text-xs font-semibold ${getPrioridadBadge(contacto.prioridad)}`}>
                                                {contacto.prioridad}
                                            </span>
                                        </td>
                                        <td className="px-2 py-1.5 text-gray-600">
                                            {contacto.fechaUltimaLlamada
                                                ? new Date(contacto.fechaUltimaLlamada).toLocaleString()
                                                : '-'
                                            }
                                        </td>
                                        <td className="px-2 py-1.5 text-gray-600 text-center">
                                            {contacto.numeroIntentos}
                                        </td>
                                        <td className="px-2 py-1.5 text-right">
                                            <button
                                                className="flex items-center justify-center gap-1.5 rounded-full h-7 px-3 bg-primary text-white text-xs font-bold hover:bg-primary/90"
                                                onClick={() => navigate(`/marketing/campanas/telefonicas/campanias/${id || contacto.idCampania}/llamar/${contacto.id}`)}
                                            >
                                                <span className="material-symbols-outlined text-base">call</span>
                                                <span>Llamar</span>
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
