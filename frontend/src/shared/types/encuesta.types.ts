// Encuesta types based on backend API
export interface EncuestaDisponible {
    idEncuesta: number;
    titulo: string;
}

export interface Encuesta {
    idEncuesta: number;
    titulo: string;
    descripcion: string;
    estado: string;
    fechaModificacion: string;
    cantidadRespuestas: number;
}
