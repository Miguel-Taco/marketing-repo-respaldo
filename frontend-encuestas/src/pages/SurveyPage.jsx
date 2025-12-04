import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { fetchSurveyContent, submitSurveyResponse } from '../services/api';
import './SurveyPage.css';

function SurveyPage() {
    const { idEncuesta, idLead } = useParams();
    const [survey, setSurvey] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [responses, setResponses] = useState({});
    const [submitting, setSubmitting] = useState(false);
    const [submitError, setSubmitError] = useState(null);
    const [success, setSuccess] = useState(false);

    useEffect(() => {
        // Validar que ambos parámetros existen
        if (!idEncuesta || !idLead) {
            setError('Enlace Inválido o Incompleto. El link de la encuesta debe incluir tanto el ID de la encuesta como el ID del lead.');
            setLoading(false);
            return;
        }

        const loadSurvey = async () => {
            try {
                setLoading(true);
                const data = await fetchSurveyContent(idEncuesta);
                setSurvey(data);
                setError(null);
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        loadSurvey();
    }, [idEncuesta, idLead]);

    const handleOptionChange = (preguntaId, opcionId, isMultiple) => {
        if (isMultiple) {
            setResponses(prev => {
                const currentResponses = prev[preguntaId] || [];
                const isSelected = currentResponses.includes(opcionId);

                return {
                    ...prev,
                    [preguntaId]: isSelected
                        ? currentResponses.filter(id => id !== opcionId)
                        : [...currentResponses, opcionId]
                };
            });
        } else {
            setResponses(prev => ({
                ...prev,
                [preguntaId]: opcionId
            }));
        }
        setSubmitError(null);
    };

    const validateResponses = () => {
        if (!survey || !survey.preguntas) return false;

        const totalQuestions = survey.preguntas.length;
        const answeredQuestions = Object.keys(responses).length;

        if (answeredQuestions < totalQuestions) {
            setSubmitError(
                `Por favor, responde todas las preguntas. Has respondido ${answeredQuestions} de ${totalQuestions}.`
            );
            return false;
        }

        return true;
    };

    const handleSubmit = async () => {
        if (!validateResponses()) return;

        try {
            setSubmitting(true);
            setSubmitError(null);

            // Preparar las respuestas en el formato esperado por el backend
            const respuestasArray = [];

            Object.entries(responses).map(([preguntaId, value]) => {
                const pregunta = survey.preguntas.find(p => p.idPregunta === parseInt(preguntaId));
                const isScale = pregunta?.tipoPregunta === 'ESCALA';

                if (isScale) {
                    respuestasArray.push({
                        idPregunta: parseInt(preguntaId),
                        idOpcion: null,
                        valorRespuesta: value // El valor numérico (1-5)
                    });
                } else if (Array.isArray(value)) {
                    // Para preguntas múltiples, enviar una entrada por cada opción seleccionada
                    value.forEach(opcionId => {
                        respuestasArray.push({
                            idPregunta: parseInt(preguntaId),
                            idOpcion: opcionId
                        });
                    });
                } else {
                    respuestasArray.push({
                        idPregunta: parseInt(preguntaId),
                        idOpcion: value
                    });
                }
            });

            const requestData = {
                leadId: parseInt(idLead),
                idEncuesta: parseInt(idEncuesta),
                respuestas: respuestasArray
            };

            await submitSurveyResponse(requestData);
            setSuccess(true);
        } catch (err) {
            setSubmitError(err.message);
        } finally {
            setSubmitting(false);
        }
    };

    const isFormComplete = survey && Object.keys(responses).length === survey.preguntas?.length;

    if (loading) {
        return (
            <div className="survey-container">
                <div className="survey-card">
                    <div className="loading-container">
                        <div className="loading-spinner"></div>
                        <p className="loading-text">Cargando encuesta...</p>
                    </div>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="survey-container">
                <div className="survey-card">
                    <div className="error-container">
                        <p className="error-text">❌ {error}</p>
                    </div>
                </div>
            </div>
        );
    }

    if (success) {
        return (
            <div className="survey-container">
                <div className="survey-card">
                    <div className="success-container">
                        <div className="success-icon">✓</div>
                        <h2>¡Gracias por tu participación!</h2>
                        <p>Tu respuesta ha sido registrada exitosamente.</p>
                    </div>
                </div>
            </div>
        );
    }

    if (!survey) {
        return null;
    }

    return (
        <div className="survey-container">
            <div className="survey-card">
                <div className="survey-header">
                    <span className="material-symbols-outlined survey-icon">
                        assignment
                    </span>
                    <h1 className="survey-title">{survey.titulo}</h1>
                    <p className="survey-description">{survey.descripcion}</p>
                </div>

                <div className="questions-container">
                    {survey.preguntas
                        .sort((a, b) => a.orden - b.orden)
                        .map((pregunta) => {
                            const isMultiple = pregunta.tipoPregunta === 'MULTIPLE';
                            const isScale = pregunta.tipoPregunta === 'ESCALA';
                            const inputType = isMultiple ? 'checkbox' : 'radio';

                            return (
                                <div key={pregunta.idPregunta} className="question-card">
                                    <h3 className="question-text">
                                        {pregunta.textoPregunta}
                                        {isMultiple && <span style={{ fontSize: '0.85rem', color: '#718096', marginLeft: '0.5rem' }}>(selecciona todas las que apliquen)</span>}
                                        {isScale && <span style={{ fontSize: '0.85rem', color: '#718096', marginLeft: '0.5rem' }}>(califica del 1 al 5)</span>}
                                    </h3>

                                    {isScale ? (
                                        <div className="scale-container">
                                            {[1, 2, 3, 4, 5].map((value) => (
                                                <button
                                                    key={value}
                                                    className={`scale-button ${responses[pregunta.idPregunta] === value ? 'selected' : ''}`}
                                                    onClick={() => handleOptionChange(pregunta.idPregunta, value, false)}
                                                >
                                                    {value}
                                                </button>
                                            ))}
                                        </div>
                                    ) : (
                                        <ul className="options-list">
                                            {pregunta.opciones
                                                .sort((a, b) => a.orden - b.orden)
                                                .map((opcion) => {
                                                    const isChecked = isMultiple
                                                        ? (responses[pregunta.idPregunta] || []).includes(opcion.idOpcion)
                                                        : responses[pregunta.idPregunta] === opcion.idOpcion;

                                                    return (
                                                        <li key={opcion.idOpcion} className="option-item">
                                                            <input
                                                                type={inputType}
                                                                id={`opcion-${opcion.idOpcion}`}
                                                                name={`pregunta-${pregunta.idPregunta}`}
                                                                checked={isChecked}
                                                                onChange={() => handleOptionChange(
                                                                    pregunta.idPregunta,
                                                                    opcion.idOpcion,
                                                                    isMultiple
                                                                )}
                                                            />
                                                            <label
                                                                htmlFor={`opcion-${opcion.idOpcion}`}
                                                                className="option-label"
                                                            >
                                                                {opcion.textoOpcion}
                                                            </label>
                                                        </li>
                                                    );
                                                })}
                                        </ul>
                                    )}
                                </div>
                            );
                        })}
                </div>

                {submitError && (
                    <div className="submit-error">
                        <span className="error-icon-small">⚠️</span>
                        {submitError}
                    </div>
                )}

                <button
                    className="submit-button"
                    onClick={handleSubmit}
                    disabled={!isFormComplete || submitting}
                >
                    {submitting ? 'Enviando...' : 'Enviar Respuestas'}
                </button>

                <p className="note-text">
                    Tus respuestas nos ayudarán a ofrecerte un mejor servicio y comunicarnos contigo de la forma que prefieras.
                </p>
            </div>
        </div>
    );
}

export default SurveyPage;
