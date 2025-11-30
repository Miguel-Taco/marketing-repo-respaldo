import React from 'react';
import { FileText, Edit, Link, Trash2, Eye } from 'lucide-react';
import { GuionDTO } from '../types/guiones.types';

interface ScriptCardProps {
    guion: GuionDTO;
    onEdit: (guion: GuionDTO) => void;
    onLink: (guion: GuionDTO) => void;
    onDelete: (guion: GuionDTO) => void;
    onView: (guion: GuionDTO) => void;
    showLinkButton?: boolean; // Show link button only when not in campaign context
}

export const ScriptCard: React.FC<ScriptCardProps> = ({
    guion,
    onEdit,
    onLink,
    onDelete,
    onView,
    showLinkButton = false, // Default to false (don't show in campaign context)
}) => {
    return (
        <div className="bg-white rounded-lg border border-gray-200 p-5 hover:shadow-md transition-shadow">
            <div className="flex justify-between items-start mb-3">
                <div className="flex items-center gap-3">
                    <div className="p-2 bg-blue-50 rounded-lg">
                        <FileText className="w-6 h-6 text-blue-600" />
                    </div>
                    <div>
                        <h3 className="font-semibold text-gray-900 line-clamp-1" title={guion.nombre}>
                            {guion.nombre}
                        </h3>
                        <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-gray-100 text-gray-800 mt-1">
                            {guion.tipo}
                        </span>
                    </div>
                </div>
                <div className="flex gap-1">
                    <button
                        onClick={() => onView(guion)}
                        className="p-1.5 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                        title="Ver detalles"
                    >
                        <Eye className="w-4 h-4" />
                    </button>
                    <button
                        onClick={() => onEdit(guion)}
                        className="p-1.5 text-gray-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg transition-colors"
                        title="Editar"
                    >
                        <Edit className="w-4 h-4" />
                    </button>
                </div>
            </div>

            <div className="space-y-3 mb-4">
                <div>
                    <p className="text-xs font-medium text-gray-500 uppercase tracking-wider mb-1">
                        Objetivo
                    </p>
                    <p className="text-sm text-gray-600 line-clamp-2" title={guion.objetivo}>
                        {guion.objetivo}
                    </p>
                </div>

                <div className="flex items-center gap-4 text-sm text-gray-500">
                    <div className="flex items-center gap-1">
                        <span className="font-medium text-gray-900">{guion.secciones?.length || 0}</span>
                        <span>secciones</span>
                    </div>
                    <div className="flex items-center gap-1">
                        <span className={`w-2 h-2 rounded-full ${guion.estado === 'ACTIVO' ? 'bg-green-500' : 'bg-gray-300'}`} />
                        <span>{guion.estado || 'BORRADOR'}</span>
                    </div>
                </div>
            </div>

            <div className="flex gap-2 pt-3 border-t border-gray-100">
                {showLinkButton ? (
                    <>
                        <button
                            onClick={() => onLink(guion)}
                            className="flex-1 flex items-center justify-center gap-2 px-3 py-2 text-sm font-medium text-blue-700 bg-blue-50 hover:bg-blue-100 rounded-lg transition-colors"
                        >
                            <Link className="w-4 h-4" />
                            Vincular
                        </button>
                        <button
                            onClick={() => onDelete(guion)}
                            className="flex items-center justify-center px-3 py-2 text-sm font-medium text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                            title="Eliminar"
                        >
                            <Trash2 className="w-4 h-4" />
                        </button>
                    </>
                ) : (
                    <button
                        onClick={() => onDelete(guion)}
                        className="flex-1 flex items-center justify-center gap-2 px-3 py-2 text-sm font-medium text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                    >
                        <Trash2 className="w-4 h-4" />
                        Eliminar
                    </button>
                )}
            </div>
        </div>
    );
};
