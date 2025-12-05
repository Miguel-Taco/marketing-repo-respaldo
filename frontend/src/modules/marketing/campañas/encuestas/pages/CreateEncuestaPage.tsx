import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Button } from '../../../../../shared/components/ui/Button';
import { encuestasApi } from '../services/encuestas.api';
import { CreateEncuestaRequest, Pregunta, Opcion } from '../types';
import { useEncuestasContext } from '../context/EncuestasContext';

export const CreateEncuestaPage: React.FC = () => {
    const navigate = useNavigate();
    const { id } = useParams<{ id: string }>();
    const isEditMode = !!id;

    const [titulo, setTitulo] = useState('');
    const [descripcion, setDescripcion] = useState('');
    const [preguntas, setPreguntas] = useState<Pregunta[]>([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (isEditMode) {
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

    const addPregunta = () => {
        if (preguntas.length >= 3) return;
        const newPregunta: Pregunta = {
            textoPregunta: '',
            tipoPregunta: 'MULTIPLE',
            orden: preguntas.length + 1,
            opciones: []
        };
        setPreguntas([...preguntas, newPregunta]);
    };

    const removePregunta = (index: number) => {
        const newPreguntas = preguntas.filter((_, i) => i !== index);
        // Reorder
        newPreguntas.forEach((p, i) => p.orden = i + 1);
        setPreguntas(newPreguntas);
    };

    const updatePregunta = (index: number, field: keyof Pregunta, value: any) => {
        const newPreguntas = [...preguntas];
        newPreguntas[index] = { ...newPreguntas[index], [field]: value };

        if (field === 'tipoPregunta') {
            if (value === 'ESCALA') {
                newPreguntas[index].opciones = [];
            } else {
                if (!newPreguntas[index].opciones) newPreguntas[index].opciones = [];
            }
        }

        setPreguntas(newPreguntas);
    };

    const addOpcion = (preguntaIndex: number) => {
        const newPreguntas = [...preguntas];
        const currentOpciones = newPreguntas[preguntaIndex].opciones || [];

        if (currentOpciones.length >= 6) {
            alert('Máximo 6 opciones permitidas');
            return;
        }

        const newOpcion: Opcion = {
            textoOpcion: '',
            orden: currentOpciones.length + 1,
            esAlertaUrgente: false
        };
        newPreguntas[preguntaIndex].opciones = [...currentOpciones, newOpcion];
        setPreguntas(newPreguntas);
    };

    const removeOpcion = (preguntaIndex: number, opcionIndex: number) => {
        const newPreguntas = [...preguntas];
        if (!newPreguntas[preguntaIndex].opciones) return;

        const newOpciones = newPreguntas[preguntaIndex].opciones!.filter((_, i) => i !== opcionIndex);
        newOpciones.forEach((o, i) => o.orden = i + 1);
        newPreguntas[preguntaIndex].opciones = newOpciones;
        setPreguntas(newPreguntas);
    };

    const updateOpcion = (preguntaIndex: number, opcionIndex: number, field: keyof Opcion, value: any) => {
        const newPreguntas = [...preguntas];
        if (!newPreguntas[preguntaIndex].opciones) return;

        const newOpciones = [...newPreguntas[preguntaIndex].opciones!];
        newOpciones[opcionIndex] = { ...newOpciones[opcionIndex], [field]: value };
        newPreguntas[preguntaIndex].opciones = newOpciones;
        setPreguntas(newPreguntas);
    };

    const validate = (isPublicar: boolean): boolean => {
        if (!titulo.trim()) {
            alert('El título es obligatorio');
            return false;
        }

        if (isPublicar) {
            if (preguntas.length === 0) {
                alert('Debe haber al menos una pregunta para publicar');
                return false;
            }
            for (const p of preguntas) {
                if (!p.textoPregunta.trim()) {
                    alert(`La pregunta ${p.orden} no tiene texto`);
                    return false;
                }
                if (p.tipoPregunta === 'MULTIPLE' || p.tipoPregunta === 'UNICA') {
                    if (!p.opciones || p.opciones.length < 2) {
                        alert(`La pregunta ${p.orden} debe tener al menos 2 opciones`);
                        return false;
                    }
                    for (const o of p.opciones) {
                        if (!o.textoOpcion.trim()) {
                            alert(`Una opción en la pregunta ${p.orden} está vacía`);
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    };

    const { fetchEncuestas } = useEncuestasContext();

    const handleSave = async (estado: 'BORRADOR' | 'ACTIVA') => {
        if (!validate(estado === 'ACTIVA')) return;

        setLoading(true);

        // Strip IDs for the payload to avoid backend issues with unknown properties if DTO is strict
        // and to ensure clean update (since we are clearing list on backend)
        const cleanPreguntas = preguntas.map(p => ({
            textoPregunta: p.textoPregunta,
            tipoPregunta: p.tipoPregunta,
            orden: p.orden,
            opciones: p.opciones?.map(o => ({
                textoOpcion: o.textoOpcion,
                orden: o.orden,
                esAlertaUrgente: o.esAlertaUrgente
            })) || []
        }));

        const payload: CreateEncuestaRequest = {
            titulo,
            descripcion,
            estado,
            preguntas: cleanPreguntas as Pregunta[] // Casting because we stripped IDs but type expects Pregunta
        };

        try {
            if (isEditMode) {
                await encuestasApi.update(Number(id), payload);
            } else {
                await encuestasApi.create(payload);
            }
            await fetchEncuestas(true); // Refresh list
            navigate('/encuestas');
        } catch (error) {
            console.error('Error saving encuesta:', error);
            alert('Error al guardar la encuesta');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="max-w-4xl mx-auto p-6 space-y-6">
            {/* Header */}
            <div className="flex justify-between items-center">
                <div className="flex items-center gap-4">
                    <button onClick={() => navigate('/encuestas')} className="text-gray-500 hover:text-gray-700">
                        <span className="material-symbols-outlined">arrow_back</span>
                    </button>
                    <h1 className="text-2xl font-bold text-gray-800">
                        {isEditMode ? 'Editando Borrador' : 'Crear Nueva Encuesta'}
                    </h1>
                </div>
                <div className="flex gap-3">
                    <Button variant="secondary" onClick={() => handleSave('BORRADOR')} disabled={loading}>
                        Guardar Borrador
                    </Button>
                    <Button variant="primary" onClick={() => handleSave('ACTIVA')} disabled={loading}>
                        Publicar
                    </Button>
                </div>
            </div>

            {/* Configuración General */}
            <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
                <h2 className="text-lg font-semibold text-gray-800 mb-4">Configuración General</h2>
                <div className="space-y-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Título de la Encuesta</label>
                        <input
                            type="text"
                            className="w-full border border-gray-300 rounded-md px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none"
                            placeholder="Ej. Encuesta: Personaliza tu Comunicación"
                            value={titulo}
                            onChange={(e) => setTitulo(e.target.value)}
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Descripción (visible para el cliente)</label>
                        <textarea
                            className="w-full border border-gray-300 rounded-md px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none h-24"
                            placeholder="Indícanos tus canales y momentos preferidos para recibir información y ofertas."
                            value={descripcion}
                            onChange={(e) => setDescripcion(e.target.value)}
                        />
                    </div>
                </div>
            </div>

            {/* Constructor de Preguntas */}
            <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
                <h2 className="text-lg font-semibold text-gray-800 mb-4">Constructor de Preguntas</h2>

                <div className="space-y-6">
                    {preguntas.map((pregunta, pIndex) => (
                        <div key={pIndex} className="border border-gray-200 rounded-lg p-4 bg-gray-50">
                            <div className="flex justify-between items-center mb-3">
                                <div className="flex items-center gap-2">
                                    {/* No drag handle as requested */}
                                    <span className="font-medium text-gray-700">Pregunta {pregunta.orden}</span>
                                </div>
                                <button onClick={() => removePregunta(pIndex)} className="text-red-500 hover:text-red-700">
                                    <span className="material-symbols-outlined">delete</span>
                                </button>
                            </div>

                            <div className="space-y-4">
                                <input
                                    type="text"
                                    className="w-full border border-gray-300 rounded-md px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none bg-white"
                                    placeholder="¿Cuál es tu pregunta?"
                                    value={pregunta.textoPregunta}
                                    onChange={(e) => updatePregunta(pIndex, 'textoPregunta', e.target.value)}
                                />

                                <select
                                    className="w-full md:w-1/2 border border-gray-300 rounded-md px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none bg-white"
                                    value={pregunta.tipoPregunta}
                                    onChange={(e) => updatePregunta(pIndex, 'tipoPregunta', e.target.value)}
                                >
                                    <option value="MULTIPLE">Opción Múltiple</option>
                                    <option value="UNICA">Opción Única</option>
                                    <option value="ESCALA">Calificación (Escala)</option>
                                </select>

                                {(pregunta.tipoPregunta === 'MULTIPLE' || pregunta.tipoPregunta === 'UNICA') && (
                                    <div className="space-y-2 pl-4 border-l-2 border-gray-200">
                                        {pregunta.opciones?.map((opcion, oIndex) => (
                                            <div key={oIndex} className="flex items-center gap-3">
                                                <input
                                                    type="text"
                                                    className="flex-1 border border-gray-300 rounded-md px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:outline-none text-sm"
                                                    placeholder={`Opción ${oIndex + 1}`}
                                                    value={opcion.textoOpcion}
                                                    onChange={(e) => updateOpcion(pIndex, oIndex, 'textoOpcion', e.target.value)}
                                                />
                                                <label className="flex items-center gap-2 text-sm text-gray-600 cursor-pointer">
                                                    <input
                                                        type="checkbox"
                                                        className="rounded text-blue-600 focus:ring-blue-500"
                                                        checked={opcion.esAlertaUrgente}
                                                        onChange={(e) => updateOpcion(pIndex, oIndex, 'esAlertaUrgente', e.target.checked)}
                                                    />
                                                    Activar Alerta Urgente
                                                </label>
                                                <button onClick={() => removeOpcion(pIndex, oIndex)} className="text-gray-400 hover:text-red-500">
                                                    <span className="material-symbols-outlined text-lg">close</span>
                                                </button>
                                            </div>
                                        ))}
                                        {(!pregunta.opciones || pregunta.opciones.length < 6) && (
                                            <button onClick={() => addOpcion(pIndex)} className="text-blue-600 hover:text-blue-800 text-sm font-medium flex items-center gap-1 mt-2">
                                                <span className="material-symbols-outlined text-lg">add</span>
                                                Añadir Opción
                                            </button>
                                        )}
                                    </div>
                                )}

                                {pregunta.tipoPregunta === 'ESCALA' && (
                                    <div className="flex gap-2 pl-4">
                                        {[1, 2, 3, 4, 5].map((val) => (
                                            <div key={val} className="w-8 h-8 rounded-full border-2 border-blue-500 flex items-center justify-center text-blue-500 font-bold hover:bg-blue-500 hover:text-white cursor-pointer transition-colors">
                                                {val}
                                            </div>
                                        ))}
                                    </div>
                                )}
                            </div>
                        </div>
                    ))}

                    {preguntas.length < 3 && (
                        <button
                            onClick={addPregunta}
                            className="w-full py-4 border-2 border-dashed border-gray-300 rounded-lg text-gray-500 hover:border-blue-500 hover:text-blue-500 transition-colors flex items-center justify-center gap-2 font-medium"
                        >
                            <span className="material-symbols-outlined">add_circle</span>
                            Añadir Pregunta
                        </button>
                    )}
                </div>
            </div>
        </div>
    );
};
