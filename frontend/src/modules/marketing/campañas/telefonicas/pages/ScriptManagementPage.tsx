import React, { useEffect, useState } from 'react';
import { telemarketingApi } from '../services/telemarketingApi';
import type { Guion } from '../types';
import { Button } from '../../../../../shared/components/ui/Button';

export const ScriptManagementPage: React.FC = () => {
    const [guiones, setGuiones] = useState<Guion[]>([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');
    const [tipoFilter, setTipoFilter] = useState<string>('Todos');
    const [selectedGuion, setSelectedGuion] = useState<Guion | null>(null);
    const [showModal, setShowModal] = useState(false);
    const [editingGuion, setEditingGuion] = useState<Guion | null>(null);

    useEffect(() => {
        loadGuiones();
    }, []);

    const loadGuiones = async () => {
        try {
            setLoading(true);
            const data = await telemarketingApi.getAllGuiones();
            setGuiones(data);
            if (data.length > 0) {
                setSelectedGuion(data[0]);
            }
        } catch (error) {
            console.error('Error cargando guiones:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleCreate = () => {
        setEditingGuion(null);
        setShowModal(true);
    };

    const handleEdit = (guion: Guion) => {
        setEditingGuion(guion);
        setShowModal(true);
    };

    const handleDelete = (id: number) => {
        if (confirm('¿Estás seguro de que deseas eliminar este guion?')) {
            setGuiones(prev => prev.filter(g => g.id !== id));
            if (selectedGuion?.id === id) {
                setSelectedGuion(guiones.find(g => g.id !== id) || null);
            }
        }
    };

    const handleSave = (guion: Partial<Guion>) => {
        if (editingGuion) {
            // Update existing
            setGuiones(prev => prev.map(g =>
                g.id === editingGuion.id ? { ...g, ...guion } : g
            ));
        } else {
            // Create new
            const newGuion: Guion = {
                id: Math.max(...guiones.map(g => g.id), 0) + 1,
                nombre: guion.nombre || 'Nuevo Guion',
                descripcion: guion.descripcion || '',
                objetivo: guion.objetivo || '',
                tipo: guion.tipo || 'VENTA',
                estado: 'BORRADOR',
                pasos: guion.pasos || []
            };
            setGuiones(prev => [...prev, newGuion]);
            setSelectedGuion(newGuion);
        }
        setShowModal(false);
    };

    const filteredGuiones = guiones.filter(g => {
        if (tipoFilter !== 'Todos' && g.tipo !== tipoFilter) return false;
        if (searchTerm && !g.nombre.toLowerCase().includes(searchTerm.toLowerCase())) return false;
        return true;
    });

    return (
        <div className="flex flex-col h-full p-6">
            <div className="flex justify-between items-start mb-6">
                <div>
                    <h1 className="text-4xl font-black text-gray-900">Gestión de guiones</h1>
                    <p className="text-gray-500 mt-2">Administra los scripts de llamadas para tus campañas</p>
                </div>
                <Button variant="primary" icon="add" onClick={handleCreate}>
                    Crear nuevo guion
                </Button>
            </div>

            {/* Filters */}
            <div className="bg-white rounded-lg p-4 mb-6 border border-gray-200">
                <div className="flex flex-col md:flex-row gap-4">
                    <div className="flex-1">
                        <input
                            className="form-input w-full rounded-lg border-gray-300 bg-white text-gray-800 h-12 px-4"
                            placeholder="Buscar guiones..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                        />
                    </div>
                    <div className="flex gap-2">
                        {['Todos', 'VENTA', 'ENCUESTA', 'RETENCION'].map(tipo => (
                            <button
                                key={tipo}
                                className={`px-4 py-2 rounded-full text-sm font-medium ${tipoFilter === tipo
                                    ? 'bg-primary text-white'
                                    : 'bg-gray-100 text-gray-700'
                                    }`}
                                onClick={() => setTipoFilter(tipo)}
                            >
                                {tipo}
                            </button>
                        ))}
                    </div>
                </div>
            </div>

            <div className="flex flex-1 gap-6 overflow-hidden">
                {/* Scripts List */}
                <div className="w-1/3 bg-white rounded-lg border border-gray-200 overflow-y-auto">
                    {loading ? (
                        <div className="flex items-center justify-center h-64">
                            <span className="animate-spin h-8 w-8 border-4 border-primary border-t-transparent rounded-full"></span>
                        </div>
                    ) : (
                        <div className="divide-y divide-gray-200">
                            {filteredGuiones.map(guion => (
                                <div
                                    key={guion.id}
                                    className={`p-4 cursor-pointer hover:bg-gray-50 ${selectedGuion?.id === guion.id ? 'bg-primary/5' : ''
                                        }`}
                                    onClick={() => setSelectedGuion(guion)}
                                >
                                    <h3 className="font-bold text-gray-900">{guion.nombre}</h3>
                                    <p className="text-sm text-gray-500 mt-1">{guion.descripcion}</p>
                                    <div className="flex gap-2 mt-2">
                                        <span className="px-2 py-1 bg-primary/10 text-primary text-xs rounded-full">{guion.tipo}</span>
                                        <span className="px-2 py-1 bg-gray-100 text-gray-700 text-xs rounded-full">
                                            {guion.estado}
                                        </span>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>

                {/* Script Detail */}
                {selectedGuion && (
                    <div className="flex-1 bg-white rounded-lg border border-gray-200 overflow-y-auto p-6">
                        <div className="flex justify-between items-start mb-6">
                            <div>
                                <h2 className="text-2xl font-bold text-gray-900">{selectedGuion.nombre}</h2>
                                <p className="text-gray-500 mt-1">{selectedGuion.descripcion}</p>
                            </div>
                            <div className="flex gap-2">
                                <Button variant="secondary" icon="edit" onClick={() => handleEdit(selectedGuion)}>
                                    Editar
                                </Button>
                                <Button variant="danger" icon="delete" onClick={() => handleDelete(selectedGuion.id)}>
                                    Eliminar
                                </Button>
                            </div>
                        </div>

                        <div className="space-y-4">
                            {selectedGuion.pasos.map((paso, index) => (
                                <div key={index} className="border border-gray-200 rounded-lg p-4">
                                    <div className="flex items-center gap-3 mb-3">
                                        <span className="flex items-center justify-center w-8 h-8 rounded-full bg-primary text-white font-bold">
                                            {paso.orden}
                                        </span>
                                        <h4 className="text-lg font-bold text-gray-900">{paso.titulo}</h4>
                                        <span className="ml-auto px-2 py-1 bg-gray-100 text-gray-600 text-xs rounded">
                                            {paso.tipo.replace('_', ' ')}
                                        </span>
                                    </div>
                                    <p className="text-gray-700">{paso.contenido}</p>
                                </div>
                            ))}
                        </div>
                    </div>
                )}
            </div>

            {/* Modal for Create/Edit */}
            {showModal && (
                <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
                    <div className="bg-white rounded-lg max-w-2xl w-full max-h-[90vh] overflow-y-auto">
                        <div className="p-6 border-b border-gray-200">
                            <h2 className="text-2xl font-bold text-gray-900">
                                {editingGuion ? 'Editar Guion' : 'Crear Nuevo Guion'}
                            </h2>
                        </div>
                        <div className="p-6 space-y-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    Nombre del Guion
                                </label>
                                <input
                                    id="guion-nombre"
                                    type="text"
                                    className="w-full px-4 py-2 border border-gray-300 rounded-lg bg-white text-gray-900"
                                    defaultValue={editingGuion?.nombre || ''}
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    Descripción
                                </label>
                                <textarea
                                    id="guion-descripcion"
                                    className="w-full px-4 py-2 border border-gray-300 rounded-lg bg-white text-gray-900"
                                    rows={3}
                                    defaultValue={editingGuion?.descripcion || ''}
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    Tipo
                                </label>
                                <select
                                    id="guion-tipo"
                                    className="w-full px-4 py-2 border border-gray-300 rounded-lg bg-white text-gray-900"
                                    defaultValue={editingGuion?.tipo || 'VENTA'}
                                >
                                    <option value="VENTA">VENTA</option>
                                    <option value="ENCUESTA">ENCUESTA</option>
                                    <option value="RETENCION">RETENCION</option>
                                </select>
                            </div>
                        </div>
                        <div className="p-6 border-t border-gray-200 flex justify-end gap-3">
                            <Button variant="secondary" onClick={() => setShowModal(false)}>
                                Cancelar
                            </Button>
                            <Button
                                variant="primary"
                                onClick={() => {
                                    const nombre = (document.getElementById('guion-nombre') as HTMLInputElement).value;
                                    const descripcion = (document.getElementById('guion-descripcion') as HTMLTextAreaElement).value;
                                    const tipo = (document.getElementById('guion-tipo') as HTMLSelectElement).value;
                                    handleSave({ nombre, descripcion, tipo: tipo as any, pasos: editingGuion?.pasos || [] });
                                }}
                            >
                                {editingGuion ? 'Guardar Cambios' : 'Crear Guion'}
                            </Button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};
