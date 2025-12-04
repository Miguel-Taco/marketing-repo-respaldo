const API_BASE_URL = 'http://localhost:8080';

export const fetchSurveyContent = async (idEncuesta) => {
    try {
        const response = await fetch(`${API_BASE_URL}/public/v1/encuestas/contenido/${idEncuesta}`);

        if (!response.ok) {
            if (response.status === 404) {
                throw new Error('Encuesta no encontrada');
            } else if (response.status === 403) {
                throw new Error('Esta encuesta no está disponible');
            }
            throw new Error('Error al cargar la encuesta');
        }

        return await response.json();
    } catch (error) {
        console.error('Error fetching survey:', error);
        throw error;
    }
};

export const submitSurveyResponse = async (data) => {
    try {
        const response = await fetch(`${API_BASE_URL}/public/v1/encuestas/respuestas`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(data)
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            if (response.status === 400) {
                throw new Error(errorData.message || 'Datos inválidos. Verifica tus respuestas.');
            } else if (response.status === 409) {
                throw new Error(errorData.message || 'Ya has respondido esta encuesta anteriormente.');
            }
            throw new Error('Error al enviar la encuesta');
        }

        return await response.json();
    } catch (error) {
        console.error('Error submitting survey:', error);
        throw error;
    }
};
