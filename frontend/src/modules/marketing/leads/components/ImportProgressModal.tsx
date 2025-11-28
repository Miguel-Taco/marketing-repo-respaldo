import React from 'react';
import { Modal } from '../../../../shared/components/ui/Modal';
import { ImportProgressUpdate } from '../hooks/useImportProgress';

interface ImportProgressModalProps {
    isOpen: boolean;
    progress: ImportProgressUpdate | null;
    onClose: () => void;
}

export const ImportProgressModal: React.FC<ImportProgressModalProps> = ({
    isOpen,
    progress,
    onClose
}) => {
    // Show loading state if modal is open but no progress yet
    if (!progress) {
        return (
            <Modal
                isOpen={isOpen}
                onClose={undefined} // Can't close while initializing
                title={<div className="flex items-center gap-2"><span className="material-symbols-outlined text-blue-600 animate-spin">sync</span> Iniciando Importación...</div>}
                variant='primary'
            >
                <div className="space-y-4">
                    <div className="p-4 rounded-lg bg-blue-50 border-2 border-blue-200">
                        <p className="text-center font-semibold text-blue-800">
                            Preparando el archivo para procesarlo...
                        </p>
                    </div>
                    <div className="flex justify-center">
                        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
                    </div>
                </div>
            </Modal>
        );
    }

    const porcentaje = progress.totalRegistros > 0
        ? (progress.procesados / progress.totalRegistros) * 100
        : 0;

    const rechazados = progress.duplicados + progress.conErrores;
    const esExitoso = progress.completado && rechazados === 0;
    const tieneErrores = progress.completado && rechazados > 0;

    return (
        <Modal
            isOpen={isOpen}
            onClose={progress.completado ? onClose : undefined}
            title={
                progress.completado
                    ? esExitoso
                        ? <div className="flex items-center gap-2"><span className="material-symbols-outlined text-green-600">check_circle</span> Importación Exitosa</div>
                        : <div className="flex items-center gap-2"><span className="material-symbols-outlined text-amber-600">warning</span> Importación Completada con Observaciones</div>
                    : <div className="flex items-center gap-2"><span className="material-symbols-outlined text-blue-600 animate-spin">sync</span> Procesando Importación...</div>
            }
            onConfirm={progress.completado ? onClose : undefined}
            confirmText={progress.completado ? "Entendido" : undefined}
            variant={esExitoso ? 'primary' : tieneErrores ? 'danger' : 'primary'}
        >
            <div className="space-y-4">
                {/* Barra de Progreso */}
                <div className="space-y-2">
                    <div className="flex justify-between text-sm text-gray-600">
                        <span>Progreso</span>
                        <span className="font-semibold">{progress.procesados} / {progress.totalRegistros}</span>
                    </div>
                    <div className="w-full bg-gray-200 rounded-full h-3 overflow-hidden">
                        <div
                            className={`h-full transition-all duration-300 ${progress.completado
                                ? esExitoso
                                    ? 'bg-green-500'
                                    : 'bg-amber-500'
                                : 'bg-blue-500 animate-pulse'
                                }`}
                            style={{ width: `${porcentaje}%` }}
                        />
                    </div>
                    <div className="text-right text-xs text-gray-500">
                        {porcentaje.toFixed(1)}%
                    </div>
                </div>

                {/* Mensaje de Estado */}
                {progress.completado ? (
                    <div className={`p-4 rounded-lg border-2 ${esExitoso
                        ? 'bg-green-50 border-green-200'
                        : 'bg-amber-50 border-amber-200'
                        }`}>
                        <p className="text-center font-semibold text-lg text-dark">
                            {esExitoso
                                ? '¡Todos los registros se procesaron correctamente!'
                                : 'El proceso finalizó con algunos errores'}
                        </p>
                    </div>
                ) : (
                    <div className="p-4 rounded-lg bg-blue-50 border-2 border-blue-200">
                        <p className="text-center font-semibold text-blue-800">
                            Procesando fila {progress.procesados} de {progress.totalRegistros}...
                        </p>
                    </div>
                )}

                {/* Estadísticas en Tarjetas */}
                <div className="grid grid-cols-3 gap-3">
                    <div className="bg-gray-50 rounded-lg border border-separator p-4 text-center">
                        <div className="text-gray-500 text-xs uppercase font-semibold mb-1">Total</div>
                        <div className="text-2xl font-bold text-dark">{progress.totalRegistros}</div>
                    </div>
                    <div className="bg-green-50 rounded-lg border border-green-200 p-4 text-center">
                        <div className="text-green-700 text-xs uppercase font-semibold mb-1">Exitosos</div>
                        <div className="text-2xl font-bold text-green-600">{progress.exitosos}</div>
                    </div>
                    <div className="bg-red-50 rounded-lg border border-red-200 p-4 text-center">
                        <div className="text-red-700 text-xs uppercase font-semibold mb-1">Fallidos</div>
                        <div className="text-2xl font-bold text-red-600">{rechazados}</div>
                    </div>
                </div>

                {/* Desglose de Rechazados */}
                {rechazados > 0 && progress.completado && (
                    <div className="grid grid-cols-2 gap-2">
                        <div className="bg-amber-50 rounded border border-amber-200 p-3 text-center">
                            <div className="text-amber-700 text-xs uppercase mb-1">Duplicados</div>
                            <div className="text-xl font-bold text-amber-600">{progress.duplicados}</div>
                        </div>
                        <div className="bg-red-50 rounded border border-red-200 p-3 text-center">
                            <div className="text-red-700 text-xs uppercase mb-1">Con Errores</div>
                            <div className="text-xl font-bold text-red-600">{progress.conErrores}</div>
                        </div>
                    </div>
                )}

                {/* Nota informativa */}
                {rechazados > 0 && progress.completado && (
                    <div className="bg-blue-50 border-l-4 border-blue-400 p-3 rounded">
                        <p className="text-sm text-blue-800">
                            <strong>Nota:</strong> Los registros fallidos (duplicados o inválidos) se descartaron automáticamente.
                            Los registros exitosos ya están disponibles en la lista de leads.
                        </p>
                    </div>
                )}

                {/* Nombre del Archivo */}
                <p className="text-xs text-gray-500 text-center">
                    Archivo: <span className="font-semibold">{progress.nombreArchivo}</span>
                </p>

                {/* Spinner mientras está en progreso */}
                {!progress.completado && (
                    <div className="flex justify-center">
                        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
                    </div>
                )}
            </div>
        </Modal>
    );
};
