import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Tabs } from '../../../../shared/components/ui/Tabs';
import { ImportUploader } from '../components/ImportUploader';
import { ImportProgressModal } from '../components/ImportProgressModal';
import { useImportProgress } from '../hooks/useImportProgress';
import { leadsApi } from '../services/leads.api';
import { LoteImportacion } from '../types/lead.types';

import { useImportHistory } from '../context/ImportHistoryContext';
import { useLeadsContext } from '../context/LeadsContext';

export const LeadImportPage: React.FC = () => {
    const navigate = useNavigate();
    const [uploading, setUploading] = useState(false);
    const [currentLoteId, setCurrentLoteId] = useState<number | null>(null);
    const [showProgressModal, setShowProgressModal] = useState(false);

    // Hook para websocket progress
    const { progress } = useImportProgress({
        loteId: currentLoteId,
        enabled: showProgressModal
    });

    // Use context for history state
    const { history, loading, currentPage, totalPages, totalElements, loadHistory } = useImportHistory();
    const { fetchLeads } = useLeadsContext();

    // Ref to track processed completion to avoid infinite loops
    const processedLoteIdRef = React.useRef<number | null>(null);

    // Detectar cuando la importación se completa
    useEffect(() => {
        // Only run if completed AND not already processed for this loteId
        if (progress?.completado && progress.loteId !== processedLoteIdRef.current) {
            processedLoteIdRef.current = progress.loteId; // Mark as processed

            setUploading(false);
            loadHistory(currentPage, true); // Force refresh import history
            fetchLeads(true); // Also refresh leads list

            // IMPORTANTE: Cerrar el modal y limpiar WebSocket después de 2 segundos
            // para que el usuario tenga tiempo de ver el resultado final
            setTimeout(() => {
                setShowProgressModal(false);
                setCurrentLoteId(null); // This will trigger WebSocket cleanup
            }, 2000);
        }
    }, [progress, currentPage, loadHistory, fetchLeads]);
    const handleUpload = async (file: File) => {
        setUploading(true);

        // 1. Show modal FIRST to ensure it's visible from the start
        setShowProgressModal(true);

        // 2. Small delay to allow modal to render and WebSocket to establish connection
        await new Promise(resolve => setTimeout(resolve, 250));

        try {
            // 3. Upload file and get lote ID
            const response = await leadsApi.uploadFile(file);
            const loteInicial = response.data;

            // 4. Set lote ID to connect WebSocket for real-time updates
            setCurrentLoteId(loteInicial.id);

            // The WebSocket hook will handle real-time progress updates

        } catch (e: any) {
            console.error("Error de red al subir archivo:", e);
            setUploading(false);
            setShowProgressModal(false);
            setCurrentLoteId(null);
            alert("Error al subir el archivo. Por favor, intenta nuevamente.");
        }
    };

    const handleCloseModal = () => {
        setShowProgressModal(false);
        setCurrentLoteId(null);
    };

    const formatDate = (dateString: string): string => {
        return new Date(dateString).toLocaleDateString('es-PE', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric'
        });
    };

    return (
        <div className="space-y-6">
            <header>
                <h1 className="text-3xl font-bold text-dark">Monitor de Importaciones</h1>
                <p className="text-gray-500 mt-1">Carga masiva y seguimiento de lotes.</p>
            </header>

            <Tabs
                items={[
                    { label: 'Listado de Leads', value: 'list' },
                    { label: 'Monitor de Importaciones', value: 'import' }
                ]}
                activeValue="import"
                onChange={(val) => val === 'list' && navigate('/leads')}
            />

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                <div className="lg:col-span-1">
                    <ImportUploader onUpload={handleUpload} isUploading={uploading} />
                </div>

                <div className="lg:col-span-2">
                    <div className="bg-white rounded-lg shadow-card border border-separator overflow-hidden">
                        <div className="p-5 border-b border-separator bg-gray-50">
                            <h3 className="text-lg font-semibold text-dark">Historial Reciente</h3>
                        </div>
                        <div className="overflow-x-auto">
                            <table className="w-full text-left text-sm">
                                <thead className="bg-gray-100 border-b border-separator">
                                    <tr>
                                        <th className="p-3 font-semibold text-gray-600">Archivo</th>
                                        <th className="p-3 font-semibold text-gray-600">Fecha</th>
                                        <th className="p-3 font-semibold text-gray-600">Total</th>
                                        <th className="p-3 font-semibold text-gray-600">Exitosos</th>
                                        <th className="p-3 font-semibold text-gray-600">Duplicados</th>
                                        <th className="p-3 font-semibold text-gray-600">Con Errores</th>
                                    </tr>
                                </thead>
                                <tbody className="divide-y divide-separator">
                                    {history.length > 0 ? (
                                        history.map((lote) => (
                                            <tr key={lote.id} className="hover:bg-gray-50">
                                                <td className="p-3 font-medium text-dark">{lote.nombreArchivo}</td>
                                                <td className="p-3 text-gray-500">
                                                    {lote.createdAt ? formatDate(lote.createdAt) : '-'}
                                                </td>
                                                <td className="p-3">{lote.totalRegistros}</td>
                                                <td className="p-3 text-green-600 font-bold">{lote.exitosos}</td>
                                                <td className="p-3 text-amber-600 font-bold">{lote.duplicados || 0}</td>
                                                <td className="p-3 text-red-600 font-bold">{lote.conErrores || 0}</td>
                                            </tr>
                                        ))
                                    ) : (
                                        <tr>
                                            <td colSpan={6} className="p-8 text-center text-gray-400">
                                                No hay importaciones registradas.
                                            </td>
                                        </tr>
                                    )}
                                </tbody>
                            </table>
                        </div>

                        {/* Controles de Paginación */}
                        {totalPages > 0 && (
                            <div className="p-4 border-t border-separator bg-gray-50 flex items-center justify-between">
                                <div className="text-sm text-gray-600">
                                    Mostrando página {currentPage + 1} de {totalPages} ({totalElements} registros en total)
                                </div>
                                <div className="flex gap-2">
                                    <button
                                        onClick={() => loadHistory(currentPage - 1)}
                                        disabled={currentPage === 0}
                                        className="px-4 py-2 bg-white border border-separator rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition"
                                    >
                                        ← Anterior
                                    </button>
                                    <button
                                        onClick={() => loadHistory(currentPage + 1)}
                                        disabled={currentPage >= totalPages - 1}
                                        className="px-4 py-2 bg-white border border-separator rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition"
                                    >
                                        Siguiente →
                                    </button>
                                </div>
                            </div>
                        )}
                    </div>
                </div>
            </div>

            <ImportProgressModal
                isOpen={showProgressModal}
                progress={progress}
                onClose={handleCloseModal}
            />
        </div>
    );
};
