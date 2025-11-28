import React from 'react';
import { FilterRow, FilterDefinition } from './FilterRow';

interface FilterBuilderProps {
    filters: FilterDefinition[];
    onFiltersChange: (filters: FilterDefinition[]) => void;
    onPreview?: () => void;
    isPreviewLoading?: boolean;
    disabled?: boolean;
}

export const FilterBuilder: React.FC<FilterBuilderProps> = ({
    filters,
    onFiltersChange,
    onPreview,
    isPreviewLoading = false,
    disabled = false
}) => {
    const handleAddFilter = () => {
        onFiltersChange([...filters, { campo: '', operador: 'IGUAL', valorTexto: '' }]);
    };

    const handleUpdateFilter = (index: number, updatedFilter: FilterDefinition) => {
        const newFilters = [...filters];
        newFilters[index] = updatedFilter;
        onFiltersChange(newFilters);
    };

    const handleRemoveFilter = (index: number) => {
        onFiltersChange(filters.filter((_, i) => i !== index));
    };

    return (
        <div className="bg-white p-6 rounded-xl shadow-lg border border-separator">
            <h2 className="text-xl font-semibold text-dark mb-4">Filtros de Segmento</h2>
            <p className="text-sm text-gray-500 mb-4">
                Se aplicarán todas las condiciones agregadas (operador "Y" implícito).
            </p>

            {/* Filter Rows Container */}
            <div className="space-y-2 mb-4">
                {filters.length === 0 ? (
                    <div className="text-center py-8 text-gray-400">
                        <span className="material-symbols-outlined text-4xl mb-2 block">filter_alt_off</span>
                        <p className="text-sm">No hay filtros agregados. Haz clic en "Agregar Filtro" para comenzar.</p>
                    </div>
                ) : (
                    filters.map((filter, index) => (
                        <FilterRow
                            key={index}
                            filter={filter}
                            index={index}
                            showLogicOperator={index > 0}
                            onUpdate={handleUpdateFilter}
                            onRemove={handleRemoveFilter}
                            disabled={disabled}
                        />
                    ))
                )}
            </div>

            {/* Add Filter Button */}
            <div className="flex gap-3 pt-4">
                <button
                    type="button"
                    onClick={handleAddFilter}
                    disabled={disabled}
                    className="flex items-center gap-2 px-4 py-2 bg-blue-50 text-primary font-semibold rounded-lg hover:bg-blue-100 transition-colors shadow-sm disabled:opacity-50 disabled:cursor-not-allowed"
                >
                    <span className="material-symbols-outlined text-lg">add</span>
                    Agregar Filtro
                </button>

                {/* Preview/Filter Button */}
                {onPreview && (
                    <button
                        type="button"
                        onClick={onPreview}
                        disabled={isPreviewLoading || disabled}
                        className="flex items-center gap-2 px-4 py-2 bg-primary text-white font-semibold rounded-lg hover:bg-primary-hover transition-colors shadow-sm disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                        {isPreviewLoading ? (
                            <>
                                <span className="animate-spin h-4 w-4 border-2 border-current border-t-transparent rounded-full" />
                                Filtrando...
                            </>
                        ) : (
                            <>
                                <span className="material-symbols-outlined text-lg">filter_alt</span>
                                Filtrar
                            </>
                        )}
                    </button>
                )}
            </div>
        </div>
    );
};
