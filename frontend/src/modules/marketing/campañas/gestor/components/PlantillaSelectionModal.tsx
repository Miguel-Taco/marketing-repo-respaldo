import React, { useState, useEffect } from 'react';
import { plantillasApi } from '../services/plantillas.api';
import { PlantillaCampana } from '../types/plantilla.types';

interface PlantillaSelectionModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSelect: (plantilla: PlantillaCampana) => void;
}

export const PlantillaSelectionModal: React.FC<PlantillaSelectionModalProps> = ({ isOpen, onClose, onSelect }) => {
    const [plantillas, setPlantillas] = useState<PlantillaCampana[]>([]);
    const [loading, setLoading] = useState(false);
    const [searchTerm, setSearchTerm] = useState('');

    useEffect(() => {
        if (isOpen) {
            loadPlantillas();
        }
    }, [isOpen]);

    const loadPlantillas = async () => {
        setLoading(true);
        try {
            const response = await plantillasApi.getAll({
                page: 0,
                size: 100, // Fetch enough templates
                nombre: searchTerm || undefined
            });
            setPlantillas(response.content);
        } catch (error) {
            console.error('Error loading templates:', error);
        } finally {
            setLoading(false);
        }
    };

    // Debounce search
    useEffect(() => {
        const timer = setTimeout(() => {
            if (isOpen) loadPlantillas();
        }, 500);
        return () => clearTimeout(timer);
    }, [searchTerm]);

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-[70] p-4">
            <div className="bg-white rounded-xl shadow-2xl max-w-4xl w-full max-h-[80vh] flex flex-col">
                {/* Header */}
                <div className="flex items-center justify-between p-6 border-b border-gray-200">
                    <div>
                        <h2 className="text-xl font-bold text-gray-900">Seleccionar Plantilla</h2>
                        <p className="text-sm text-gray-500">Elija una plantilla para autocompletar los datos de la campaña</p>
                    </div>
                    <button onClick={onClose} className="p-2 rounded-full hover:bg-gray-100 transition-colors">
                        <span className="material-symbols-outlined text-gray-500">close</span>
                    </button>
                </div>

                {/* Search */}
                <div className="p-4 border-b border-gray-100">
                    <div className="relative">
                        <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-gray-400">search</span>
                        <input
                            type="text"
                            placeholder="Buscar plantilla por nombre..."
                            className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary focus:outline-none"
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                        />
                    </div>
                </div>

                {/* List */}
                <div className="flex-1 overflow-y-auto p-6">
                    {loading ? (
                        <div className="flex justify-center py-10">
                            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
                        </div>
                    ) : plantillas.length === 0 ? (
                        <div className="text-center py-10 text-gray-500">
                            No se encontraron plantillas
                        </div>
                    ) : (
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            {plantillas.map((plantilla) => (
                                <div
                                    key={plantilla.idPlantilla}
                                    className="border border-gray-200 rounded-lg p-4 hover:border-primary hover:shadow-md transition-all cursor-pointer group flex flex-col h-full"
                                    onClick={() => onSelect(plantilla)}
                                >
                                    <div className="flex justify-between items-start mb-2">
                                        <h3 className="font-semibold text-gray-900 group-hover:text-primary transition-colors line-clamp-1">
                                            {plantilla.nombre}
                                        </h3>
                                        {plantilla.canalEjecucion && (
                                            <span className={`px-2 py-1 text-xs rounded-full font-medium shrink-0 ml-2 ${plantilla.canalEjecucion === 'Mailing' ? 'bg-purple-100 text-purple-700' : 'bg-orange-100 text-orange-700'
                                                }`}>
                                                {plantilla.canalEjecucion}
                                            </span>
                                        )}
                                    </div>

                                    <p className="text-sm text-gray-600 mb-4 line-clamp-2 flex-grow">
                                        {plantilla.descripcion || 'Sin descripción'}
                                    </p>

                                    <div className="space-y-2 mt-auto">
                                        <div className="flex items-center text-xs text-gray-500">
                                            <span className="material-symbols-outlined text-[16px] mr-1.5 text-gray-400">category</span>
                                            <span className="font-medium text-gray-700 mr-1">Temática:</span>
                                            {plantilla.tematica}
                                        </div>

                                        {plantilla.idSegmento && (
                                            <div className="flex items-center text-xs text-gray-500">
                                                <span className="material-symbols-outlined text-[16px] mr-1.5 text-blue-400">groups</span>
                                                <span className="font-medium text-gray-700 mr-1">Segmento:</span>
                                                <span className="bg-blue-50 text-blue-700 px-1.5 py-0.5 rounded" title={`ID: ${plantilla.idSegmento}`}>
                                                    {plantilla.nombreSegmento || `ID ${plantilla.idSegmento}`}
                                                </span>
                                            </div>
                                        )}

                                        {plantilla.idEncuesta && (
                                            <div className="flex items-center text-xs text-gray-500">
                                                <span className="material-symbols-outlined text-[16px] mr-1.5 text-green-400">poll</span>
                                                <span className="font-medium text-gray-700 mr-1">Encuesta:</span>
                                                <span className="bg-green-50 text-green-700 px-1.5 py-0.5 rounded" title={`ID: ${plantilla.idEncuesta}`}>
                                                    {plantilla.tituloEncuesta || `ID ${plantilla.idEncuesta}`}
                                                </span>
                                            </div>
                                        )}
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};
