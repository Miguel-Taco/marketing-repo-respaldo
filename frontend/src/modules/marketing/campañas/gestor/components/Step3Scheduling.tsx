import React from 'react';
import { CanalEjecucion, Prioridad } from '../types/campana.types';

interface Step3SchedulingProps {
    formData: {
        nombre: string;
        tematica: string;
        descripcion: string;
        prioridad: Prioridad | '';
        canalEjecucion: CanalEjecucion | '';
        idEncuesta: number | undefined;
        idSegmento: number | undefined;
        fechaProgramadaInicio: string;
        fechaProgramadaFin: string;
        idAgente: number | undefined;
    };
    selectedSegmentName?: string;
    selectedSegmentSize?: number;
    onChange: (field: string, value: any) => void;
    errors: Record<string, string>;
}

export const Step3Scheduling: React.FC<Step3SchedulingProps> = ({
    formData,
    selectedSegmentName,
    selectedSegmentSize,
    onChange,
    errors
}) => {
    return (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* Left Column - Schedule Form */}
            <div className="lg:col-span-2 flex flex-col gap-6">
                <h3 className="text-lg font-semibold text-gray-900">Fecha y hora</h3>

                {/* Date and Time Inputs */}
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
                    {/* Fecha y hora de inicio */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            Fecha y hora de inicio<span className="text-red-500">*</span>
                        </label>
                        <div className="relative">
                            <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 text-lg">
                                calendar_today
                            </span>
                            <input
                                type="datetime-local"
                                value={formData.fechaProgramadaInicio}
                                onChange={(e) => onChange('fechaProgramadaInicio', e.target.value)}
                                className={`w-full pl-10 pr-4 py-2.5 border rounded-lg focus:ring-2 focus:ring-primary focus:outline-none text-sm ${errors.fechaProgramadaInicio ? 'border-red-500' : 'border-separator'
                                    }`}
                            />
                        </div>
                        {errors.fechaProgramadaInicio && (
                            <p className="mt-1 text-sm text-red-500">{errors.fechaProgramadaInicio}</p>
                        )}
                    </div>

                    {/* Fecha y hora de fin */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            Fecha y hora de fin<span className="text-red-500">*</span>
                        </label>
                        <div className="relative">
                            <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 text-lg">
                                event_busy
                            </span>
                            <input
                                type="datetime-local"
                                value={formData.fechaProgramadaFin}
                                onChange={(e) => onChange('fechaProgramadaFin', e.target.value)}
                                className={`w-full pl-10 pr-4 py-2.5 border rounded-lg focus:ring-2 focus:ring-primary focus:outline-none text-sm ${errors.fechaProgramadaFin ? 'border-red-500' : 'border-separator'
                                    }`}
                            />
                        </div>
                        {errors.fechaProgramadaFin && (
                            <p className="mt-1 text-sm text-red-500">{errors.fechaProgramadaFin}</p>
                        )}
                    </div>

                    {/* Agente Selection */}
                    <div className="sm:col-span-2">
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            Asignar Agente<span className="text-red-500">*</span>
                        </label>
                        <div className="relative">
                            <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 text-lg">
                                support_agent
                            </span>
                            <select
                                value={formData.idAgente || ''}
                                onChange={(e) => onChange('idAgente', Number(e.target.value))}
                                className={`w-full pl-10 pr-4 py-2.5 border rounded-lg focus:ring-2 focus:ring-primary focus:outline-none text-sm appearance-none bg-white ${errors.idAgente ? 'border-red-500' : 'border-separator'
                                    }`}
                            >
                                <option value="">Seleccione un agente...</option>
                                <option value="1">Juan Pérez (Agente Senior)</option>
                                <option value="2">María García (Agente Ventas)</option>
                                <option value="3">Carlos López (Soporte)</option>
                                <option value="4">Ana Martínez (Telemarketing)</option>
                            </select>
                            <span className="material-symbols-outlined absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 text-lg pointer-events-none">
                                expand_more
                            </span>
                        </div>
                        {errors.idAgente && (
                            <p className="mt-1 text-sm text-red-500">{errors.idAgente}</p>
                        )}
                    </div>
                </div>
            </div>

            {/* Right Column - Campaign Summary */}
            <div className="lg:col-span-1 flex flex-col gap-4">
                <div className="p-4 rounded-lg border border-gray-200 bg-gray-50">
                    <h4 className="text-base font-semibold text-gray-900 mb-4">Resumen de la Campaña</h4>
                    <ul className="space-y-3 text-sm">
                        <li className="flex justify-between items-start gap-2">
                            <span className="text-gray-500 min-w-[80px]">Nombre:</span>
                            <span className="font-medium text-gray-800 text-right">{formData.nombre || '-'}</span>
                        </li>
                        <li className="flex justify-between items-start gap-2">
                            <span className="text-gray-500 min-w-[80px]">Temática:</span>
                            <span className="font-medium text-gray-800 text-right">{formData.tematica || '-'}</span>
                        </li>
                        {formData.descripcion && (
                            <li className="flex flex-col gap-1">
                                <span className="text-gray-500">Descripción:</span>
                                <span className="font-medium text-gray-800 text-sm">{formData.descripcion}</span>
                            </li>
                        )}
                        <li className="flex justify-between items-start gap-2">
                            <span className="text-gray-500 min-w-[80px]">Canal:</span>
                            <span className="font-medium text-gray-800 text-right">{formData.canalEjecucion || '-'}</span>
                        </li>
                        <li className="flex justify-between items-start gap-2">
                            <span className="text-gray-500 min-w-[80px]">Prioridad:</span>
                            <span className={`font-medium text-right ${formData.prioridad === 'Alta' ? 'text-red-600' :
                                    formData.prioridad === 'Media' ? 'text-yellow-600' :
                                        formData.prioridad === 'Baja' ? 'text-green-600' :
                                            'text-gray-800'
                                }`}>
                                {formData.prioridad || '-'}
                            </span>
                        </li>
                        <li className="flex justify-between items-start gap-2">
                            <span className="text-gray-500 min-w-[80px]">Segmento:</span>
                            <span className="font-medium text-gray-800 text-right">{selectedSegmentName || '-'}</span>
                        </li>
                        {selectedSegmentSize !== undefined && (
                            <li className="flex justify-between items-start gap-2">
                                <span className="text-gray-500 min-w-[80px]">Tamaño:</span>
                                <span className="font-medium text-gray-800 text-right">{selectedSegmentSize.toLocaleString()} usuarios</span>
                            </li>
                        )}
                    </ul>
                </div>

                {/* Info note */}
                <div className="flex items-start gap-2 text-xs text-gray-600 bg-blue-50 p-3 rounded border border-blue-100">
                    <span className="material-symbols-outlined text-sm text-blue-600">info</span>
                    <p>La campaña se ejecutará automáticamente en la fecha y hora programadas.</p>
                </div>
            </div>
        </div>
    );
};
