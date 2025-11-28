import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { telemarketingApi } from '../services/telemarketingApi';
import type { Llamada } from '../types';
import { Button } from '../../../../../shared/components/ui/Button';
import { downloadCSV } from '../../../../../shared/utils/exportUtils';

export const CallHistoryPage: React.FC = () => {
    const { id } = useParams<{ id: string }>();
    const [llamadas, setLlamadas] = useState<Llamada[]>([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');
    const [resultadoFilter, setResultadoFilter] = useState<string>('Todos');
    const [selectedLlamada, setSelectedLlamada] = useState<Llamada | null>(null);

    const idAgente = 10; // TODO: Get from auth context

    useEffect(() => {
        loadHistorial();
    }, [id]);

    const loadHistorial = async () => {
        try {
            setLoading(true);
            if (id) {
                const data = await telemarketingApi.getHistorialLlamadas(Number(id), idAgente);
                setLlamadas(data);
                if (data.length > 0) {
                    setSelectedLlamada(data[0]);
                }
            } else {
                // Global view
                const campanias = await telemarketingApi.getCampaniasAgente(idAgente);
                const allCallsPromises = campanias.map(c =>
                    telemarketingApi.getHistorialLlamadas(c.id, idAgente).then(calls =>
                        calls.map(call => ({ ...call, nombreCampania: c.nombre }))
                    )
                );
                const results = await Promise.all(allCallsPromises);
                const allCalls = results.flat().sort((a, b) => new Date(b.fechaHora).getTime() - new Date(a.fechaHora).getTime());
                setLlamadas(allCalls);
                if (allCalls.length > 0) {
                    setSelectedLlamada(allCalls[0]);
                }
            }
        } catch (error) {
            console.error('Error cargando historial:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleExport = () => {
        const columns = [
            { key: 'fechaHora' as keyof Llamada, label: 'Fecha/Hora' },
            ...(id ? [] : [{ key: 'nombreCampania' as keyof Llamada, label: 'Campaña' }]),
            { key: 'nombreContacto' as keyof Llamada, label: 'Lead' },
            { key: 'telefonoContacto' as keyof Llamada, label: 'Teléfono' },
            { key: 'resultado' as keyof Llamada, label: 'Resultado' },
            { key: 'duracion' as keyof Llamada, label: 'Duración (seg)' },
            { key: 'notas' as keyof Llamada, label: 'Notas' }
        ];

        const filename = id ? `historial_campania_${id}` : 'historial_global';
        downloadCSV(filteredLlamadas, filename, columns);
    };

    const filteredLlamadas = llamadas.filter(l => {
        if (resultadoFilter !== 'Todos' && l.resultado !== resultadoFilter) return false;
        if (searchTerm) {
            const term = searchTerm.toLowerCase();
            return l.nombreContacto?.toLowerCase().includes(term) ||
                l.telefonoContacto?.includes(term);
        }
        return true;
    });

    const getResultadoBadge = (resultado: string) => {
        const colors: Record<string, string> = {
            'VENTA': 'bg-green-100 text-green-800',
            'CONTACTADO': 'bg-green-100 text-green-800',
            'INTERESADO': 'bg-blue-100 text-blue-800',
            'NO_CONTESTA': 'bg-gray-200 text-gray-800',
            'BUZON': 'bg-yellow-100 text-yellow-800',
            'NO_INTERESADO': 'bg-red-100 text-red-800'
        };
        return colors[resultado] || 'bg-gray-200 text-gray-800';
    };

    const formatDuration = (seconds: number) => {
        const mins = Math.floor(seconds / 60);
        const secs = seconds % 60;
        return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    };

    return (
        <div className="flex flex-col h-full p-6">
            <div className="flex flex-wrap items-center justify-between gap-4 mb-6">
                <div className="flex flex-col gap-1">
                    <h1 className="text-4xl font-black text-gray-900">
                        {id ? 'Historial de llamadas' : 'Historial Global'}
                    </h1>
                    <p className="text-gray-500">
                        {id ? 'Selecciona una campaña y un rango de fechas para ver el historial.' : 'Historial de llamadas de todas las campañas asignadas.'}
                    </p>
                </div>
                <Button variant="primary" icon="download" onClick={handleExport}>
                    Exportar historial
                </Button>
            </div>

            {/* Filters */}
            <div className="bg-white rounded-lg p-4 sm:p-6 mb-6 border border-gray-200">
                <div className="grid grid-cols-1 md:grid-cols-4 gap-4 items-end mb-4">
                    <label className="flex flex-col col-span-1 md:col-span-4">
                        <div className="flex w-full items-stretch rounded-xl h-12">
                            <div className="text-gray-500 flex bg-gray-100 items-center justify-center pl-4 rounded-l-xl">
                                <span className="material-symbols-outlined">search</span>
                            </div>
                            <input
                                className="form-input flex w-full rounded-r-xl border-none bg-gray-100 text-gray-900 h-full pl-2 text-base"
                                placeholder="Buscar por nombre, teléfono o ID del lead..."
                                value={searchTerm}
                                onChange={(e) => setSearchTerm(e.target.value)}
                            />
                        </div>
                    </label>
                </div>

                <div className="flex gap-3 flex-wrap">
                    <button
                        className={`h-8 px-4 rounded-full text-sm font-medium ${resultadoFilter === 'Todos'
                            ? 'bg-primary/20 text-primary'
                            : 'bg-gray-100 text-gray-700'
                            }`}
                        onClick={() => setResultadoFilter('Todos')}
                    >
                        Todos
                    </button>
                    <button
                        className={`h-8 px-4 rounded-full text-sm font-medium ${resultadoFilter === 'VENTA'
                            ? 'bg-primary/20 text-primary'
                            : 'bg-gray-100 text-gray-700'
                            }`}
                        onClick={() => setResultadoFilter('VENTA')}
                    >
                        Venta
                    </button>
                    <button
                        className={`h-8 px-4 rounded-full text-sm font-medium ${resultadoFilter === 'NO_CONTESTA'
                            ? 'bg-primary/20 text-primary'
                            : 'bg-gray-100 text-gray-700'
                            }`}
                        onClick={() => setResultadoFilter('NO_CONTESTA')}
                    >
                        No contesta
                    </button>
                    <button
                        className={`h-8 px-4 rounded-full text-sm font-medium ${resultadoFilter === 'BUZON'
                            ? 'bg-primary/20 text-primary'
                            : 'bg-gray-100 text-gray-700'
                            }`}
                        onClick={() => setResultadoFilter('BUZON')}
                    >
                        Buzón
                    </button>
                </div>
            </div>

            <div className="flex flex-1 gap-6 overflow-hidden">
                {/* Tabla de llamadas */}
                <div className="flex flex-col flex-1 bg-white rounded-lg border border-gray-200 overflow-hidden">
                    {loading ? (
                        <div className="flex items-center justify-center h-64">
                            <span className="animate-spin h-8 w-8 border-4 border-primary border-t-transparent rounded-full"></span>
                        </div>
                    ) : (
                        <div className="overflow-x-auto">
                            <table className="w-full">
                                <thead>
                                    <tr className="bg-gray-50">
                                        <th className="px-6 py-4 text-left text-xs font-medium text-gray-600 uppercase">Fecha/hora</th>
                                        {!id && <th className="px-6 py-4 text-left text-xs font-medium text-gray-600 uppercase">Campaña</th>}
                                        <th className="px-6 py-4 text-left text-xs font-medium text-gray-600 uppercase">Lead</th>
                                        <th className="px-6 py-4 text-left text-xs font-medium text-gray-600 uppercase">Teléfono</th>
                                        <th className="px-6 py-4 text-left text-xs font-medium text-gray-600 uppercase">Resultado</th>
                                        <th className="px-6 py-4 text-left text-xs font-medium text-gray-600 uppercase">Duración</th>
                                        <th className="px-6 py-4 text-left text-xs font-medium text-gray-600 uppercase">Acción</th>
                                    </tr>
                                </thead>
                                <tbody className="divide-y divide-gray-200">
                                    {filteredLlamadas.map((llamada) => (
                                        <tr
                                            key={llamada.id}
                                            className={`hover:bg-gray-50 cursor-pointer ${selectedLlamada?.id === llamada.id ? 'bg-primary/5' : ''
                                                }`}
                                            onClick={() => setSelectedLlamada(llamada)}
                                        >
                                            <td className="px-6 py-4 text-sm text-gray-600">
                                                {new Date(llamada.fechaHora).toLocaleString()}
                                            </td>
                                            {!id && (
                                                <td className="px-6 py-4 text-sm text-gray-600">
                                                    {llamada.nombreCampania}
                                                </td>
                                            )}
                                            <td className="px-6 py-4 text-sm font-medium text-gray-900">
                                                {llamada.nombreContacto || 'N/A'}
                                            </td>
                                            <td className="px-6 py-4 text-sm text-gray-600">
                                                {llamada.telefonoContacto || 'N/A'}
                                            </td>
                                            <td className="px-6 py-4">
                                                <span className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-medium ${getResultadoBadge(llamada.resultado)}`}>
                                                    {llamada.resultado.replace('_', ' ')}
                                                </span>
                                            </td>
                                            <td className="px-6 py-4 text-sm text-gray-600">
                                                {formatDuration(llamada.duracionSegundos)}
                                            </td>
                                            <td className="px-6 py-4 text-sm font-medium">
                                                <button className="text-primary hover:text-primary/80">Ver detalle</button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </div>

                {/* Panel de detalle */}
                {selectedLlamada && (
                    <div className="w-96 bg-white rounded-lg shadow-xl border border-gray-200 flex flex-col overflow-hidden">
                        <div className="flex items-center justify-between p-6 border-b border-gray-200">
                            <div>
                                <h2 className="text-lg font-bold text-gray-900">Detalle de llamada</h2>
                                <p className="text-sm text-gray-600">
                                    {new Date(selectedLlamada.fechaHora).toLocaleString()}
                                </p>
                            </div>
                            <button
                                className="text-gray-500 hover:text-gray-800"
                                onClick={() => setSelectedLlamada(null)}
                            >
                                <span className="material-symbols-outlined">close</span>
                            </button>
                        </div>

                        <div className="flex-1 overflow-y-auto p-6 space-y-6">
                            <div className="flex items-center">
                                <span className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-medium ${getResultadoBadge(selectedLlamada.resultado)}`}>
                                    {selectedLlamada.resultado.replace('_', ' ')}
                                </span>
                            </div>

                            <div>
                                <h3 className="text-base font-semibold text-gray-900 mb-3">Datos de lead</h3>
                                <div className="space-y-2 text-sm">
                                    <div className="flex justify-between">
                                        <span className="text-gray-600">Nombre:</span>
                                        <span className="font-medium text-gray-900">{selectedLlamada.nombreContacto}</span>
                                    </div>
                                    <div className="flex justify-between">
                                        <span className="text-gray-600">Teléfono:</span>
                                        <span className="font-medium text-gray-900">{selectedLlamada.telefonoContacto}</span>
                                    </div>
                                    <div className="flex justify-between">
                                        <span className="text-gray-600">Duración:</span>
                                        <span className="font-medium text-gray-900">{formatDuration(selectedLlamada.duracionSegundos)}</span>
                                    </div>
                                </div>
                            </div>

                            {selectedLlamada.notas && (
                                <div>
                                    <h3 className="text-base font-semibold text-gray-900 mb-3">Notas completas</h3>
                                    <div className="p-4 rounded-lg bg-gray-50 text-sm text-gray-700">
                                        <p>{selectedLlamada.notas}</p>
                                    </div>
                                </div>
                            )}

                            {selectedLlamada.fechaReagendamiento && (
                                <div>
                                    <h3 className="text-base font-semibold text-gray-900 mb-3">Info de reagendamiento</h3>
                                    <p className="text-sm text-gray-700">
                                        Reagendado para: {new Date(selectedLlamada.fechaReagendamiento).toLocaleString()}
                                    </p>
                                </div>
                            )}

                            {selectedLlamada.derivadoVentas && (
                                <div>
                                    <h3 className="text-base font-semibold text-gray-900 mb-3">Derivación a ventas</h3>
                                    <p className="text-sm text-gray-700">
                                        Tipo: {selectedLlamada.tipoOportunidad || 'No especificado'}
                                    </p>
                                </div>
                            )}
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};
