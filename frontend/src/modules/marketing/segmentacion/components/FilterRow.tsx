import React, { useMemo } from 'react';

export interface FilterDefinition {
    campo: string;
    operador: string;
    valorTexto: string;
}

interface FilterRowProps {
    filter: FilterDefinition;
    index: number;
    showLogicOperator: boolean;
    onUpdate: (index: number, filter: FilterDefinition) => void;
    onRemove: (index: number) => void;
    disabled?: boolean;
}

const FILTER_OPTIONS = [
    { value: 'genero', label: 'Género', type: 'select', options: ['Masculino', 'Femenino', 'Otro'] },
    { value: 'edad', label: 'Edad', type: 'number' },
    { value: 'provincia', label: 'Provincia', type: 'text' },
    { value: 'estado_civil', label: 'Estado Civil', type: 'select', options: ['Soltero(a)', 'Casado(a)', 'Divorciado(a)', 'Viudo(a)'] },
    { value: 'distrito', label: 'Distrito', type: 'text' },
    { value: 'departamento', label: 'Departamento', type: 'text' },
    { value: 'utmSource', label: 'Fuente de Campaña', type: 'text' },
    { value: 'utmMedium', label: 'Medio de Campaña', type: 'text' },
    { value: 'utmCampaign', label: 'Nombre de Campaña', type: 'text' },
    { value: 'tipoFuente', label: 'Tipo de Origen', type: 'select', options: ['WEB', 'MANUAL', 'IMPORTACION'] },
];

// Operadores disponibles según el tipo de campo
const getAvailableOperators = (fieldType: string) => {
    switch (fieldType) {
        case 'number':
            return [
                { value: 'IGUAL', label: 'Es igual a' },
                { value: 'DIFERENTE', label: 'No es igual a' },
                { value: 'MAYOR_QUE', label: 'Es mayor que' },
                { value: 'MENOR_QUE', label: 'Es menor que' },
                { value: 'MAYOR_IGUAL', label: 'Mayor o igual a' },
                { value: 'MENOR_IGUAL', label: 'Menor o igual a' },
            ];
        case 'select':
            return [
                { value: 'IGUAL', label: 'Es igual a' },
                { value: 'DIFERENTE', label: 'No es igual a' },
            ];
        case 'text':
            return [
                { value: 'IGUAL', label: 'Es igual a' },
                { value: 'DIFERENTE', label: 'No es igual a' },
                { value: 'CONTIENE', label: 'Contiene' },
            ];
        default:
            return [
                { value: 'IGUAL', label: 'Es igual a' },
            ];
    }
};

