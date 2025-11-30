import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Tabs } from '../../../../../shared/components/ui/Tabs';
import { Input } from '../../../../../shared/components/ui/Input';
import { Badge } from '../../../../../shared/components/ui/Badge';
import { LoadingSpinner } from '../../../../../shared/components/ui/LoadingSpinner';
import { useToast } from '../../../../../shared/components/ui/Toast';
import { mailingApi } from '../services/mailing.api';
import { CampanaMailing, ESTADO_COLORS, PRIORIDAD_COLORS } from '../types/mailing.types';

export const MailingListPage: React.FC = () => {
    const navigate = useNavigate();
    const { showToast } = useToast();

    const [activeTab, setActiveTab] = useState('pendiente');
    const [campanas, setCampanas] = useState<CampanaMailing[]>([]);
    const [loading, setLoading] = useState(false); // ✅ CORREGIDO: Inicia en false
    const [error, setError] = useState<string | null>(null);
    const [searchTerm, setSearchTerm] = useState('');

    const idAgente = 1; // TODO: Obtener del contexto de autenticación

    useEffect(() => {
        loadCampanas();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [activeTab]);

    const loadCampanas = async () => {
        try {
            setLoading(true);
            setError(null);
            console.log('Cargando campañas de mailing para agente:', idAgente, 'estado:', activeTab);
            const data = await mailingApi.listarCampanas(idAgente, activeTab);
            console.log('Campañas cargadas:', data);
            setCampanas(data || []);
        } catch (error) {
            console.error('Error cargando campañas:', error);
            
            let errorMessage = 'Error al cargar campañas';
            
            if (error instanceof Error) {
                errorMessage = error.message;
            } else if (typeof error === 'object' && error !== null) {
                const errObj = error as any;
                if (errObj.message) {
                    errorMessage = errObj.message;
                } else if (errObj.status) {
                    errorMessage = `Error ${errObj.status}: No se puede conectar con el servidor`;
                }
            }
            
            setError(errorMessage);
            showToast(errorMessage, 'error');
            setCampanas([]);
        } finally {
            setLoading(false);
        }
    };

    const filteredCampanas = campanas.filter(c =>
        c.nombre.toLowerCase().includes(searchTerm.toLowerCase()) ||
        c.descripcion.toLowerCase().includes(searchTerm.toLowerCase())
    );

    const handleEdit = (id: number) => {
        navigate(`/marketing/campanas/mailing/${id}/edit`);
    };

    const totalCampanas = campanas.length;
    const campanasActivas = campanas.filter(c => c.idEstado === 3).length;
    const tasaPromedioApertura = 0; // TODO: Calcular desde métricas

    return (
        <div className="space-y-6">
            {/* ✅ Header siempre visible */}
            <header className="flex justify-between items-center">
                <div>
                    <h1 className="text-3xl font-bold text-dark">Campañas de Mailing</h1>
                </div>
            </header>

            {/* ✅ Tabs siempre visibles */}
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

            {/* ✅ Barra de búsqueda siempre visible */}
            <div className="bg-white p-4 rounded-lg shadow-sm border border-gray-100 flex items-center justify-between gap-4">
                <div className="flex-1 relative">
                    <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none">
                        search
                    </span>
                    <Input
                        placeholder="Search campaigns..."
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

            {/* ✅ Tabla con estados de carga/error internos */}
            <div className="bg-white rounded-lg shadow-sm border border-separator overflow-hidden">
                {error ? (
                    // Estado de error
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
                    // Tabla normal
                    <div className="overflow-x-auto">
                        <table className="w-full text-left border-collapse">
                            <thead className="bg-table-header">
                                <tr>
                                    <th className="p-4 text-sm font-semibold text-dark tracking-wide">PRIORIDAD</th>
                                    <th className="p-4 text-sm font-semibold text-dark tracking-wide">NOMBRE CAMPAÑA</th>
                                    <th className="p-4 text-sm font-semibold text-dark tracking-wide">DESCRIPCIÓN</th>
                                    <th className="p-4 text-sm font-semibold text-dark tracking-wide">FECHA DE INICIO</th>
                                    <th className="p-4 text-sm font-semibold text-dark tracking-wide">ACCIONES</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-separator">
                                {loading ? (
                                    // ✅ Loading solo en la tabla
                                    <tr>
                                        <td colSpan={5} className="p-12 text-center">
                                            <div className="flex flex-col items-center justify-center">
                                                <LoadingSpinner size="lg" />
                                                <p className="text-gray-500 mt-4">Cargando campañas...</p>
                                            </div>
                                        </td>
                                    </tr>
                                ) : filteredCampanas.length === 0 ? (
                                    // Estado vacío
                                    <tr>
                                        <td colSpan={5} className="p-12 text-center">
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
                                    // Datos de la tabla
                                    filteredCampanas.map((campana) => (
                                        <tr key={campana.id} className="hover:bg-gray-50 transition-colors">
                                            <td className="p-4">
                                                <span className={`px-3 py-1 rounded-full text-xs font-bold ${PRIORIDAD_COLORS[campana.prioridad]}`}>
                                                    {campana.prioridad}
                                                </span>
                                            </td>
                                            <td className="p-4">
                                                <div className="flex flex-col gap-2">
                                                    <span className="text-sm font-medium text-gray-900">
                                                        {campana.nombre}
                                                    </span>
                                                    <Badge variant={ESTADO_COLORS[campana.idEstado]}>
                                                        {campana.estadoNombre}
                                                    </Badge>
                                                </div>
                                            </td>
                                            <td className="p-4 text-sm text-gray-600 max-w-md">
                                                <p className="line-clamp-2">{campana.descripcion}</p>
                                            </td>
                                            <td className="p-4 text-sm text-gray-600">
                                                {new Date(campana.fechaInicio).toLocaleDateString('es-ES', {
                                                    year: 'numeric',
                                                    month: '2-digit',
                                                    day: '2-digit'
                                                })}
                                            </td>
                                            <td className="p-4">
                                                <button
                                                    onClick={() => handleEdit(campana.id)}
                                                    className="p-2 text-primary hover:bg-blue-50 rounded-lg transition-colors"
                                                    title="Editar campaña"
                                                >
                                                    <span className="material-symbols-outlined text-xl">edit</span>
                                                </button>
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