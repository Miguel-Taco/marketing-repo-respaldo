import React from 'react';
import { Lead } from '../types/lead.types';

export const LeadInfoCards: React.FC<{ lead: Lead }> = ({ lead }) => {
    return (
        <div className="space-y-6">
            {/* Datos de Contacto */}
            <div className="bg-white rounded-lg shadow-card border border-separator p-5">
                <h3 className="text-lg font-semibold text-dark border-b border-separator pb-4 mb-4">Datos de Contacto</h3>
                <dl className="space-y-3">
                    <div>
                        <dt className="text-sm font-medium text-gray-500">Email</dt>
                        <dd className="font-semibold text-dark">{lead.contacto.email}</dd>
                    </div>
                    <div>
                        <dt className="text-sm font-medium text-gray-500">Teléfono</dt>
                        <dd className="font-semibold text-dark">{lead.contacto.telefono}</dd>
                    </div>
                    <div>
                        <dt className="text-sm font-medium text-gray-500">Ubicación</dt>
                        <dd className="font-semibold text-dark">
                            {lead.demograficos?.distritoNombre ? (
                                <>
                                    {lead.demograficos.distritoNombre}
                                    <span className="block text-xs text-gray-500 font-normal">
                                        {lead.demograficos.provinciaNombre}, {lead.demograficos.departamentoNombre}
                                    </span>
                                </>
                            ) : (
                                <span className="text-gray-400">No especificado</span>
                            )}
                        </dd>
                    </div>
                </dl>
            </div>

            {/* Datos Demográficos */}
            <div className="bg-white rounded-lg shadow-card border border-separator p-5">
                <h3 className="text-lg font-semibold text-dark border-b border-separator pb-4 mb-4">Datos Demográficos</h3>
                <dl className="space-y-3">
                    <div>
                        <dt className="text-sm font-medium text-gray-500">Edad</dt>
                        <dd className="font-semibold text-dark">
                            {lead.demograficos?.edad ? (
                                `${lead.demograficos.edad} años`
                            ) : (
                                <span className="text-gray-400">No especificado</span>
                            )}
                        </dd>
                    </div>
                    <div>
                        <dt className="text-sm font-medium text-gray-500">Género</dt>
                        <dd className="font-semibold text-dark">
                            {lead.demograficos?.genero ? (
                                lead.demograficos.genero
                            ) : (
                                <span className="text-gray-400">No especificado</span>
                            )}
                        </dd>
                    </div>
                </dl>
            </div>

            {/* Tracking */}
            <div className="bg-white rounded-lg shadow-card border border-separator p-5">
                <h3 className="text-lg font-semibold text-dark border-b border-separator pb-4 mb-4">Tracking de Origen</h3>
                <dl className="space-y-3">
                    <div>
                        <dt className="text-sm font-medium text-gray-500">Fuente (Source)</dt>
                        <dd className="font-semibold text-dark">{lead.tracking?.source || 'Desconocido'}</dd>
                    </div>
                    <div>
                        <dt className="text-sm font-medium text-gray-500">Campaña</dt>
                        <dd className="font-semibold text-dark">{lead.tracking?.campaign || '-'}</dd>
                    </div>
                </dl>
            </div>
        </div>
    );
};
