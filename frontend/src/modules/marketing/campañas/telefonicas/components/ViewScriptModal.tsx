import React from 'react';
import { X, FileText, Target, Tag, StickyNote } from 'lucide-react';
import { GuionDTO } from '../types/guiones.types';
import { generateMarkdownFromScript } from '../utils/markdownGenerator';
import { MarkdownViewer } from './MarkdownViewer';

interface ViewScriptModalProps {
    isOpen: boolean;
    onClose: () => void;
    guion: GuionDTO | null;
}

export const ViewScriptModal: React.FC<ViewScriptModalProps> = ({
    isOpen,
    onClose,
    guion,
}) => {
    if (!isOpen || !guion) return null;

    const markdownContent = generateMarkdownFromScript({
        nombre: guion.nombre,
        objetivo: guion.objetivo,
        tipo: guion.tipo,
        notasInternas: guion.notasInternas,
        secciones: guion.secciones,
    });

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
            {/* Backdrop */}
            <div
                className="absolute inset-0 bg-black bg-opacity-50"
                onClick={onClose}
            />

            {/* Modal */}
            <div className="relative bg-white rounded-lg shadow-xl max-w-4xl w-full mx-4 max-h-[90vh] flex flex-col">
                {/* Header */}
                <div className="flex items-center justify-between p-6 border-b border-gray-200">
                    <div className="flex items-center gap-3">
                        <div className="p-2 bg-blue-50 rounded-lg">
                            <FileText className="w-6 h-6 text-blue-600" />
                        </div>
                        <div>
                            <h2 className="text-2xl font-bold text-gray-900">
                                {guion.nombre}
                            </h2>
                            <div className="flex items-center gap-2 mt-1">
                                <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-gray-100 text-gray-800">
                                    {guion.tipo}
                                </span>
                                <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium ${guion.estado === 'ACTIVO'
                                        ? 'bg-green-100 text-green-800'
                                        : 'bg-gray-100 text-gray-800'
                                    }`}>
                                    {guion.estado || 'BORRADOR'}
                                </span>
                            </div>
                        </div>
                    </div>
                    <button
                        onClick={onClose}
                        className="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-lg transition-colors"
                    >
                        <X className="w-6 h-6" />
                    </button>
                </div>

                {/* Content */}
                <div className="flex-1 overflow-y-auto p-6">
                    {/* Metadata Section */}
                    <div className="mb-6 space-y-4">
                        <div className="flex items-start gap-3 p-4 bg-blue-50 rounded-lg">
                            <Target className="w-5 h-5 text-blue-600 mt-0.5" />
                            <div className="flex-1">
                                <h3 className="text-sm font-semibold text-blue-900 mb-1">
                                    Objetivo
                                </h3>
                                <p className="text-sm text-blue-800">
                                    {guion.objetivo}
                                </p>
                            </div>
                        </div>

                        {guion.notasInternas && (
                            <div className="flex items-start gap-3 p-4 bg-amber-50 rounded-lg">
                                <StickyNote className="w-5 h-5 text-amber-600 mt-0.5" />
                                <div className="flex-1">
                                    <h3 className="text-sm font-semibold text-amber-900 mb-1">
                                        Notas Internas
                                    </h3>
                                    <p className="text-sm text-amber-800">
                                        {guion.notasInternas}
                                    </p>
                                </div>
                            </div>
                        )}

                        <div className="flex items-center gap-2 text-sm text-gray-600">
                            <Tag className="w-4 h-4" />
                            <span>
                                {guion.secciones?.length || 0} sección{guion.secciones?.length !== 1 ? 'es' : ''}
                            </span>
                        </div>
                    </div>

                    {/* Divider */}
                    <div className="border-t border-gray-200 my-6" />

                    {/* Script Content */}
                    <div className="bg-gray-50 rounded-lg p-6">
                        <MarkdownViewer
                            content={markdownContent}
                            className="prose-headings:text-gray-900 prose-p:text-gray-700 prose-strong:text-gray-900"
                        />
                    </div>
                </div>

                {/* Footer */}
                <div className="flex justify-end gap-3 p-6 border-t border-gray-200 bg-gray-50">
                    <button
                        onClick={onClose}
                        className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
                    >
                        Cerrar
                    </button>
                </div>
            </div>
        </div>
    );
};
