import React from 'react';
import { useSegmentosContext } from '../context/SegmentosContext';

interface Segmento {
    id: number;
    nombre: string;
    descripcion?: string;
    tipoAudiencia: 'LEAD' | 'CLIENTE' | 'MIXTO';
    estado: string;
    cantidadMiembros: number;
    reglaPrincipal?: any;
}

interface TemplateModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSelectTemplate: (template: Segmento) => void;
}

// Mapeo de operadores a texto legible
const OPERATOR_LABELS: Record<string, string> = {
    'IGUAL': 'es',
    'DIFERENTE': 'no es',
    'MAYOR_QUE': 'mayor que',
    'MENOR_QUE': 'menor que',
    'MAYOR_IGUAL': 'mayor o igual a',
    'MENOR_IGUAL': 'menor o igual a',
    'CONTIENE': 'contiene',
};

// Mapeo de campos a etiquetas legibles
const FIELD_LABELS: Record<string, string> = {
    'genero': 'Género',
    'edad': 'Edad',
    'provincia': 'Provincia',
    'distrito': 'Distrito',
    'departamento': 'Departamento',
    'ciudad': 'Ciudad',
    'estado_civil': 'Estado Civil',
    'niveleducativo': 'Nivel Educativo',
};

export const TemplateModal: React.FC<TemplateModalProps> = ({ isOpen, onClose, onSelectTemplate }) => {
    // Usar el contexto en vez de hacer una nueva consulta
    const { segmentos } = useSegmentosContext();

    // Filtrar solo ACTIVO e INACTIVO (no ELIMINADO)
    const templates = segmentos.filter(
        (seg) => seg.estado === 'ACTIVO' || seg.estado === 'INACTIVO'
    );

    const handleSelectTemplate = (template: Segmento) => {
        onSelectTemplate(template);
        onClose();
    };

    const getFilterSummary = (template: Segmento): string[] => {
        if (!template.reglaPrincipal || !template.reglaPrincipal.reglas) {
            return [];
        }

        return template.reglaPrincipal.reglas.map((regla: any) => {
            const fieldLabel = FIELD_LABELS[regla.campo] || regla.campo;
            const operatorLabel = OPERATOR_LABELS[regla.operador] || regla.operador;
            return `${fieldLabel} ${operatorLabel} ${regla.valorTexto}`;
        });
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-xl shadow-2xl max-w-4xl w-full max-h-[90vh] overflow-hidden flex flex-col">
                {/* Header */}
                <div className="flex items-center justify-between p-6 border-b border-gray-200">
                    <h2 className="text-2xl font-bold text-gray-900">Selecciona una Plantilla de Segmento</h2>
                    <button
                        onClick={onClose}
                        className="p-2 rounded-full hover:bg-gray-100 transition-colors"
                    >
                        <span className="material-symbols-outlined text-gray-500">close</span>
                    </button>
                </div>

                {/* Content */}
                <div className="flex-1 overflow-y-auto p-6">
                    {templates.length === 0 ? (
                        <div className="text-center py-12">
                            <span className="material-symbols-outlined text-6xl text-gray-300 mb-4">folder_open</span>
                            <p className="text-gray-500">No hay plantillas disponibles</p>
                            <p className="text-sm text-gray-400 mt-2">Crea segmentos para usarlos como plantillas</p>
                        </div>
                    ) : (
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            {templates.map((template) => {
                                const filters = getFilterSummary(template);
                                const badgeColor = template.tipoAudiencia === 'CLIENTE' ? 'bg-purple-100 text-purple-700' :
                                    template.tipoAudiencia === 'MIXTO' ? 'bg-orange-100 text-orange-700' :
                                        'bg-blue-100 text-blue-700';

                                return (
                                    <div
                                        key={template.id}
                                        className="border border-gray-200 rounded-lg p-4 hover:border-primary hover:shadow-md transition-all cursor-pointer"
                                        onClick={() => handleSelectTemplate(template)}
                                    >
                                        {/* Template Header */}
                                        <div className="flex items-start justify-between mb-3">
                                            <div className="flex-1">
                                                <h3 className="font-semibold text-gray-900 mb-1">{template.nombre}</h3>
                                                <span className={`inline-block px-2 py-1 rounded-full text-xs font-medium ${badgeColor}`}>
                                                    {template.tipoAudiencia}
                                                </span>
                                            </div>
                                        </div>

                                        {/* Description */}
                                        {template.descripcion && (
                                            <p className="text-sm text-gray-600 mb-3">{template.descripcion}</p>
                                        )}

                                        {/* Filters Applied */}
                                        {filters.length > 0 && (
                                            <div className="mb-3">
                                                <p className="text-xs font-medium text-gray-500 mb-2">FILTROS APLICADOS:</p>
                                                <div className="flex flex-wrap gap-2">
                                                    {filters.map((filter, idx) => (
                                                        <span
                                                            key={idx}
                                                            className="inline-block px-2 py-1 bg-gray-100 text-gray-700 rounded text-xs"
                                                        >
                                                            {filter}
                                                        </span>
                                                    ))}
                                                </div>
                                            </div>
                                        )}

                                        {/* Use Button */}
                                        <button
                                            className="w-full bg-primary text-white py-2 rounded-lg hover:bg-primary-dark transition-colors font-medium"
                                            onClick={(e) => {
                                                e.stopPropagation();
                                                handleSelectTemplate(template);
                                            }}
                                        >
                                            Usar esta Plantilla
                                        </button>
                                    </div>
                                );
                            })}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};
