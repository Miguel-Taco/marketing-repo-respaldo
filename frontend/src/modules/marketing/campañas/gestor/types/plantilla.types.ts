import { CanalEjecucion } from './campana.types';

export interface PlantillaCampana {
    idPlantilla: number;
    nombre: string;
    tematica: string;
    descripcion?: string;
    canalEjecucion?: CanalEjecucion;
    idSegmento?: number;
    nombreSegmento?: string;
    idEncuesta?: number;
    tituloEncuesta?: string;
    fechaCreacion: string;
    fechaModificacion: string;
}

export interface CrearPlantillaRequest {
    nombre: string;
    tematica: string;
    descripcion?: string;
    canalEjecucion?: CanalEjecucion;
    idSegmento?: number;
    idEncuesta?: number;
}
