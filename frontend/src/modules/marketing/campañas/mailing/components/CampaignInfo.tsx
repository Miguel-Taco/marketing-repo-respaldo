import React, { useState } from 'react';
import { CampanaMailing, ESTADO_LABELS, PRIORIDAD_COLORS } from '../types/mailing.types';
import { Badge } from '../../../../../shared/components/ui/Badge';

interface CampaignInfoProps {
    campaign: CampanaMailing;
    onCtaTextChange?: (text: string) => void;
}

export const CampaignInfo: React.FC<CampaignInfoProps> = ({ campaign, onCtaTextChange }) => {
    const [ctaText, setCtaText] = useState(campaign.ctaTexto || '');

    const handleCtaTextChange = (value: string) => {
        setCtaText(value);
        onCtaTextChange?.(value);
    };
    const formatDate = (dateString: string) => {
        const date = new Date(dateString);
        return date.toLocaleDateString('es-ES', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit'
        });
    };

    return (
        <div className="space-y-6">
            {/* Información de la Campaña */}
            <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-100">
                <h3 className="text-lg font-semibold text-dark mb-4">Información de la Campaña</h3>
                
                <div className="space-y-4">
                    <div>
                        <p className="text-sm text-gray-600">Descripción</p>
                        <p className="text-gray-800">{campaign.descripcion || '-'}</p>
                    </div>
                    
                    <div>
                        <p className="text-sm text-gray-600">Prioridad</p>
                        <div className={`inline-block px-3 py-1 rounded-full text-sm font-medium ${PRIORIDAD_COLORS[campaign.prioridad] || 'bg-gray-100 text-gray-700'}`}>
                            {campaign.prioridad}
                        </div>
                    </div>
                    
                    <div>
                        <p className="text-sm text-gray-600">Fecha de Inicio</p>
                        <p className="text-gray-800">{formatDate(campaign.fechaInicio)}</p>
                    </div>
                </div>
            </div>

            {/* Datos Adicionales */}
            <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-100">
                <h3 className="text-lg font-semibold text-dark mb-4">Datos Adicionales</h3>
                
                <div className="space-y-4">
                    <div>
                        <p className="text-sm text-gray-600">Encuesta Asignada</p>
                        <p className="text-gray-800">{campaign.nombreEncuesta}</p>
                    </div>
                    
                    <div>
                        <p className="text-sm text-gray-600">Texto del Botón CTA</p>
                        <input
                            type="text"
                            value={ctaText}
                            onChange={(e) => handleCtaTextChange(e.target.value)}
                            placeholder="¡ME INTERESA!"
                            className="w-full px-4 py-2 border border-gray-200 rounded-lg focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary/20"
                        />
                    </div>
                </div>
            </div>

            {/* Estado de Preparación */}
            <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-100">
                <h3 className="text-lg font-semibold text-dark mb-4">Estado de Preparación</h3>
                <Badge variant="default">
                    {ESTADO_LABELS[campaign.idEstado]}
                </Badge>
            </div>
        </div>
    );
};
