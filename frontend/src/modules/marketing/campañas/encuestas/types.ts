export interface Encuesta {
    idEncuesta: number;
    titulo: string;
    descripcion: string;
    estado: 'BORRADOR' | 'ACTIVA' | 'ARCHIVADA';
    fechaModificacion: string;
    totalRespuestas: number;
    preguntas?: Pregunta[];
}

export interface Pregunta {
    idPregunta?: number;
    textoPregunta: string;
    tipoPregunta: 'UNICA' | 'MULTIPLE' | 'ESCALA';
    orden: number;
    opciones?: Opcion[];
}

export interface Opcion {
    idOpcion?: number;
    textoOpcion: string;
    orden: number;
    esAlertaUrgente: boolean;
}

export interface CreateEncuestaRequest {
    titulo: string;
    descripcion: string;
    estado: 'BORRADOR' | 'ACTIVA';
    preguntas: Pregunta[];
}
