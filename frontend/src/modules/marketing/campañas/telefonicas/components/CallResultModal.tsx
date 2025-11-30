import React, { useState } from 'react';
import type { ResultadoLlamadaRequest } from '../types';
import { Button } from '../../../../../shared/components/ui/Button';

interface CallResultModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSave: (data: ResultadoLlamadaRequest, abrirSiguiente: boolean) => void;
    idContacto: number;
    duracionSegundos: number;
    autoNext?: boolean;
}

const formatDuration = (seconds: number): string => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return mins > 0 ? `${mins}m ${secs}s` : `${secs}s`;
};

const RESULTADOS = [
    { id: 'CONTACTADO', label: 'Contactado' },
    { id: 'NO_CONTESTA', label: 'No contesta' },
    { id: 'BUZON', label: 'Buzón de voz' },
    { id: 'NO_INTERESADO', label: 'No interesado' },
    { id: 'INTERESADO', label: 'Interesado' },
    { id: 'VENTA', label: 'Venta' }
];

export const CallResultModal: React.FC<CallResultModalProps> = ({
    isOpen,
    onClose,
    onSave,
    idContacto,
    duracionSegundos,
    autoNext = false
}) => {
    const [resultado, setResultado] = useState('');
    const [motivo, setMotivo] = useState('');
    const [notas, setNotas] = useState('');
    const [reagendar, setReagendar] = useState(false);
    const [fechaReagendamiento, setFechaReagendamiento] = useState('');
    const [horaReagendamiento, setHoraReagendamiento] = useState('');
    const [derivadoVentas, setDerivadoVentas] = useState(false);
    const [crearOportunidad, setCrearOportunidad] = useState(false);
    const [tipoOportunidad, setTipoOportunidad] = useState('VENTA_NUEVA');

    if (!isOpen) return null;

    const handleSubmit = (abrirSiguiente: boolean = false) => {
        if (!resultado) {
            alert('Debe seleccionar un resultado para la llamada');
            return;
        }

        const data: ResultadoLlamadaRequest = {
            idContacto,
            resultado,
            motivo: motivo || undefined,
            notas: notas || undefined,
            fechaReagendamiento: (resultado === 'CONTACTADO' || resultado === 'INTERESADO' || resultado === 'BUZON') && fechaReagendamiento && horaReagendamiento
                ? `${fechaReagendamiento}T${horaReagendamiento}:00`
                : undefined,
            derivadoVentas: crearOportunidad || undefined,
            tipoOportunidad: crearOportunidad ? tipoOportunidad : undefined,
            duracionSegundos: duracionSegundos || 0
        };
        onSave(data, abrirSiguiente);
    };

    return (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-lg shadow-xl w-full max-w-2xl max-h-[90vh] overflow-y-auto">
                <div className="p-6 border-b border-gray-200 flex justify-between items-center">
                    <h2 className="text-xl font-bold text-gray-900">Registrar Resultado de Llamada</h2>
                    <button onClick={onClose} className="text-gray-500 hover:text-gray-700">
                        <span className="material-symbols-outlined">close</span>
                    </button>
                </div>

                <div className="p-6 space-y-6">
                    {/* Duration Display */}
                    <div className="p-4 bg-blue-50 rounded-lg border border-blue-100">
                        <div className="flex items-center gap-2">
                            <span className="material-symbols-outlined text-blue-600">schedule</span>
                            <div>
                                <p className="text-sm font-medium text-blue-900">Duración de la llamada</p>
                                <p className="text-lg font-bold text-blue-700">{formatDuration(duracionSegundos)}</p>
                            </div>
                        </div>
                    </div>

                    {/* Resultado Principal */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">Resultado de la llamada</label>
                        <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
                            {RESULTADOS.map((res) => (
                                <button
                                    key={res.id}
                                    type="button"
                                    className={`p-3 rounded-lg border text-left transition-all ${resultado === res.id
                                        ? 'border-primary bg-primary/5 ring-1 ring-primary'
                                        : 'border-gray-200 hover:border-gray-300 hover:bg-gray-50'
                                        }`}
                                    onClick={() => setResultado(res.id)}
                                >
                                    <div className={`font-semibold ${resultado === res.id ? 'text-primary' : 'text-gray-900'}`}>
                                        {res.label}
                                    </div>
                                </button>
                            ))}
                        </div>
                    </div>

                    {/* Notas */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">Notas de la llamada</label>
                        <textarea
                            className="w-full rounded-lg border-gray-300 focus:border-primary focus:ring-primary min-h-[100px]"
                            placeholder="Describe los detalles importantes de la conversación..."
                            value={notas}
                            onChange={(e) => setNotas(e.target.value)}
                        ></textarea>
                    </div>

                    {/* Reagendamiento */}
                    {(resultado === 'CONTACTADO' || resultado === 'INTERESADO' || resultado === 'BUZON') && (
                        <div className="p-4 bg-blue-50 rounded-lg border border-blue-100">
                            <h3 className="font-semibold text-blue-900 mb-3 flex items-center gap-2">
                                <span className="material-symbols-outlined">calendar_clock</span>
                                Reagendar llamada
                            </h3>
                            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                <div>
                                    <label className="block text-sm font-medium text-blue-800 mb-1">Fecha</label>
                                    <input
                                        type="date"
                                        className="w-full rounded-lg border-blue-200 focus:border-blue-500 focus:ring-blue-500"
                                        value={fechaReagendamiento}
                                        onChange={(e) => setFechaReagendamiento(e.target.value)}
                                    />
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-blue-800 mb-1">Hora</label>
                                    <input
                                        type="time"
                                        className="w-full rounded-lg border-blue-200 focus:border-blue-500 focus:ring-blue-500"
                                        value={horaReagendamiento}
                                        onChange={(e) => setHoraReagendamiento(e.target.value)}
                                    />
                                </div>
                            </div>
                        </div>
                    )}

                    {/* Derivación a Ventas */}
                    {(resultado === 'VENTA' || resultado === 'INTERESADO') && (
                        <div className="p-4 bg-green-50 rounded-lg border border-green-100">
                            <h3 className="font-semibold text-green-900 mb-3 flex items-center gap-2">
                                <span className="material-symbols-outlined">monetization_on</span>
                                Oportunidad de Venta
                            </h3>
                            <div className="flex items-center gap-3 mb-3">
                                <input
                                    type="checkbox"
                                    id="crear-oportunidad"
                                    className="rounded border-green-300 text-green-600 focus:ring-green-500"
                                    checked={crearOportunidad}
                                    onChange={(e) => setCrearOportunidad(e.target.checked)}
                                />
                                <label htmlFor="crear-oportunidad" className="text-sm font-medium text-green-800">
                                    Crear oportunidad en CRM
                                </label>
                            </div>

                            {crearOportunidad && (
                                <div>
                                    <label className="block text-sm font-medium text-green-800 mb-1">Tipo de oportunidad</label>
                                    <select
                                        className="w-full rounded-lg border-green-200 focus:border-green-500 focus:ring-green-500"
                                        value={tipoOportunidad}
                                        onChange={(e) => setTipoOportunidad(e.target.value)}
                                    >
                                        <option value="VENTA_NUEVA">Venta Nueva</option>
                                        <option value="UPSELL">Upsell / Upgrade</option>
                                        <option value="RENOVACION">Renovación</option>
                                    </select>
                                </div>
                            )}
                        </div>
                    )}
                </div>

                <div className="p-6 border-t border-gray-200 bg-gray-50 flex justify-end gap-3">
                    <Button variant="secondary" onClick={onClose}>
                        Cancelar
                    </Button>
                    <Button variant="primary" onClick={() => handleSubmit(autoNext)} disabled={!resultado}>
                        {autoNext ? 'Guardar y Continuar' : 'Guardar Resultado'}
                    </Button>
                </div>
            </div>
        </div>
    );
};
