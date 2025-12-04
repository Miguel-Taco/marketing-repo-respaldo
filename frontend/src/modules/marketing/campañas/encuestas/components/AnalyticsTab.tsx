import { useState, useEffect } from 'react';
import { Select } from '../../../../../shared/components/ui/Select';
import { Button } from '../../../../../shared/components/ui/Button';
import { LoadingSpinner } from '../../../../../shared/components/ui/LoadingSpinner';
import { TrendChartComponent } from './TrendChartComponent';
import { ScaleQuestionComponent } from './ScaleQuestionComponent';
import { ChoiceQuestionComponent } from './ChoiceQuestionComponent';
import { encuestasApi } from '../services/encuestas.api';
import { Encuesta } from '../types';

// Interfaces for Analytics Data
interface TrendData {
    fecha: string;
    cantidad: number;
}

interface IndicatorData {
    etiqueta: string;
    valor: number;
    porcentaje: number;
    metadata: any;
}

interface SummaryData {
    totalRespuestas: number;
    alertasUrgentes: number;
}

export const AnalyticsTab: React.FC = () => {
    const [encuestas, setEncuestas] = useState<Encuesta[]>([]);
    const [selectedEncuestaId, setSelectedEncuestaId] = useState<string>('');
    const [trendData, setTrendData] = useState<TrendData[]>([]);
    const [indicatorsData, setIndicatorsData] = useState<IndicatorData[]>([]);
    const [summaryData, setSummaryData] = useState<SummaryData>({ totalRespuestas: 0, alertasUrgentes: 0 });
    const [loading, setLoading] = useState(false);
    const [questions, setQuestions] = useState<any[]>([]); // To map indicators to question types

    const [selectedRange, setSelectedRange] = useState<string>('all');

    useEffect(() => {
        loadEncuestas();
    }, []);

    useEffect(() => {
        if (selectedEncuestaId) {
            loadAnalytics(parseInt(selectedEncuestaId), selectedRange);
        }
    }, [selectedEncuestaId, selectedRange]);

    const loadEncuestas = async () => {
        try {
            const data = await encuestasApi.getAll();
            setEncuestas(data);
            if (data.length > 0) {
                // Encontrar la última encuesta ACTIVA
                const ultimaActiva = [...data]
                    .sort((a, b) => b.idEncuesta - a.idEncuesta)
                    .find(e => e.estado === 'ACTIVA');

                if (ultimaActiva) {
                    setSelectedEncuestaId(ultimaActiva.idEncuesta.toString());
                } else {
                    setSelectedEncuestaId(data[data.length - 1].idEncuesta.toString());
                }
            }
        } catch (error) {
            console.error('Error loading encuestas:', error);
        }
    };

    const loadAnalytics = async (id: number, range: string) => {
        setLoading(true);
        try {
            const trendData = await encuestasApi.getTendencia(id);
            setTrendData(trendData);

            const summaryData = await encuestasApi.getResumen(id, range);
            setSummaryData(summaryData);

            const indicatorsData = await encuestasApi.getIndicadores(id, range);
            setIndicatorsData(indicatorsData);

        } catch (error) {
            console.error('Error loading analytics:', error);
            setTrendData([]);
            setSummaryData({ totalRespuestas: 0, alertasUrgentes: 0 });
            setIndicatorsData([]);
        } finally {
            setLoading(false);
        }
    };

    const handleRefresh = () => {
        if (selectedEncuestaId) {
            loadAnalytics(parseInt(selectedEncuestaId), selectedRange);
        }
    };

    const selectedEncuesta = encuestas.find(e => e.idEncuesta.toString() === selectedEncuestaId);

    const dateOptions = [
        { value: 'all', label: 'Todas las respuestas' },
        { value: '7d', label: 'Últimos 7 días' },
        { value: '14d', label: 'Últimos 14 días' },
        { value: '28d', label: 'Últimos 28 días' },
    ];

    const filteredEncuestas = encuestas.filter(e => e.estado === 'ACTIVA' || e.estado === 'ARCHIVADA');

    return (
        <div className="space-y-6 animate-fade-in">
            {/* Controls */}
            <div className="bg-white p-4 rounded-lg shadow-sm border border-gray-100 flex flex-col md:flex-row justify-between items-center gap-4">
                <div className="w-full md:w-1/3">
                    <Select
                        options={filteredEncuestas.map(e => ({ value: e.idEncuesta.toString(), label: e.titulo }))}
                        value={selectedEncuestaId}
                        onChange={(e) => {
                            setLoading(true); // Set loading immediately
                            setIndicatorsData([]); // Clear previous data
                            setTrendData([]); // Clear previous data
                            setSummaryData({ totalRespuestas: 0, alertasUrgentes: 0 }); // Clear summary
                            setSelectedEncuestaId(e.target.value);
                        }}
                        placeholder="Seleccionar Encuesta"
                    />
                </div>
                <div className="flex items-center gap-4 w-full md:w-auto">
                    <div className="w-48">
                        <Select
                            options={dateOptions}
                            value={selectedRange}
                            onChange={(e) => setSelectedRange(e.target.value)}
                        />
                    </div>
                    <Button variant="primary" onClick={handleRefresh} disabled={loading}>
                        {loading ? 'Cargando...' : 'Refrescar Datos'}
                    </Button>
                </div>
            </div>

            {loading ? (
                <div className="flex justify-center items-center py-20">
                    <LoadingSpinner size="lg" />
                </div>
            ) : (
                <>
                    {/* KPI Cards */}
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                        <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-100">
                            <span className="text-sm text-gray-500">Total Respuestas Recibidas</span>
                            <div className="text-3xl font-bold text-gray-900 mt-2">
                                {summaryData.totalRespuestas}
                            </div>
                        </div>
                        <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-100">
                            <span className="text-sm text-gray-500">Estado</span>
                            <div className="mt-2">
                                <span className={`px-3 py-1 rounded-full text-sm font-medium ${selectedEncuesta?.estado === 'ACTIVA' ? 'bg-green-100 text-green-800' :
                                    selectedEncuesta?.estado === 'BORRADOR' ? 'bg-gray-100 text-gray-800' : 'bg-red-100 text-red-800'
                                    }`}>
                                    {selectedEncuesta?.estado || 'N/A'}
                                </span>
                            </div>
                        </div>
                        <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-100">
                            <span className="text-sm text-gray-500">Alertas Urgentes</span>
                            <div className="text-3xl font-bold text-gray-900 mt-2">{summaryData.alertasUrgentes}</div>
                        </div>
                    </div>

                    {/* Trend Chart */}
                    <TrendChartComponent data={trendData} />

                    {/* Questions Analysis */}
                    <div className="space-y-4">
                        <h3 className="text-lg font-bold text-gray-800">Resultados de: {selectedEncuesta?.titulo}</h3>

                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                            {indicatorsData.map((indicator, index) => {
                                // Determine type based on metadata structure or fetch from question definition
                                // Since we don't have the question definition easily here, we infer from metadata
                                // If 'histograma' exists -> Scale
                                // If 'distribucion' exists -> Choice
                                // If 'porcentajes' exists -> Multiple (or just check logic)

                                if (indicator.metadata.histograma) {
                                    return <ScaleQuestionComponent key={index} data={indicator} />;
                                } else if (indicator.metadata.distribucion) {
                                    // Infer UNICA vs MULTIPLE
                                    // Usually we would pass the type from backend.
                                    // Let's assume UNICA unless we see 'totalEncuestadosUnicos' which implies MULTIPLE
                                    const type = indicator.metadata.totalEncuestadosUnicos ? 'MULTIPLE' : 'UNICA';
                                    return <ChoiceQuestionComponent key={index} data={indicator} type={type} />;
                                }
                                return null;
                            })}
                        </div>

                        {indicatorsData.length === 0 && (
                            <div className="text-center py-10 text-gray-500 bg-white rounded-lg border border-gray-100">
                                No hay datos de indicadores disponibles para esta encuesta.
                            </div>
                        )}
                    </div>
                </>
            )}
        </div>
    );
};
