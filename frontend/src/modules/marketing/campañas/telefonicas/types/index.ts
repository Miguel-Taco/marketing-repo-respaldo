export interface CampaniaTelefonica {
  id: number;
  codigo: string;
  nombre: string;
  descripcion: string;
  fechaInicio: string;
  fechaFin: string;
  estado: 'BORRADOR' | 'ACTIVA' | 'PAUSADA' | 'FINALIZADA' | 'PENDIENTE';
  prioridad: 'ALTA' | 'MEDIA' | 'BAJA';
  idGuion: number;
  totalLeads: number;
  leadsPendientes: number;
  leadsContactados: number;
  porcentajeAvance: number;
  idsAgentes: number[];
}

export interface Contacto {
  id: number;
  idLead: number;
  nombreCompleto: string;
  telefono: string;
  email: string;
  empresa: string;
  estadoCampania: 'NO_CONTACTADO' | 'EN_SEGUIMIENTO' | 'CERRADO' | 'NO_INTERESADO';
  prioridad: 'ALTA' | 'MEDIA' | 'BAJA';
  segmento: string;
  numeroIntentos: number;
  fechaUltimaLlamada?: string;
  resultadoUltimaLlamada?: string;
  notas?: string;
  nombreCampania?: string;
  idCampania?: number;
}

export interface Llamada {
  id: number;
  idCampania: number;
  idContacto: number;
  idAgente: number;
  fechaHora: string;
  duracionSegundos: number;
  resultado: 'CONTACTADO' | 'NO_CONTESTA' | 'BUZON' | 'NO_INTERESADO' | 'INTERESADO' | 'VENTA';
  motivo?: string;
  notas?: string;
  fechaReagendamiento?: string;
  derivadoVentas?: boolean;
  tipoOportunidad?: string;
  nombreContacto?: string;
  telefonoContacto?: string;
  nombreCampania?: string;
}

export interface ResultadoLlamadaRequest {
  idContacto: number;
  resultado: string;
  motivo?: string;
  notas?: string;
  fechaReagendamiento?: string;
  derivadoVentas?: boolean;
  tipoOportunidad?: string;
  duracionSegundos: number;
}

export interface MetricasAgente {
  idAgente: number;
  idCampania?: number;
  llamadasRealizadas: number;
  contactosEfectivos: number;
  tasaContacto: number;
  duracionPromedio: number;
  llamadasHoy: number;
  llamadasSemana: number;
  llamadasMes: number;
  distribucionResultados: Record<string, number>;
  periodoActual?: MetricasComparativa;
  periodoAnterior?: MetricasComparativa;
}

export interface MetricasComparativa {
  periodo: string;
  llamadasRealizadas: number;
  contactosEfectivos: number;
  tasaContacto: number;
}

export interface Guion {
  id: number;
  nombre: string;
  descripcion: string;
  objetivo: string;
  tipo: 'VENTA' | 'ENCUESTA' | 'RETENCION';
  estado: 'BORRADOR' | 'PUBLICADO' | 'ARCHIVADO';
  pasos: PasoGuion[];
}

export interface PasoGuion {
  orden: number;
  tipo: 'INFORMATIVO' | 'PREGUNTA_ABIERTA' | 'PREGUNTA_CERRADA' | 'OPCION_UNICA';
  titulo: string;
  contenido: string;
  campoGuardado?: string;
}

export interface CreateCampaniaRequest {
  nombre: string;
  fechaInicio: string;
  fechaFin: string;
  estado: string;
  idGuion: number;
  idsAgentes: number[];
  leadsIniciales: number[];
  prioridadColaDefault?: string;
}
