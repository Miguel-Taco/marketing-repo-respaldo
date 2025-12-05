import React, { useState } from 'react';

export type PriorityFilter = 'todas' | 'Alta' | 'Media' | 'Baja';

interface MailingFiltersProps {
    isOpen: boolean;
    onClose: () => void;
    onApply: (filters: FilterValues) => void;
}

export interface FilterValues {
    nombre: string;
    fechaInicio: string;
    prioridad: PriorityFilter;
}

export const MailingFilters: React.FC<MailingFiltersProps> = ({ 
    isOpen, 
    onClose,
    onApply
}) => {
    const [filters, setFilters] = useState<FilterValues>({
        nombre: '',
        fechaInicio: '',
        prioridad: 'todas'
    });

    const handleApply = () => {
        onApply(filters);
        onClose();
    };

    const handleReset = () => {
        setFilters({
            nombre: '',
            fechaInicio: '',
            prioridad: 'todas'
        });
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm">
            <div className="bg-white rounded-xl shadow-2xl w-full max-w-md animate-in fade-in zoom-in duration-200">
                
                {/* Header */}
                <div className="flex justify-between items-center p-5 border-b border-separator">
                    <h3 className="text-lg font-bold text-dark">Filtrar Campañas</h3>
                    <button 
                        onClick={onClose} 
                        className="text-gray-400 hover:text-gray-600 transition-colors"
                    >
                        <span className="material-symbols-outlined">close</span>
                    </button>
                </div>

                {/* Body */}
                <div className="p-6 space-y-4">
                    {/* Filtro por Nombre */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Nombre de Campaña
                        </label>
                        <input
                            type="text"
                            placeholder="Buscar por nombre..."
                            value={filters.nombre}
                            onChange={(e) => setFilters({ ...filters, nombre: e.target.value })}
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                        />
                    </div>

                    {/* Filtro por Fecha */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Fecha de Inicio
                        </label>
                        <input
                            type="date"
                            value={filters.fechaInicio}
                            onChange={(e) => setFilters({ ...filters, fechaInicio: e.target.value })}
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                        />
                    </div>

                    {/* Filtro por Prioridad */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Prioridad
                        </label>
                        <select
                            value={filters.prioridad}
                            onChange={(e) => setFilters({ ...filters, prioridad: e.target.value as PriorityFilter })}
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                        >
                            <option value="todas">Todas las prioridades</option>
                            <option value="Alta">Alta</option>
                            <option value="Media">Media</option>
                            <option value="Baja">Baja</option>
                        </select>
                    </div>

                    {/* Botón Limpiar */}
                    <button
                        onClick={handleReset}
                        className="w-full px-4 py-2 text-gray-700 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors font-medium text-sm"
                    >
                        Limpiar Filtros
                    </button>
                </div>

                {/* Footer */}
                <div className="flex justify-end gap-3 p-5 bg-gray-50 rounded-b-xl border-t border-separator">
                    <button
                        onClick={onClose}
                        className="px-4 py-2 text-gray-700 border border-gray-300 rounded-lg hover:bg-gray-100 transition-colors font-medium"
                    >
                        Cancelar
                    </button>
                    <button
                        onClick={handleApply}
                        className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-medium"
                    >
                        Aplicar Filtros
                    </button>
                </div>
            </div>
        </div>
    );
};