// Segmento types based on backend API
export interface Segmento {
    id: number;
    nombre: string;
    descripcion: string;
    tipoAudiencia: string;
    estado: string;
    fechaCreacion: string;
    fechaActualizacion: string;
    cantidadMiembros: number;
}

export interface SegmentoResumen {
    id: number;
    nombre: string;
    descripcion: string;
    tipoAudiencia: string;
    cantidadMiembros: number;
    estado: string;
}
