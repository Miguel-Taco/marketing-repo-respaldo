import React, { useState, useEffect } from 'react';
import { segmentosApi } from '../services/segmentos.api';
import { SegmentoResumen, Segmento } from '../../../../../shared/types/segmento.types';

export const SegmentosTab: React.FC = () => {
    const [segments, setSegments] = useState<SegmentoResumen[]>([]);
    const [loadingSegments, setLoadingSegments] = useState(false);
    const [selectedSegmentId, setSelectedSegmentId] = useState<number | null>(null);
    const [selectedSegment, setSelectedSegment] = useState<Segmento | null>(null);
    const [loadingDetails, setLoadingDetails] = useState(false);
    const [searchTerm, setSearchTerm] = useState('');

    // Load segments on mount
    useEffect(() => {
        const loadSegments = async () => {
            setLoadingSegments(true);
            try {
                const segmentList = await segmentosApi.getActivos();
                setSegments(segmentList);
            } catch (error) {
                console.error('Error loading segments:', error);
            } finally {
                setLoadingSegments(false);
            }
        };

        loadSegments();
    }, []);

    // Load segment details when selection changes
    useEffect(() => {
        const loadSegmentDetails = async () => {
            if (!selectedSegmentId) {
                setSelectedSegment(null);
                return;
            }

            setLoadingDetails(true);
            try {
                const segment = await segmentosApi.getById(selectedSegmentId);
                setSelectedSegment(segment);
            } catch (error) {
                console.error('Error loading segment details:', error);
                setSelectedSegment(null);
            } finally {
                setLoadingDetails(false);
            }
        };

        loadSegmentDetails();
    }, [selectedSegmentId]);

    const handleSegmentClick = (segmentId: number) => {
        setSelectedSegmentId(segmentId);
    };

    const filteredSegments = segments.filter(segment =>
        segment.nombre.toLowerCase().includes(searchTerm.toLowerCase())
    );

    // Helper function to format field names (e.g., "genero" -> "Género")
    const formatFieldName = (field: string): string => {
        const fieldMap: Record<string, string> = {
            'genero': 'Género',
            'edad': 'Edad',
            'ubicacion': 'Ubicación',
            'departamento': 'Departamento',
            'provincia': 'Provincia',
            'distrito': 'Distrito',
            'estadoCivil': 'Estado Civil',
            'nivelEducativo': 'Nivel Educativo',
            'ocupacion': 'Ocupación',
            'ingresos': 'Ingresos',
            'fechaNacimiento': 'Fecha de Nacimiento',
            'fechaRegistro': 'Fecha de Registro',
        };

        return fieldMap[field] || field.charAt(0).toUpperCase() + field.slice(1);
    };

    // Helper function to format operators (e.g., "IGUAL" -> "igual a")
    const formatOperator = (operator: string): string => {
        const operatorMap: Record<string, string> = {
            'IGUAL': 'igual a',
            'DIFERENTE': 'diferente de',
            'MAYOR_QUE': 'mayor que',
            'MENOR_QUE': 'menor que',
            'MAYOR_O_IGUAL': 'mayor o igual a',
            'MENOR_O_IGUAL': 'menor o igual a',
            'CONTIENE': 'contiene',
            'NO_CONTIENE': 'no contiene',
            'EMPIEZA_CON': 'empieza con',
            'TERMINA_CON': 'termina con',
            'ENTRE': 'entre',
            'EN': 'en',
        };

        return operatorMap[operator] || operator.toLowerCase().replace(/_/g, ' ');
    };

    return (
        <div className="bg-white rounded-lg shadow-card border border-separator p-6 h-[calc(100vh-12rem)]">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6 h-full">
                {/* Left Column - Segment List */}
                <div className="md:col-span-1 flex flex-col gap-4 border-r border-separator pr-6 h-full">
                    {/* Search */}
                    <div className="relative w-full flex-shrink-0">
                        <span className="material-symbols-outlined text-gray-500 absolute top-1/2 left-3 -translate-y-1/2 text-lg">
                            search
                        </span>
                        <input
                            type="text"
                            className="w-full pl-10 pr-4 py-2.5 border border-separator rounded-lg focus:ring-2 focus:ring-primary focus:outline-none text-sm"
                            placeholder="Buscar segmento..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                        />
                    </div>

                    {/* Segment List */}
                    <div className="flex flex-col gap-2 overflow-y-auto flex-1 pr-2">
                        {loadingSegments ? (
                            <div className="flex items-center justify-center py-8">
                                <div className="inline-block h-6 w-6 animate-spin rounded-full border-2 border-solid border-primary border-r-transparent"></div>
                            </div>
                        ) : filteredSegments.length === 0 ? (
                            <div className="text-center py-8 text-gray-500 text-sm">
                                {searchTerm ? 'No se encontraron segmentos' : 'No hay segmentos disponibles'}
                            </div>
                        ) : (
                            filteredSegments.map((segment) => (
                                <div
                                    key={segment.id}
                                    onClick={() => handleSegmentClick(segment.id)}
                                    className={`p-3 rounded-lg cursor-pointer transition-all ${selectedSegmentId === segment.id
                                        ? 'border border-primary bg-primary/10'
                                        : 'border border-transparent hover:bg-primary/5'
                                        }`}
                                >
                                    <p className={`font-medium text-sm ${selectedSegmentId === segment.id ? 'text-primary' : 'text-gray-800'
                                        }`}>
                                        {segment.nombre}
                                    </p>
                                </div>
                            ))
                        )}
                    </div>
                </div>

                {/* Right Column - Segment Details */}
                <div className="md:col-span-2 flex flex-col gap-6 h-full overflow-y-auto pr-2">
                    {loadingDetails && selectedSegmentId ? (
                        <div className="flex items-center justify-center h-full">
                            <div className="inline-block h-8 w-8 animate-spin rounded-full border-2 border-solid border-primary border-r-transparent"></div>
                            <span className="ml-3 text-gray-600">Cargando información del segmento...</span>
                        </div>
                    ) : selectedSegment ? (
                        <>
                            {/* Segment Name */}
                            <h3 className="text-lg font-bold text-gray-900">{selectedSegment.nombre}</h3>

                            {/* Description */}
                            {selectedSegment.descripcion && (
                                <p className="text-sm text-gray-600 -mt-4">{selectedSegment.descripcion}</p>
                            )}

                            {/* Stats Grid */}
                            <div className="grid grid-cols-2 gap-4">
                                <div className="bg-gray-50 p-4 rounded-lg border border-gray-200">
                                    <p className="text-sm text-gray-600">Usuarios Totales</p>
                                    <p className="text-2xl font-bold text-gray-900 mt-1">
                                        {selectedSegment.cantidadMiembros?.toLocaleString() || '0'}
                                    </p>
                                </div>
                                <div className="bg-gray-50 p-4 rounded-lg border border-gray-200">
                                    <p className="text-sm text-gray-600">Tipo de Audiencia</p>
                                    <p className="text-2xl font-bold text-gray-900 mt-1">
                                        {selectedSegment.tipoAudiencia}
                                    </p>
                                </div>
                            </div>

                            {/* Criteria Section */}
                            {(selectedSegment as any).reglaPrincipal && (selectedSegment as any).reglaPrincipal.reglas && (selectedSegment as any).reglaPrincipal.reglas.length > 0 && (
                                <div>
                                    <h4 className="text-base font-semibold text-gray-800 mb-3">Criterios del Segmento</h4>
                                    <div className="space-y-3 border border-gray-200 rounded-lg p-4">
                                        {(selectedSegment as any).reglaPrincipal.reglas.map((regla: any, index: number) => (
                                            <React.Fragment key={index}>
                                                <div className="flex items-start gap-3 p-3 bg-gray-100 rounded">
                                                    <span className="material-symbols-outlined text-primary text-xl mt-0.5">
                                                        filter_alt
                                                    </span>
                                                    <div className="flex-1">
                                                        <p className="text-sm font-medium text-gray-800">
                                                            {formatFieldName(regla.campo)} {formatOperator(regla.operador)} <span className="font-bold">{regla.valorTexto || regla.valorNumero}</span>
                                                        </p>
                                                    </div>
                                                </div>
                                                {index < (selectedSegment as any).reglaPrincipal.reglas.length - 1 && (
                                                    <div className="flex items-center justify-center">
                                                        <span className="text-xs font-bold text-gray-500 bg-white px-2">
                                                            {(selectedSegment as any).reglaPrincipal.operadorLogico || 'Y'}
                                                        </span>
                                                    </div>
                                                )}
                                            </React.Fragment>
                                        ))}
                                    </div>
                                </div>
                            )}

                            {/* Info note */}
                            <div className="flex items-start gap-2 text-xs text-gray-600 bg-blue-50 p-3 rounded border border-blue-100">
                                <span className="material-symbols-outlined text-sm text-blue-600">info</span>
                                <p>Este segmento está disponible para ser utilizado en nuevas campañas.</p>
                            </div>
                        </>
                    ) : (
                        <div className="flex flex-col items-center justify-center h-full text-gray-400">
                            <span className="material-symbols-outlined text-6xl mb-3">group</span>
                            <p className="text-sm">Selecciona un segmento para ver su información</p>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};
