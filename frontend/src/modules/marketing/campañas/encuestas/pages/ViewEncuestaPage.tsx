import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { encuestasApi } from '../services/encuestas.api';
import { Pregunta } from '../types';

export const ViewEncuestaPage: React.FC = () => {
    const navigate = useNavigate();
    const { id } = useParams<{ id: string }>();

    const [titulo, setTitulo] = useState('');
    const [descripcion, setDescripcion] = useState('');
    const [preguntas, setPreguntas] = useState<Pregunta[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (id) {
            loadEncuesta();
        }
    }, [id]);

    const loadEncuesta = async () => {
        try {
            setLoading(true);
            const data = await encuestasApi.getById(Number(id));
            setTitulo(data.titulo);
            setDescripcion(data.descripcion);
            // Ensure questions are sorted by order
            const sortedPreguntas = (data.preguntas || []).sort((a, b) => a.orden - b.orden);
            // Ensure options are sorted
            sortedPreguntas.forEach(p => {
                if (p.opciones) {
                    p.opciones.sort((a, b) => a.orden - b.orden);
                }
            });
            setPreguntas(sortedPreguntas);
        } catch (error) {
            console.error('Error loading encuesta:', error);
            alert('Error al cargar la encuesta');
            navigate('/encuestas');
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return <div className="p-8 text-center text-gray-500">Cargando encuesta...</div>;
    }

    return (
        <div className="max-w-4xl mx-auto p-6 space-y-6">
            {/* Header */}
            <div className="flex justify-between items-center">
                <div className="flex items-center gap-4">
                    <button onClick={() => navigate('/encuestas')} className="text-gray-500 hover:text-gray-700">
                        <span className="material-symbols-outlined">arrow_back</span>
                    </button>
                    <h1 className="text-2xl font-bold text-gray-800">
                        Detalle de Encuesta
                    </h1>
                </div>
                <div className="flex gap-3">
                    {/* Botón Volver eliminado */}
                </div>
            </div>

            {/* Configuración General */}
            <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200 opacity-90">
                <h2 className="text-lg font-semibold text-gray-800 mb-4">Información General</h2>
                <div className="space-y-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Título de la Encuesta</label>
                        <div className="w-full border border-gray-200 rounded-md px-3 py-2 bg-gray-50 text-gray-800">
                            {titulo}
                        </div>
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Descripción</label>
                        <div className="w-full border border-gray-200 rounded-md px-3 py-2 bg-gray-50 text-gray-800 min-h-[60px]">
                            {descripcion || 'Sin descripción'}
                        </div>
                    </div>
                </div>
            </div>

            {/* Visualizador de Preguntas */}
            <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
                <h2 className="text-lg font-semibold text-gray-800 mb-4">Preguntas</h2>

                <div className="space-y-6">
                    {preguntas.map((pregunta, pIndex) => (
                        <div key={pIndex} className="border border-gray-200 rounded-lg p-4 bg-gray-50">
                            <div className="flex justify-between items-center mb-3">
                                <span className="font-medium text-gray-700">Pregunta {pregunta.orden}</span>
                                <span className="text-xs font-medium px-2 py-1 bg-blue-100 text-blue-700 rounded-full">
                                    {pregunta.tipoPregunta === 'MULTIPLE' ? 'Opción Múltiple' :
                                        pregunta.tipoPregunta === 'UNICA' ? 'Opción Única' : 'Escala'}
                                </span>
                            </div>

                            <div className="space-y-4">
                                <div className="w-full border border-gray-300 rounded-md px-3 py-2 bg-white text-gray-800">
                                    {pregunta.textoPregunta}
                                </div>

                                {(pregunta.tipoPregunta === 'MULTIPLE' || pregunta.tipoPregunta === 'UNICA') && (
                                    <div className="space-y-2 pl-4 border-l-2 border-gray-200">
                                        {pregunta.opciones?.map((opcion, oIndex) => (
                                            <div key={oIndex} className="flex items-center gap-3">
                                                <div className="flex-1 border border-gray-200 rounded-md px-3 py-2 bg-white text-sm text-gray-600">
                                                    {opcion.textoOpcion}
                                                </div>
                                                {opcion.esAlertaUrgente && (
                                                    <span className="flex items-center gap-1 text-xs text-red-600 bg-red-50 px-2 py-1 rounded-full border border-red-100">
                                                        <span className="material-symbols-outlined text-sm">warning</span>
                                                        Alerta Urgente
                                                    </span>
                                                )}
                                            </div>
                                        ))}
                                    </div>
                                )}

                                {pregunta.tipoPregunta === 'ESCALA' && (
                                    <div className="flex gap-2 pl-4">
                                        {[1, 2, 3, 4, 5].map((val) => (
                                            <div key={val} className="w-8 h-8 rounded-full border-2 border-gray-300 flex items-center justify-center text-gray-400 font-bold bg-white">
                                                {val}
                                            </div>
                                        ))}
                                    </div>
                                )}
                            </div>
                        </div>
                    ))}

                    {preguntas.length === 0 && (
                        <div className="text-center py-8 text-gray-500 italic">
                            Esta encuesta no tiene preguntas configuradas.
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};