export const FilterRow: React.FC<FilterRowProps> = ({
    filter,
    index,
    showLogicOperator,
    onUpdate,
    onRemove,
    disabled = false
}) => {
    const selectedFilterDef = FILTER_OPTIONS.find(f => f.value === filter.campo);

    // Obtener operadores disponibles para el campo seleccionado
    const availableOperators = useMemo(() => {
        return selectedFilterDef ? getAvailableOperators(selectedFilterDef.type) : [];
    }, [selectedFilterDef]);

    const handleFieldChange = (campo: string) => {
        const newFilterDef = FILTER_OPTIONS.find(f => f.value === campo);
        const newOperators = newFilterDef ? getAvailableOperators(newFilterDef.type) : [];

        // Si el operador actual no es válido para el nuevo campo, usar el primero disponible
        const validOperator = newOperators.some(op => op.value === filter.operador)
            ? filter.operador
            : (newOperators[0]?.value || 'IGUAL');

        onUpdate(index, { campo, operador: validOperator, valorTexto: '' });
    };

    const handleOperatorChange = (operador: string) => {
        onUpdate(index, { ...filter, operador });
    };

    const handleValueChange = (value: string) => {
        // Siempre guardar como valorTexto (el backend lo convertirá si es necesario)
        onUpdate(index, { ...filter, valorTexto: value });
    };

    return (
        <div className="flex items-center gap-3 py-3 border-b border-dashed border-gray-200 last:border-0">
            {/* Remove Button */}
            <button
                type="button"
                onClick={() => onRemove(index)}
                disabled={disabled}
                className="p-1 rounded-full text-gray-400 hover:text-red-500 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                title="Eliminar filtro"
            >
                <span className="material-symbols-outlined text-xl">close</span>
            </button>

            {/* Logic Operator */}
            <span className={`text-sm font-semibold text-gray-500 w-4 ${!showLogicOperator ? 'invisible' : ''}`}>
                Y
            </span>

            {/* Filter Field Selection */}
            <div className="relative flex-1 min-w-[140px]">
                <select
                    value={filter.campo}
                    onChange={(e) => handleFieldChange(e.target.value)}
                    disabled={disabled}
                    className="w-full px-3 py-2 pr-8 border border-separator rounded-lg focus:ring-2 focus:ring-primary focus:outline-none appearance-none disabled:bg-gray-50 disabled:text-gray-500"
                    required
                >
                    <option value="">Seleccionar filtro...</option>
                    {FILTER_OPTIONS.map(opt => (
                        <option key={opt.value} value={opt.value}>{opt.label}</option>
                    ))}
                </select>
                <span className="material-symbols-outlined absolute right-2 top-1/2 -translate-y-1/2 pointer-events-none text-gray-400">
                    arrow_drop_down
                </span>
            </div>

            {/* Operator Selection */}
            <div className="relative w-[180px]">
                <select
                    value={filter.operador}
                    onChange={(e) => handleOperatorChange(e.target.value)}
                    className="w-full px-3 py-2 pr-8 border border-separator rounded-lg focus:ring-2 focus:ring-primary focus:outline-none appearance-none disabled:bg-gray-50 disabled:text-gray-500"
                    required
                    disabled={!selectedFilterDef || disabled}
                >
                    {availableOperators.map(opt => (
                        <option key={opt.value} value={opt.value}>{opt.label}</option>
                    ))}
                </select>
                <span className="material-symbols-outlined absolute right-2 top-1/2 -translate-y-1/2 pointer-events-none text-gray-400">
                    arrow_drop_down
                </span>
            </div>

            {/* Value Input */}
            <div className="flex-1 min-w-[140px]">
                {!selectedFilterDef ? (
                    <input
                        type="text"
                        placeholder="Selecciona un filtro primero"
                        className="w-full px-3 py-2 border border-separator rounded-lg bg-gray-50 text-gray-500"
                        disabled
                    />
                ) : selectedFilterDef.type === 'select' ? (
                    <div className="relative">
                        <select
                            value={filter.valorTexto || ''}
                            onChange={(e) => handleValueChange(e.target.value)}
                            disabled={disabled}
                            className="w-full px-3 py-2 pr-8 border border-separator rounded-lg focus:ring-2 focus:ring-primary focus:outline-none appearance-none disabled:bg-gray-50 disabled:text-gray-500"
                            required
                        >
                            <option value="">Selecciona...</option>
                            {selectedFilterDef.options?.map(opt => (
                                <option key={opt} value={opt}>{opt}</option>
                            ))}
                        </select>
                        <span className="material-symbols-outlined absolute right-2 top-1/2 -translate-y-1/2 pointer-events-none text-gray-400">
                            arrow_drop_down
                        </span>
                    </div>
                ) : (
                    <input
                        type={selectedFilterDef.type === 'number' ? 'number' : 'text'}
                        value={filter.valorTexto || ''}
                        onChange={(e) => handleValueChange(e.target.value)}
                        disabled={disabled}
                        placeholder={selectedFilterDef.type === 'number' ? 'Ej: 30' : 'Ingresa valor...'}
                        className="w-full px-3 py-2 border border-separator rounded-lg focus:ring-2 focus:ring-primary focus:outline-none disabled:bg-gray-50 disabled:text-gray-500"
                        required
                    />
                )}
            </div>
        </div>
    );
};
