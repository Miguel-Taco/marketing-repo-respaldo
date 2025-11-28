import React from 'react';
import { FilterDefinition } from './FilterRow';

interface FilterInfoDisplayProps {
    filters: FilterDefinition[];
}

export const FilterInfoDisplay: React.FC<FilterInfoDisplayProps> = ({ filters }) => {
    if (!filters || filters.length === 0) {
        return (
            <div className="text-center py-4 text-gray-500 bg-gray-50 rounded-lg border border-gray-200">
                <span className="material-symbols-outlined text-4xl text-gray-300">filter_alt_off</span>
                <p className="mt-2 text-sm">No hay filtros aplicados</p>
            </div>
        );
    }

    const getOperatorLabel = (operator: string): string => {
        const labels: Record<string, string> = {
            'IGUAL': 'es igual a',
            'DIFERENTE': 'es diferente de',
            'MAYOR_QUE': 'es mayor que',
            'MENOR_QUE': 'es menor que',
            'CONTIENE': 'contiene',
            'NO_CONTIENE': 'no contiene',
            'COMIENZA_CON': 'comienza con',
            'TERMINA_CON': 'termina con',
            'ENTRE': 'está entre'
        };
        return labels[operator] || operator;
    };

    const getFieldLabel = (field: string): string => {
        const labels: Record<string, string> = {
            'edad': 'Edad',
            'distrito': 'Distrito',
            'provincia': 'Provincia',
            'departamento': 'Departamento',
            'genero': 'Género',
            'estado_civil': 'Estado Civil',
            'nivel_educativo': 'Nivel Educativo',
            'ocupacion': 'Ocupación',
            'ingreso_mensual': 'Ingreso Mensual',
            'email': 'Correo Electrónico',
            'telefono': 'Teléfono',
            'fecha_registro': 'Fecha de Registro'
        };
        return labels[field] || field;
    };

    const getFieldIcon = (field: string): string => {
        const icons: Record<string, string> = {
            'edad': 'cake',
            'distrito': 'location_on',
            'provincia': 'map',
            'departamento': 'public',
            'genero': 'person',
            'estado_civil': 'favorite',
            'nivel_educativo': 'school',
            'ocupacion': 'work',
            'ingreso_mensual': 'attach_money',
            'email': 'email',
            'telefono': 'phone',
            'fecha_registro': 'calendar_today'
        };
        return icons[field] || 'filter_alt';
    };

    return (
        <div className="space-y-3">
            <div className="flex items-center gap-2 mb-3">
                <span className="material-symbols-outlined text-primary">filter_list</span>
                <h3 className="text-sm font-semibold text-gray-700">Filtros Aplicados</h3>
                <span className="text-xs bg-primary text-white px-2 py-0.5 rounded-full">{filters.length}</span>
            </div>

            <div className="space-y-2">
                {filters.map((filter, index) => (
                    <div
                        key={index}
                        className="flex items-center gap-3 p-3 bg-blue-50 border border-blue-200 rounded-lg hover:bg-blue-100 transition-colors"
                    >
                        <span className="material-symbols-outlined text-primary text-xl">
                            {getFieldIcon(filter.campo)}
                        </span>
                        <div className="flex-1">
                            <div className="flex items-center gap-2 flex-wrap">
                                <span className="font-semibold text-gray-800 text-sm">
                                    {getFieldLabel(filter.campo)}
                                </span>
                                <span className="text-gray-500 text-xs">
                                    {getOperatorLabel(filter.operador)}
                                </span>
                                <span className="font-medium text-primary text-sm bg-white px-2 py-0.5 rounded border border-blue-200">
                                    {filter.valorTexto}
                                </span>
                            </div>
                        </div>
                        {index < filters.length - 1 && (
                            <div className="text-xs font-semibold text-gray-400 uppercase">Y</div>
                        )}
                    </div>
                ))}
            </div>
        </div>
    );
};
