import React from 'react';
import type { EnvioEncuesta } from '../types';

interface SurveyDetailModalProps {
    isOpen: boolean;
    onClose: () => void;
    envioEncuesta: EnvioEncuesta | null;
}

export const SurveyDetailModal: React.FC<SurveyDetailModalProps> = ({
    isOpen,
    onClose,
    envioEncuesta
}) => {
    if (!isOpen || !envioEncuesta) return null;

    const copyToClipboard = (text: string) => {
        navigator.clipboard.writeText(text);
        // TODO: Mostrar toast de confirmación
    };

    const openInNewTab = (url: string) => {
        window.open(url, '_blank');
    };

    const formatDate = (dateString: string) => {
        const date = new Date(dateString);
        return date.toLocaleString('es-PE', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40">
            <div className="layout-content-container flex flex-col w-full max-w-2xl bg-white rounded-lg shadow-2xl overflow-hidden">
                {/* Modal Header */}
                <div className="flex items-start justify-between gap-4 p-6 border-b border-gray-200">
                    <div className="flex flex-col gap-1">
                        <p className="text-gray-800 text-xl font-semibold leading-tight">Encuesta post-llamada</p>
                        <p className="text-gray-500 text-sm font-normal leading-normal">
                            Lead: {envioEncuesta.nombreLead} – {envioEncuesta.nombreCampania}
                        </p>
                    </div>
                    <button
                        onClick={onClose}
                        className="flex items-center justify-center rounded-full h-8 w-8 text-gray-500 hover:bg-gray-100 hover:text-gray-800 transition-colors"
                    >
                        <span className="material-symbols-outlined text-2xl">close</span>
                    </button>
                </div>

                {/* Modal Body */}
                <div className="flex-1 p-6 space-y-6 overflow-y-auto">
                    {/* Survey Status Block */}
                    <div className="space-y-2">
                        <h3 className="text-gray-800 text-base font-semibold leading-tight tracking-[-0.01em]">
                            Estado de la encuesta
                        </h3>
                        <div className="flex items-center justify-between gap-3 flex-wrap">
                            <div className="flex items-center gap-2">
                                <p className="text-gray-500 text-sm">Encuesta:</p>
                                <div className="flex h-7 shrink-0 items-center justify-center gap-x-2 rounded-full bg-gray-100 px-3">
                                    <p className="text-gray-800 text-sm font-medium leading-normal">
                                        {envioEncuesta.tituloEncuesta || 'Encuesta de satisfacción post-llamada'}
                                    </p>
                                </div>
                            </div>
                            <div className={`flex h-7 shrink-0 items-center justify-center gap-x-1.5 rounded-full px-3 ${envioEncuesta.estado === 'ENVIADA'
                                    ? 'bg-lime-500/10'
                                    : envioEncuesta.estado === 'ERROR'
                                        ? 'bg-red-500/10'
                                        : 'bg-yellow-500/10'
                                }`}>
                                <span className={`w-2 h-2 rounded-full ${envioEncuesta.estado === 'ENVIADA'
                                        ? 'bg-lime-500'
                                        : envioEncuesta.estado === 'ERROR'
                                            ? 'bg-red-500'
                                            : 'bg-yellow-500'
                                    }`}></span>
                                <p className={`text-sm font-medium leading-normal ${envioEncuesta.estado === 'ENVIADA'
                                        ? 'text-lime-700'
                                        : envioEncuesta.estado === 'ERROR'
                                            ? 'text-red-700'
                                            : 'text-yellow-700'
                                    }`}>
                                    {envioEncuesta.estado === 'ENVIADA' ? 'Enviada' :
                                        envioEncuesta.estado === 'ERROR' ? 'Error' : 'Pendiente'}
                                </p>
                            </div>
                        </div>
                        <p className="text-gray-600 text-sm font-normal leading-normal pt-1">
                            Origen: Campaña Telefónica – {envioEncuesta.nombreCampania}
                        </p>
                    </div>

                    {/* Shipment Details Card */}
                    <div className="border border-gray-200 rounded-lg bg-gray-50 p-4">
                        <h3 className="text-gray-800 text-base font-semibold leading-tight tracking-[-0.01em] mb-4">
                            Datos de envío
                        </h3>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-x-6 gap-y-4">
                            <div className="flex flex-col gap-1">
                                <p className="text-gray-500 text-sm font-normal leading-normal">Fecha de envío</p>
                                <p className="text-gray-800 text-sm font-medium leading-normal">
                                    {formatDate(envioEncuesta.fechaEnvio)}
                                </p>
                            </div>
                            <div className="flex flex-col gap-1">
                                <p className="text-gray-500 text-sm font-normal leading-normal">Teléfono destino</p>
                                <p className="text-gray-800 text-sm font-medium leading-normal">
                                    {envioEncuesta.telefonoDestino}
                                </p>
                            </div>
                            <div className="md:col-span-2 flex flex-col gap-1">
                                <p className="text-gray-500 text-sm font-normal leading-normal">Enlace de encuesta</p>
                                <div className="relative flex items-center">
                                    <input
                                        className="w-full h-9 rounded-md border border-gray-300 bg-white text-gray-800 text-sm px-3 focus:outline-none focus:ring-2 focus:ring-primary/50 truncate pr-20"
                                        readOnly
                                        type="text"
                                        value={envioEncuesta.urlEncuesta}
                                    />
                                    <button
                                        onClick={() => openInNewTab(envioEncuesta.urlEncuesta)}
                                        className="absolute right-10 flex h-7 w-7 items-center justify-center rounded-md text-gray-500 hover:bg-gray-100 hover:text-gray-800 transition-colors"
                                        title="Abrir en nueva pestaña"
                                    >
                                        <span className="material-symbols-outlined text-lg">open_in_new</span>
                                    </button>
                                    <button
                                        onClick={() => copyToClipboard(envioEncuesta.urlEncuesta)}
                                        className="absolute right-1.5 flex h-7 w-7 items-center justify-center rounded-md text-gray-500 hover:bg-gray-100 hover:text-gray-800 transition-colors"
                                        title="Copiar enlace"
                                    >
                                        <span className="material-symbols-outlined text-lg">content_copy</span>
                                    </button>
                                </div>
                            </div>
                            {envioEncuesta.metodoComunicacion && (
                                <div className="flex flex-col gap-1">
                                    <p className="text-gray-500 text-sm font-normal leading-normal">Método de envío</p>
                                    <p className="text-gray-800 text-sm font-medium leading-normal">
                                        {envioEncuesta.metodoComunicacion}
                                    </p>
                                </div>
                            )}
                            {envioEncuesta.mensajeError && (
                                <div className="md:col-span-2 flex flex-col gap-1">
                                    <p className="text-red-500 text-sm font-normal leading-normal">Error</p>
                                    <p className="text-red-700 text-sm font-medium leading-normal">
                                        {envioEncuesta.mensajeError}
                                    </p>
                                </div>
                            )}
                        </div>
                    </div>
                </div>

                {/* Modal Footer */}
                <div className="flex flex-wrap justify-end gap-3 p-4 bg-gray-50 border-t border-gray-200">
                    <button
                        onClick={onClose}
                        className="flex min-w-[84px] max-w-[480px] cursor-pointer items-center justify-center overflow-hidden rounded-full h-9 px-4 bg-gray-200 text-gray-800 text-sm font-medium leading-normal hover:bg-gray-300 transition-colors"
                    >
                        <span className="truncate">Cerrar</span>
                    </button>
                    <button
                        onClick={() => copyToClipboard(envioEncuesta.urlEncuesta)}
                        className="flex min-w-[84px] max-w-[480px] cursor-pointer items-center justify-center gap-2 overflow-hidden rounded-full h-9 px-4 bg-primary text-white text-sm font-medium leading-normal hover:bg-primary/90 transition-colors"
                    >
                        <span className="material-symbols-outlined text-base">link</span>
                        <span className="truncate">Copiar enlace</span>
                    </button>
                </div>
            </div>
        </div>
    );
};
