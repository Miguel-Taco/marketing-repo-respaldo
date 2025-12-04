import React, { useEffect, useState, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { mailingApi } from '../services/mailing.api';
import { CampanaMailing, ActualizarContenidoRequest, EstadoCampana } from '../types/mailing.types';
import { ContentEditor, type ContentEditorHandle } from '../components/ContentEditor';
import { CampaignInfo } from '../components/CampaignInfo';
import { LoadingSpinner } from '../../../../../shared/components/ui/LoadingSpinner';
import { useToast } from '../../../../../shared/components/ui/Toast';

export const MailingEditorPage: React.FC = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { showToast } = useToast();
    
    const [campaign, setCampaign] = useState<CampanaMailing | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [isSaving, setIsSaving] = useState(false);
    const [asunto, setAsunto] = useState('');
    const [ctaText, setCtaText] = useState('');
    const editorRef = useRef<ContentEditorHandle>(null);

    // Cargar datos de la campaña
    useEffect(() => {
        const loadCampaign = async () => {
            try {
                setLoading(true);
                setError(null);
                const data = await mailingApi.obtenerDetalle(parseInt(id!));
                setCampaign(data);
                setAsunto(data.asunto || '');
                setCtaText(data.ctaTexto || '');
            } catch (err: any) {
                setError(err.message || 'Error al cargar la campaña');
                showToast('Error al cargar la campaña', 'error');
            } finally {
                setLoading(false);
            }
        };

        if (id) {
            loadCampaign();
        }
    }, [id, showToast]);

    const handleSaveContent = async () => {
        if (!campaign) return;

        try {
            setIsSaving(true);
            const editorContent = editorRef.current?.getHTML() || '';
            const data: ActualizarContenidoRequest = {
                asunto: asunto,
                cuerpo: editorContent,
                ctaTexto: ctaText
            };
            
            await mailingApi.guardarBorrador(parseInt(id!), data);
            
            // ✅ NUEVO: Mostrar mensaje diferente según estado previo
            if (campaign.idEstado === EstadoCampana.LISTO) {
                showToast(
                    '⚠️ Borrador guardado. La campaña regresó a PENDIENTE porque se modificó el contenido', 
                    'warning'
                );
            } else {
                showToast('✓ Borrador guardado exitosamente', 'success');
            }
            
            navigate('/emailing');
        } catch (err: any) {
            const errorMsg = err.message || 'Error al guardar el borrador';
            setError(errorMsg);
            showToast(errorMsg, 'error');
        } finally {
            setIsSaving(false);
        }
    };

    const handleMarkAsReady = async () => {
        if (!campaign) return;

        try {
            setIsSaving(true);
            const editorContent = editorRef.current?.getHTML?.() || campaign?.cuerpo || '';
            
            // Primero guardar el contenido
            const data: ActualizarContenidoRequest = {
                asunto: asunto,
                cuerpo: editorContent,
                ctaTexto: ctaText
            };
            await mailingApi.guardarBorrador(parseInt(id!), data);
            
            // Luego marcar como listo
            await mailingApi.marcarListo(parseInt(id!));
            
            showToast('✓ Campaña marcada como LISTO y preparada para envío', 'success');
            navigate('/emailing');
        } catch (err: any) {
            const errorMsg = err.message || 'Error al marcar como listo';
            setError(errorMsg);
            showToast(errorMsg, 'error');
        } finally {
            setIsSaving(false);
        }
    };

    if (loading) {
        return (
            <div className="flex items-center justify-center min-h-screen">
                <LoadingSpinner />
            </div>
        );
    }

    if (error && !campaign) {
        return (
            <div className="bg-red-50 border border-red-200 p-6 rounded-lg">
                <h3 className="text-red-800 font-semibold">Error</h3>
                <p className="text-red-700 mt-2">{error}</p>
                <button
                    onClick={() => navigate('/emailing')}
                    className="mt-4 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition"
                >
                    Volver
                </button>
            </div>
        );
    }

    if (!campaign) {
        return (
            <div className="bg-yellow-50 border border-yellow-200 p-6 rounded-lg">
                <p className="text-yellow-800">Campaña no encontrada</p>
            </div>
        );
    }

    // ✅ NUEVO: Determinar si puede marcar como listo
    const canMarkAsReady = campaign.idEstado === EstadoCampana.PENDIENTE;

    return (
        <div className="space-y-6">
            {/* Header */}
            <header className="flex justify-between items-center">
                <div>
                    <h1 className="text-3xl font-bold text-dark">{`Preparar Campaña: ${campaign.nombre}`}</h1>
                    <p className="text-gray-500 mt-1">
                        {campaign.idEstado === EstadoCampana.LISTO 
                            ? '⚠️ Campaña LISTA - Cualquier cambio la regresará a PENDIENTE'
                            : 'Edita el contenido de tu correo'
                        }
                    </p>
                </div>
                <div className="flex gap-3">
                    <button
                        onClick={handleSaveContent}
                        disabled={isSaving}
                        className="px-6 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition disabled:opacity-50 font-medium"
                    >
                        {isSaving ? 'Guardando...' : 'Guardar Borrador'}
                    </button>
                    <button
                        onClick={handleMarkAsReady}
                        disabled={isSaving || !canMarkAsReady}
                        className="px-6 py-2 bg-primary text-white rounded-lg hover:bg-primary/90 transition disabled:opacity-50 disabled:cursor-not-allowed font-medium"
                        title={!canMarkAsReady ? 'Solo campañas PENDIENTES pueden marcarse como LISTO' : ''}
                    >
                        {isSaving ? 'Procesando...' : 'Marcar como Listo'}
                    </button>
                </div>
            </header>


            {/* Main Content */}
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                {/* Editor (2/3 del ancho) */}
                <div className="lg:col-span-2">
                    <ContentEditor
                        ref={editorRef}
                        asunto={asunto}
                        cuerpo={campaign.cuerpo}
                        onAsuntoChange={setAsunto}
                    />
                </div>

                {/* Panel Lateral (1/3 del ancho) */}
                <div className="lg:col-span-1">
                    <CampaignInfo 
                        campaign={campaign}
                        onCtaTextChange={setCtaText}
                    />
                </div>
            </div>
        </div>
    );
};