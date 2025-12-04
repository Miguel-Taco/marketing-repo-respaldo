import React from 'react';
import { Segmento } from '../types/segmentacion.types';

interface SegmentTableProps {
    segmentos: Segmento[];
    isLoading: boolean;
    onEdit: (id: number) => void;
    selectedIds: number[];
    onSelectionChange: (ids: number[]) => void;
    onDelete: (id: number) => void;
}

export const SegmentTable: React.FC<SegmentTableProps> = ({
    segmentos,
    isLoading,
    onEdit,
    selectedIds,
    onSelectionChange,
    onDelete
}) => {
    if (isLoading) {
        return (
            <div className="flex items-center justify-center p-12">
                <div className="text-center">
                    <div className="inline-block h-8 w-8 animate-spin rounded-full border-4 border-solid border-primary border-r-transparent"></div>
                    <p className="mt-4 text-gray-600">Cargando segmentos...</p>
                </div>
            </div>
        );
    }

    if (segmentos.length === 0) {
        return (
            <div className="bg-gray-50 border border-separator rounded-lg p-8 text-center">
                <span className="material-symbols-outlined mx-auto block h-12 w-12 text-gray-400 text-5xl">group</span>
                <h3 className="mt-2 text-sm font-medium text-gray-900">No se encontraron segmentos</h3>
                <p className="mt-1 text-sm text-gray-500">Intenta ajustar los filtros o crea un nuevo segmento.</p>
            </div>
        );
    }

    // Selection handlers
    const handleSelectAll = () => {
        if (selectedIds.length === segmentos.length) {
            onSelectionChange([]);
        } else {
            onSelectionChange(segmentos.map(s => s.id));
        }
    };

    const handleSelectOne = (id: number) => {
        if (selectedIds.includes(id)) {
            onSelectionChange(selectedIds.filter(selectedId => selectedId !== id));
        } else {
            onSelectionChange([...selectedIds, id]);
        }
    };

    const getEstadoBadge = (estado: string) => {
        if (estado === 'ACTIVO') {
            return <span className="chip bg-success-chip text-success-chip">● Activo</span>;
        }
        if (estado === 'ELIMINADO') {
            return <span className="chip bg-gray-200 text-gray-600">✕ Eliminado</span>;
        }
        return <span className="chip bg-red-100 text-red-700">○ Inactivo</span>;
    };

    const getTipoAudienciaLabel = (tipo: string) => {
        const labels: Record<string, string> = {
            'LEAD': 'Lead',
            'CLIENTE': 'Cliente',
            'MIXTO': 'Mixto'
        };
        return labels[tipo] || tipo;
    };

    return (
        <div className="overflow-x-auto">
            <table className="w-full text-left">
                <thead className="bg-table-header">
                    <tr>
                        <th className="p-4 w-12">
                            <input
                                type="checkbox"
                                checked={selectedIds.length === segmentos.length && segmentos.length > 0}
                                onChange={handleSelectAll}
                                className="w-4 h-4 text-primary bg-white border-gray-300 rounded focus:ring-primary focus:ring-2 cursor-pointer"
                            />
                        </th>
                        <th className="p-4 text-sm font-semibold text-dark tracking-wide">Nombre / Descripción</th>
                        <th className="p-4 text-sm font-semibold text-dark tracking-wide">Estado</th>
                        <th className="p-4 text-sm font-semibold text-dark tracking-wide">Tipo Audiencia</th>
                        <th className="p-4 text-sm font-semibold text-dark tracking-wide">Miembros</th>
                        <th className="p-4 text-sm font-semibold text-dark tracking-wide">Fecha Creación</th>
                        <th className="p-4 text-sm font-semibold text-dark tracking-wide">Acciones</th>
                    </tr>
                </thead>
                <tbody className="divide-y divide-separator">
                    {segmentos.map((segmento) => (
                        <tr key={segmento.id} className="hover:bg-gray-50 transition-colors">
                            <td className="p-4 whitespace-nowrap">
                                <input
                                    type="checkbox"
                                    checked={selectedIds.includes(segmento.id)}
                                    onChange={() => handleSelectOne(segmento.id)}
                                    className="w-4 h-4 text-primary bg-white border-gray-300 rounded focus:ring-primary focus:ring-2 cursor-pointer"
                                />
                            </td>
                            <td className="p-4 whitespace-nowrap">
                                <div className="font-medium text-dark">{segmento.nombre}</div>
                                <div className="text-sm text-gray-500">{segmento.descripcion || 'Sin descripción'}</div>
                            </td>
                            <td className="p-4 whitespace-nowrap">
                                {getEstadoBadge(segmento.estado)}
                            </td>
                            <td className="p-4 whitespace-nowrap text-gray-600">
                                {getTipoAudienciaLabel(segmento.tipoAudiencia)}
                            </td>
                            <td className="p-4 whitespace-nowrap text-gray-600">
                                {segmento.cantidadMiembros?.toLocaleString() || '0'}
                            </td>
                            <td className="p-4 whitespace-nowrap text-gray-600">
                                {new Date(segmento.fechaCreacion).toLocaleDateString('es-ES', {
                                    day: 'numeric',
                                    month: 'short',
                                    year: 'numeric'
                                })}
                            </td>
                            <td className="p-4 whitespace-nowrap">
                                <div className="flex items-center gap-2">
                                    <button
                                        onClick={() => onEdit(segmento.id)}
                                        className="font-medium text-primary hover:underline focus:outline-none"
                                    >
                                        Detalle/Edición
                                    </button>
                                    <button
                                        onClick={(e) => {
                                            e.stopPropagation();
                                            onDelete(segmento.id);
                                        }}
                                        className="p-1.5 text-red-600 hover:bg-red-50 rounded transition-colors focus:outline-none"
                                        title="Eliminar segmento"
                                    >
                                        <span className="material-symbols-outlined text-xl">delete</span>
                                    </button>
                                </div>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
};
