import React, { useState, useEffect } from 'react';
import { CanalEjecucion, Prioridad } from '../types/campana.types';
import { encuestasApi } from '../services/encuestas.api';

interface Step1BasicDataProps {
    formData: {
        nombre: string;
        tematica: string;
        descripcion: string;
        prioridad: Prioridad | '';
        canalEjecucion: CanalEjecucion | '';
        idEncuesta: number | undefined;
    };
    onChange: (field: string, value: any) => void;
    errors: Record<string, string>;
}

export const Step1BasicData: React.FC<Step1BasicDataProps> = ({ formData, onChange, errors }) => {
    const [encuestaOptions, setEncuestaOptions] = useState<Array<{ value: number; label: string }>>([]);
    const [loadingEncuestas, setLoadingEncuestas] = useState(false);

    // Load encuestas on mount
    useEffect(() => {
        const loadEncuestas = async () => {
            setLoadingEncuestas(true);
            try {
                const encuestas = await encuestasApi.getDisponibles();
                setEncuestaOptions(encuestas.map(e => ({ value: e.idEncuesta, label: e.titulo })));
            } catch (error) {
                console.error('Error loading surveys:', error);
            } finally {
                setLoadingEncuestas(false);
            }
        };

        loadEncuestas();
    }, []);

    return (
        <div className="space-y-6">
            {/* Grid de campos */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {/* Nombre */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                        Nombre de la campaña<span className="text-red-500">*</span>
                    </label>
                    <input
                        type="text"
                        value={formData.nombre}
                        onChange={(e) => onChange('nombre', e.target.value)}
                        placeholder="Ej: Campaña de Verano 2024"
                        className={`w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-primary focus:outline-none ${errors.nombre ? 'border-red-500' : 'border-separator'
                            }`}
                    />
                    {errors.nombre && (
                        <p className="mt-1 text-sm text-red-500">{errors.nombre}</p>
                    )}
                </div>

                {/* Temática */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                        Temática<span className="text-red-500">*</span>
                    </label>
                    <input
                        type="text"
                        value={formData.tematica}
                        onChange={(e) => onChange('tematica', e.target.value)}
                        placeholder="Ej: Promociones"
                        className={`w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-primary focus:outline-none ${errors.tematica ? 'border-red-500' : 'border-separator'
                            }`}
                    />
                    {errors.tematica && (
                        <p className="mt-1 text-sm text-red-500">{errors.tematica}</p>
                    )}
                </div>

                {/* Descripción - Full width */}
                <div className="md:col-span-2">
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                        Descripción
                    </label>
                    <textarea
                        value={formData.descripcion}
                        onChange={(e) => onChange('descripcion', e.target.value)}
                        placeholder="Describe brevemente el objetivo de esta campaña..."
                        rows={3}
                        className={`w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-primary focus:outline-none resize-y ${errors.descripcion ? 'border-red-500' : 'border-separator'
                            }`}
                    />
                    {errors.descripcion && (
                        <p className="mt-1 text-sm text-red-500">{errors.descripcion}</p>
                    )}
                </div>

                {/* Canal de Ejecución */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                        Canal de Ejecución<span className="text-red-500">*</span>
                    </label>
                    <select
                        value={formData.canalEjecucion}
                        onChange={(e) => onChange('canalEjecucion', e.target.value as CanalEjecucion)}
                        className={`w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-primary focus:outline-none ${errors.canalEjecucion ? 'border-red-500' : 'border-separator'
                            }`}
                    >
                        <option value="">Seleccionar canal...</option>
                        <option value="Mailing">Mailing</option>
                        <option value="Llamadas">Llamadas</option>
                    </select>
                    {errors.canalEjecucion && (
                        <p className="mt-1 text-sm text-red-500">{errors.canalEjecucion}</p>
                    )}
                </div>

                {/* Prioridad */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                        Prioridad<span className="text-red-500">*</span>
                    </label>
                    <select
                        value={formData.prioridad}
                        onChange={(e) => onChange('prioridad', e.target.value as Prioridad)}
                        className={`w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-primary focus:outline-none ${errors.prioridad ? 'border-red-500' : 'border-separator'
                            }`}
                    >
                        <option value="">Seleccionar prioridad...</option>
                        <option value="Baja">Baja</option>
                        <option value="Media">Media</option>
                        <option value="Alta">Alta</option>
                    </select>
                    {errors.prioridad && (
                        <p className="mt-1 text-sm text-red-500">{errors.prioridad}</p>
                    )}
                </div>

                {/* Asignar Encuesta - Solo mitad del ancho */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                        Asignar Encuesta
                    </label>
                    <select
                        value={formData.idEncuesta || ''}
                        onChange={(e) => onChange('idEncuesta', e.target.value ? parseInt(e.target.value) : undefined)}
                        className={`w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-primary focus:outline-none ${errors.idEncuesta ? 'border-red-500' : 'border-separator'
                            }`}
                        disabled={loadingEncuestas}
                    >
                        <option value="">
                            {loadingEncuestas ? 'Cargando encuestas...' : 'Seleccionar encuesta (opcional)'}
                        </option>
                        {encuestaOptions.map(option => (
                            <option key={option.value} value={option.value}>
                                {option.label}
                            </option>
                        ))}
                    </select>
                    {errors.idEncuesta && (
                        <p className="mt-1 text-sm text-red-500">{errors.idEncuesta}</p>
                    )}
                </div>
            </div>
        </div>
    );
};
