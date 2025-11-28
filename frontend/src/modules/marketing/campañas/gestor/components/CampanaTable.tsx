import React from 'react';
import { CampanaListItem } from '../types/campana.types';
import { CampanaStatusBadge } from './CampanaStatusBadge';
import { CampanaPriorityBadge } from './CampanaPriorityBadge';

interface CampanaTableProps {
    campanas: CampanaListItem[];
    isLoading: boolean;
    onEdit?: (id: number) => void;
    onDelete?: (id: number) => void;
}

export const CampanaTable: React.FC<CampanaTableProps> = ({
    campanas,
    isLoading,
    onEdit,
    onDelete,
}) => {
    if (isLoading) {
        return (
            <div className="flex items-center justify-center p-12">
                <div className="text-center">
                    <div className="inline-block h-8 w-8 animate-spin rounded-full border-4 border-solid border-primary border-r-transparent"></div>
                    <p className="mt-4 text-gray-600">Cargando campañas...</p>
                </div>
            </div>
        );
    }

    if (campanas.length === 0) {
        return (
            <div className="bg-gray-50 border border-separator rounded-lg p-8 text-center">
                <span className="material-symbols-outlined mx-auto block h-12 w-12 text-gray-400 text-5xl">
                    campaign
                </span>
                <h3 className="mt-2 text-sm font-medium text-gray-900">No se encontraron campañas</h3>
                <p className="mt-1 text-sm text-gray-500">
                    Intenta ajustar los filtros o crea una nueva campaña.
                </p>
            </div>
        );
    }

    const formatFechaRange = (inicio: string | null, fin: string | null): string => {
        if (!inicio || !fin) {
            return 'Sin programar';
        }

        const formatDate = (isoDate: string) => {
            const date = new Date(isoDate);
            return date.toLocaleDateString('es-ES', {
                day: '2-digit',
                month: '2-digit',
                year: 'numeric',
            });
        };

        return `${formatDate(inicio)} - ${formatDate(fin)}`;
    };

    return (
        <div className="overflow-x-auto">
            <table className="w-full text-left">
                <thead className="bg-table-header">
                    <tr>
                        <th className="p-4 text-sm font-semibold text-dark tracking-wide">
                            Nombre Campaña
                        </th>
                        <th className="p-4 text-sm font-semibold text-dark tracking-wide">Prioridad</th>
                        <th className="p-4 text-sm font-semibold text-dark tracking-wide">Estado</th>
                        <th className="p-4 text-sm font-semibold text-dark tracking-wide">
                            Canal Ejecución
                        </th>
                        <th className="p-4 text-sm font-semibold text-dark tracking-wide">
                            Fecha Inicio/Fin
                        </th>
                        <th className="p-4 text-sm font-semibold text-dark tracking-wide">Acciones</th>
                    </tr>
                </thead>
                <tbody className="divide-y divide-separator">
                    {campanas.map((campana) => (
                        <tr key={campana.idCampana} className="hover:bg-gray-50 transition-colors">
                            <td className="p-4 font-medium text-dark whitespace-nowrap">
                                {campana.nombre}
                            </td>
                            <td className="p-4 whitespace-nowrap">
                                <CampanaPriorityBadge prioridad={campana.prioridad} />
                            </td>
                            <td className="p-4 whitespace-nowrap">
                                <CampanaStatusBadge estado={campana.estado} />
                            </td>
                            <td className="p-4 text-gray-600 whitespace-nowrap">
                                {campana.canalEjecucion}
                            </td>
                            <td className="p-4 text-gray-600 whitespace-nowrap">
                                {formatFechaRange(campana.fechaProgramadaInicio, campana.fechaProgramadaFin)}
                            </td>
                            <td className="p-4 whitespace-nowrap">
                                <div className="flex items-center gap-2">
                                    {onEdit && (
                                        <button
                                            onClick={() => onEdit(campana.idCampana)}
                                            className="p-1.5 text-primary hover:bg-blue-50 rounded transition-colors"
                                            title="Ver detalles"
                                        >
                                            <span className="material-symbols-outlined text-xl">visibility</span>
                                        </button>
                                    )}
                                    {onDelete && (
                                        <button
                                            onClick={() => campana.estado === 'Borrador' && onDelete(campana.idCampana)}
                                            className={`p-1.5 rounded transition-colors ${campana.estado === 'Borrador'
                                                ? 'text-red-600 hover:bg-red-50 cursor-pointer'
                                                : 'text-gray-400 cursor-not-allowed'
                                                }`}
                                            title={campana.estado === 'Borrador' ? 'Eliminar campaña' : 'Solo se pueden eliminar campañas en Borrador'}
                                            disabled={campana.estado !== 'Borrador'}
                                        >
                                            <span className="material-symbols-outlined text-xl">delete</span>
                                        </button>
                                    )}
                                </div>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
};
