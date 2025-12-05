import React, { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { Tabs } from '../../../../../shared/components/ui/Tabs';
import { Input } from '../../../../../shared/components/ui/Input';
import { Badge } from '../../../../../shared/components/ui/Badge';
import { LoadingSpinner } from '../../../../../shared/components/ui/LoadingSpinner';
import { useToast } from '../../../../../shared/components/ui/Toast';
import { CampanaMailing, ESTADO_COLORS, PRIORIDAD_COLORS, MetricasMailing } from '../types/mailing.types';
import { useAuth } from '../../../../../shared/context/AuthContext';
import { useMailing } from '../context/MailingContext';

interface CampanaConMetricas extends CampanaMailing {
    metricas?: MetricasMailing;
}

export const MailingListPage: React.FC = () => {
    const navigate = useNavigate();
    const { showToast } = useToast();
    const { user, hasRole } = useAuth();
    
    const { listarCampanas, metricsCache, obtenerMetricas } = useMailing();

    const [activeTab, setActiveTab] = useState('pendiente');
    const [campanas, setCampanas] = useState<CampanaConMetricas[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [searchTerm, setSearchTerm] = useState('');
    
    // ✅ KEY PARA FORZAR RE-RENDER LIMPIO AL CAMBIAR DE PESTAÑA
    const [tableKey, setTableKey] = useState('pendiente');

    const idAgente = user?.agentId;
    const isAdmin = hasRole('ADMIN');
    const canEditCampaigns = isAdmin;
    const assignmentKey = (user?.campaniasMailing || []).join(',');

    // ✅ SEPARAR LA LÓGICA DE CARGAR CAMPAÑAS
    const loadCampanas = useCallback(async () => {
        try {
            setLoading(true);
            setError(null);

            if (!isAdmin && !idAgente) {
                setError('No hay agente asignado para campañas de mailing');
                setCampanas([]);
                return;
            }

            // ✅ Obtener datos del context
            const data = await listarCampanas(activeTab);

            if (!isAdmin && (!user?.campaniasMailing || user.campaniasMailing.length === 0)) {
                setError('No tienes campañas de mailing asignadas. Solicita acceso al administrador.');
                setCampanas([]);
                return;
            }

            // ✅ Filtrar solo campañas asignadas
            const visibleCampanas = !isAdmin && user?.campaniasMailing?.length
                ? (data || []).filter(c => user.campaniasMailing?.includes(c.id))
                : (data || []);

            // ✅ CRITICAL: Establecer campanas Y tableKey al mismo tiempo
            setCampanas(visibleCampanas);
            setTableKey(activeTab); // Esto fuerza re-render limpio de la tabla

            // Cargar métricas para campañas en estado ENVIADO o FINALIZADO
            if (['enviado', 'finalizado'].includes(activeTab)) {
                await cargarMetricas(visibleCampanas);
            }
        } catch (error: any) {
            const errorMsg = error.message || 'Error al cargar campañas';
            setError(errorMsg);
            showToast(errorMsg, 'error');
            setCampanas([]);
        } finally {
            setLoading(false);
        }
    }, [activeTab, isAdmin, idAgente, user?.campaniasMailing, listarCampanas, showToast]);

    // ✅ EFECTO QUE SE EJECUTA CUANDO CAMBIA activeTab
    useEffect(() => {
        loadCampanas();
    }, [activeTab, loadCampanas]);

    const cargarMetricas = async (campanasData: CampanaMailing[]) => {
        for (const campana of campanasData) {
            try {
                await obtenerMetricas(campana.id);
            } catch (err) {
                console.error(`Error cargando métricas para campaña ${campana.id}:`, err);
            }
        }

        // ✅ Actualizar campanas con métricas después de cargar todas
        setCampanas(prev => prev.map(c => ({
            ...c,
            metricas: metricsCache.get(c.id)
        })));
    };

    const filteredCampanas = campanas.filter(c =>
        c.nombre.toLowerCase().includes(searchTerm.toLowerCase()) ||
        c.descripcion.toLowerCase().includes(searchTerm.toLowerCase())
    );

    const handleEdit = (id: number) => {
        navigate(`/emailing/${id}/edit`);
    };

    const handleViewMetrics = (id: number) => {
        navigate(`/emailing/${id}/metricas`);
    };

    const mostrarMetricas = ['enviado', 'finalizado'].includes(activeTab);

    return (
        <div className="space-y-6">
            {/* Header */}
            <header className="flex justify-between items-center">
                <div>
                    <h1 className="text-3xl font-bold text-dark">Campañas de Mailing</h1>
                </div>
            </header>

            {/* Tabs */}
            <Tabs
                items={[
                    { label: 'Pendientes', value: 'pendiente' },
                    { label: 'Listos', value: 'listo' },
                    { label: 'Enviados', value: 'enviado' },
                    { label: 'Finalizados', value: 'finalizado' }
                ]}
                activeValue={activeTab}
                onChange={setActiveTab}
            />

            {/* Search Bar */}
            <div className="bg-white p-4 rounded-lg shadow-sm border border-gray-100 flex items-center justify-between gap-4">
                <div className="flex-1 relative">
                    <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none">
                        search
                    </span>
                    <Input
                        placeholder="Buscar por nombre de campaña..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        className="w-full pl-10"
                    />
                </div>
                <button className="flex items-center gap-2 px-4 py-2 border border-separator rounded-lg hover:bg-gray-50 transition-colors">
                    <span className="material-symbols-outlined text-gray-600">tune</span>
                    <span className="text-sm font-medium text-gray-600">Filtros</span>
                </button>
            </div>

            {/* Tabla - ✅ KEY FUERZA RE-RENDER LIMPIO */}
            <div 
                key={tableKey}
                className="bg-white rounded-lg shadow-sm border border-separator overflow-hidden"
            >
                {error ? (
                    <div className="p-8">
                        <div className="bg-red-50 border border-red-200 rounded-lg p-6">
                            <div className="flex items-start gap-3">
                                <span className="material-symbols-outlined text-red-600 text-2xl">error</span>
                                <div className="flex-1">
                                    <p className="text-red-700 font-bold text-lg">Error al cargar campañas</p>
                                    <p className="text-red-600 mt-2">{error}</p>
                                    <button
                                        onClick={loadCampanas}
                                        className="mt-4 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors font-medium"
                                    >
                                        Reintentar
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="w-full text-left border-collapse">
                            <thead className="bg-table-header sticky top-0">
                                <tr>
                                    <th className="p-4 text-sm font-semibold text-dark tracking-wide w-24">PRIORIDAD</th>
                                    <th className="p-4 text-sm font-semibold text-dark tracking-wide">NOMBRE CAMPAÑA</th>
                                    <th className="p-4 text-sm font-semibold text-dark tracking-wide">DESCRIPCIÓN</th>
                                    <th className="p-4 text-sm font-semibold text-dark tracking-wide w-32">FECHA DE INICIO</th>

                                    {mostrarMetricas && (
                                        <>
                                            <th className="p-4 text-sm font-semibold text-dark tracking-wide w-28 text-center">TASA APERTURA</th>
                                            <th className="p-4 text-sm font-semibold text-dark tracking-wide w-28 text-center">TASA CLICS</th>
                                            {activeTab === 'finalizado' && (
                                                <th className="p-4 text-sm font-semibold text-dark tracking-wide w-20 text-center">BAJAS</th>
                                            )}
                                        </>
                                    )}

                                    <th className="p-4 text-sm font-semibold text-dark tracking-wide w-16">ACCIONES</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-separator">
                                {loading ? (
                                    <tr>
                                        <td colSpan={mostrarMetricas ? 8 : 5} className="p-12 text-center">
                                            <div className="flex flex-col items-center justify-center">
                                                <LoadingSpinner size="lg" />
                                                <p className="text-gray-500 mt-4">Cargando campañas...</p>
                                            </div>
                                        </td>
                                    </tr>
                                ) : filteredCampanas.length === 0 ? (
                                    <tr>
                                        <td colSpan={mostrarMetricas ? 8 : 5} className="p-12 text-center">
                                            <div className="flex flex-col items-center justify-center">
                                                <span className="material-symbols-outlined text-6xl text-gray-300 mb-4">inbox</span>
                                                <p className="text-gray-500 text-lg font-medium">No se encontraron campañas</p>
                                                <p className="text-gray-400 text-sm mt-2">
                                                    {searchTerm ? 'Intenta con otro término de búsqueda' : 'Aún no hay campañas en este estado'}
                                                </p>
                                            </div>
                                        </td>
                                    </tr>
                                ) : (
                                    filteredCampanas.map((campana) => (
                                        <tr key={`${activeTab}-${campana.id}`} className="hover:bg-gray-50 transition-colors">
                                            {/* Prioridad */}
                                            <td className="p-4">
                                                <span className={`px-2 py-1 rounded text-xs font-bold whitespace-nowrap ${PRIORIDAD_COLORS[campana.prioridad]}`}>
                                                    {campana.prioridad}
                                                </span>
                                            </td>

                                            {/* Nombre */}
                                            <td className="p-4">
                                                <span className="text-sm font-medium text-gray-900 truncate">
                                                    {campana.nombre}
                                                </span>
                                            </td>

                                            {/* Descripción */}
                                            <td className="p-4 text-sm text-gray-600 max-w-md">
                                                <p className="line-clamp-2">{campana.descripcion}</p>
                                            </td>

                                            {/* Fecha */}
                                            <td className="p-4 text-sm text-gray-600 whitespace-nowrap">
                                                {new Date(campana.fechaInicio).toLocaleDateString('es-ES', {
                                                    year: 'numeric',
                                                    month: '2-digit',
                                                    day: '2-digit'
                                                })}
                                            </td>

                                            {/* Columnas Dinámicas */}
                                            {mostrarMetricas && campana.metricas && (
                                                <>
                                                    <td className="p-4 text-sm text-gray-900 font-medium text-center">
                                                        {campana.metricas.tasaApertura.toFixed(1)}%
                                                    </td>

                                                    <td className="p-4 text-sm text-gray-900 font-medium text-center">
                                                        {campana.metricas.tasaClics.toFixed(1)}%
                                                    </td>

                                                    {activeTab === 'finalizado' && (
                                                        <td className="p-4 text-sm text-gray-900 font-medium text-center">
                                                            {campana.metricas.bajas}
                                                        </td>
                                                    )}
                                                </>
                                            )}

                                            {/* Acciones */}
                                            <td className="p-4 text-center">
                                                <div className="flex gap-2 justify-center">
                                                    {canEditCampaigns && ['pendiente', 'listo'].includes(activeTab) && (
                                                        <button
                                                            onClick={() => handleEdit(campana.id)}
                                                            className="p-2 text-primary hover:bg-blue-50 rounded-lg transition-colors inline-block"
                                                            title="Editar campaña"
                                                        >
                                                            <span className="material-symbols-outlined text-xl">edit</span>
                                                        </button>
                                                    )}

                                                    {mostrarMetricas && (
                                                        <button
                                                            onClick={() => handleViewMetrics(campana.id)}
                                                            className="p-2 text-primary hover:bg-blue-50 rounded-lg transition-colors inline-block"
                                                            title="Ver métricas detalladas"
                                                        >
                                                            <span className="material-symbols-outlined text-xl">bar_chart</span>
                                                        </button>
                                                    )}
                                                </div>
                                            </td>
                                        </tr>
                                    ))
                                )}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        </div>
    );
};