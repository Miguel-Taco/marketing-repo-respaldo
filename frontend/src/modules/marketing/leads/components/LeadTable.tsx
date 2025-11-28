import React from 'react';
import { Lead } from '../types/lead.types';

interface LeadTableProps {
  leads: Lead[];
  isLoading: boolean;
  onViewDetail: (id: number) => void;
  selectedIds: number[];
  onSelectionChange: (ids: number[]) => void;
  onDelete: (id: number) => void;
}

export const LeadTable: React.FC<LeadTableProps> = ({
  leads,
  isLoading,
  onViewDetail,
  selectedIds,
  onSelectionChange,
  onDelete
}) => {
  if (isLoading) {
    return (
      <div className="flex items-center justify-center p-12">
        <div className="text-center">
          <div className="inline-block h-8 w-8 animate-spin rounded-full border-4 border-solid border-primary border-r-transparent"></div>
          <p className="mt-4 text-gray-600">Cargando leads...</p>
        </div>
      </div>
    );
  }

  if (leads.length === 0) {
    return (
      <div className="bg-gray-50 border border-separator rounded-lg p-8 text-center">
        <span className="material-symbols-outlined mx-auto block h-12 w-12 text-gray-400 text-5xl">person_search</span>
        <h3 className="mt-2 text-sm font-medium text-gray-900">No se encontraron leads</h3>
        <p className="mt-1 text-sm text-gray-500">Intenta ajustar los filtros o crea un nuevo lead.</p>
      </div>
    );
  }

  // Manejadores de selección
  const handleSelectAll = () => {
    if (selectedIds.length === leads.length) {
      onSelectionChange([]);
    } else {
      onSelectionChange(leads.map(lead => lead.id));
    }
  };

  const handleSelectOne = (id: number) => {
    if (selectedIds.includes(id)) {
      onSelectionChange(selectedIds.filter(selectedId => selectedId !== id));
    } else {
      onSelectionChange([...selectedIds, id]);
    }
  };

  const getSourceLabel = (lead: Lead) => {
    if (lead.fuenteTipo === 'WEB') return 'Formulario Interno';
    if (lead.fuenteTipo === 'IMPORTACION') return 'Importación Masiva';
    return 'Desconocido';
  };

  const getTrackingInfo = (lead: Lead) => {
    if (lead.tracking?.campaign) return lead.tracking.campaign;
    if (lead.registroImportadoId) return `LOTE_${lead.registroImportadoId}`;
    return 'Sin tracking';
  };

  return (
    <div className="overflow-x-auto">
      <table className="w-full text-left">
        <thead className="bg-table-header">
          <tr>
            <th className="p-4 w-12">
              <input
                type="checkbox"
                checked={selectedIds.length === leads.length && leads.length > 0}
                onChange={handleSelectAll}
                className="w-4 h-4 text-primary bg-white border-gray-300 rounded focus:ring-primary focus:ring-2 cursor-pointer"
              />
            </th>
            <th className="p-4 text-sm font-semibold text-dark tracking-wide">Nombre / Email</th>
            <th className="p-4 text-sm font-semibold text-dark tracking-wide">Estado</th>
            <th className="p-4 text-sm font-semibold text-dark tracking-wide">Origen (Fuente)</th>
            <th className="p-4 text-sm font-semibold text-dark tracking-wide">Fecha Creación</th>
            <th className="p-4 text-sm font-semibold text-dark tracking-wide">Acciones</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-separator">
          {leads.map((lead) => (
            <tr key={lead.id} className="hover:bg-gray-50 transition-colors">
              <td className="p-4 whitespace-nowrap">
                <input
                  type="checkbox"
                  checked={selectedIds.includes(lead.id)}
                  onChange={() => handleSelectOne(lead.id)}
                  className="w-4 h-4 text-primary bg-white border-gray-300 rounded focus:ring-primary focus:ring-2 cursor-pointer"
                />
              </td>
              <td className="p-4 whitespace-nowrap">
                <div className="font-medium text-dark">{lead.nombreCompleto}</div>
                <div className="text-sm text-gray-500">{lead.contacto.email}</div>
              </td>
              <td className="p-4 whitespace-nowrap">
                <span className={`chip ${lead.estado === 'CALIFICADO' ? 'bg-success-chip text-success-chip' :
                  lead.estado === 'NUEVO' ? 'bg-default-chip text-default-chip' :
                    'bg-warn-chip text-warn-chip'
                  }`}>
                  {lead.estado}
                </span>
              </td>
              <td className="p-4 whitespace-nowrap">
                <div className="text-dark">{getSourceLabel(lead)}</div>
                <div className="text-sm text-gray-500">Tracking: {getTrackingInfo(lead)}</div>
              </td>
              <td className="p-4 whitespace-nowrap text-gray-600">
                {new Date(lead.fechaCreacion).toLocaleDateString('es-ES', {
                  day: 'numeric',
                  month: 'short',
                  year: 'numeric'
                })}
              </td>
              <td className="p-4 whitespace-nowrap">
                <div className="flex items-center gap-2">
                  <button
                    onClick={() => onViewDetail(lead.id)}
                    className="font-medium text-primary hover:underline focus:outline-none"
                  >
                    Ver Detalle
                  </button>
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      onDelete(lead.id);
                    }}
                    className="p-1.5 text-red-600 hover:bg-red-50 rounded transition-colors focus:outline-none"
                    title="Eliminar lead"
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
