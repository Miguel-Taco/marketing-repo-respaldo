import React, { useState } from 'react';
import { Modal } from '../../../../shared/components/ui/Modal';
import { Button } from '../../../../shared/components/ui/Button';
import { Input } from '../../../../shared/components/ui/Input';
import { Select } from '../../../../shared/components/ui/Select';
import { leadReportsApi } from '../services/lead-reports.api';
import { LeadReportFilterDTO } from '../types/lead.types';

interface LeadReportModalProps {
    isOpen: boolean;
    onClose: () => void;
}

type ReportType = 'GENERAL' | 'FUENTES' | 'CONVERSION' | 'TENDENCIAS' | 'DEMOGRAFICO';

export const LeadReportModal: React.FC<LeadReportModalProps> = ({ isOpen, onClose }) => {
    const [reportType, setReportType] = useState<ReportType>('GENERAL');
    const [loading, setLoading] = useState(false);
    const [filters, setFilters] = useState<LeadReportFilterDTO>({
        fechaInicio: '',
        fechaFin: '',
        estado: undefined,
        fuenteTipo: undefined
    });

    const handleGenerate = async () => {
        setLoading(true);
        try {
            // Limpiar filtros vacíos
            const activeFilters: LeadReportFilterDTO = {};
            if (filters.fechaInicio) activeFilters.fechaInicio = filters.fechaInicio;
            if (filters.fechaFin) activeFilters.fechaFin = filters.fechaFin;
            if (filters.estado) activeFilters.estado = filters.estado;
            if (filters.fuenteTipo) activeFilters.fuenteTipo = filters.fuenteTipo;

            switch (reportType) {
                case 'GENERAL':
                    await leadReportsApi.getGeneralReport(activeFilters);
                    break;
                case 'FUENTES':
                    await leadReportsApi.getSourceReport(activeFilters);
                    break;
                case 'CONVERSION':
                    await leadReportsApi.getConversionReport(activeFilters);
                    break;
                case 'TENDENCIAS':
                    await leadReportsApi.getTrendsReport(activeFilters);
                    break;
                case 'DEMOGRAFICO':
                    await leadReportsApi.getDemographicReport(activeFilters);
                    break;
            }
            onClose();
        } catch (error) {
            console.error("Error generando reporte", error);
            alert("Error al generar el reporte. Por favor intente nuevamente.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <Modal isOpen={isOpen} onClose={onClose} title="Generar Reporte de Leads">
            <div className="space-y-4">
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Tipo de Reporte</label>
                    <Select
                        value={reportType}
                        onChange={(e) => setReportType(e.target.value as ReportType)}
                        options={[
                            { value: 'GENERAL', label: 'Reporte General' },
                            { value: 'FUENTES', label: 'Análisis de Fuentes' },
                            { value: 'CONVERSION', label: 'Embudo de Conversión' },
                            { value: 'TENDENCIAS', label: 'Tendencias Temporales' },
                            { value: 'DEMOGRAFICO', label: 'Análisis Demográfico' }
                        ]}
                    />
                </div>

                <div className="grid grid-cols-2 gap-4">
                    <Input
                        type="date"
                        label="Fecha Inicio"
                        value={filters.fechaInicio || ''}
                        onChange={(e) => setFilters({ ...filters, fechaInicio: e.target.value })}
                    />
                    <Input
                        type="date"
                        label="Fecha Fin"
                        value={filters.fechaFin || ''}
                        onChange={(e) => setFilters({ ...filters, fechaFin: e.target.value })}
                    />
                </div>

                <div className="grid grid-cols-2 gap-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Estado</label>
                        <Select
                            value={filters.estado || ''}
                            onChange={(e) => setFilters({ ...filters, estado: e.target.value as any || undefined })}
                            options={[
                                { value: '', label: 'Todos' },
                                { value: 'NUEVO', label: 'Nuevo' },
                                { value: 'CALIFICADO', label: 'Calificado' },
                                { value: 'DESCARTADO', label: 'Descartado' }
                            ]}
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Fuente</label>
                        <Select
                            value={filters.fuenteTipo || ''}
                            onChange={(e) => setFilters({ ...filters, fuenteTipo: e.target.value as any || undefined })}
                            options={[
                                { value: '', label: 'Todas' },
                                { value: 'WEB', label: 'Web' },
                                { value: 'IMPORTACION', label: 'Importación' }
                            ]}
                        />
                    </div>
                </div>

                <div className="flex justify-end gap-3 mt-6">
                    <Button variant="secondary" onClick={onClose} disabled={loading}>
                        Cancelar
                    </Button>
                    <Button onClick={handleGenerate} isLoading={loading}>
                        {loading ? 'Generando...' : 'Descargar PDF'}
                    </Button>
                </div>
            </div>
        </Modal>
    );
};
