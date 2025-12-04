export interface CampaniaTelefonica {
  id: number;
  codigo: string;
  nombre: string;
  descripcion: string;
  fechaInicio: string;
  fechaFin: string;
  estado: 'BORRADOR' | 'Programada' | 'Vigente' | 'ACTIVA' | 'Pausada' | 'PAUSADA' | 'Cancelada' | 'Finalizada' | 'FINALIZADA' | 'PENDIENTE';
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

  // Encuesta post-llamada
  encuestaEnviada?: boolean;
  estadoEncuesta?: string;
  fechaEnvioEncuesta?: string;
  urlEncuesta?: string;
}

export interface ResultadoLlamadaRequest {
  idContacto: number;
  idContactoCola?: number;
  idLead?: number;
  idResultado?: number;
  resultado: string;
  motivo?: string;
  notas?: string;
  fechaReagendamiento?: string;
  derivadoVentas?: boolean;
  tipoOportunidad?: string;
  duracionSegundos: number;
  inicio?: Date;
  fin?: Date;
  enviarEncuesta?: boolean;
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
  descripcion?: string;
  objetivo: string;
  tipo?: 'VENTA' | 'ENCUESTA' | 'RETENCION' | 'RENOVACION' | 'VENTA_NUEVA' | 'RECUPERO';
  estado: 'BORRADOR' | 'PUBLICADO' | 'ARCHIVADO' | 'ACTIVO' | 'INACTIVO';
  pasos: PasoGuion[];
}

export interface PasoGuion {
  id?: number;
  orden: number;
  tipoSeccion: string; // INTRO, DIAGNOSTICO, OBJECIONES, CIERRE, POST_LLAMADA
  contenido: string;
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

export interface MetricasDiarias {
  pendientes: number;
  realizadasHoy: number;
  efectivasHoy: number;
}

export interface BreadcrumbItem {
  label: string;
  path?: string;
}

export interface GuionArchivo {
  id: number;
  idCampania: number;
  idAgente?: number;
  nombreArchivo: string;
  tipoArchivo: string;
  tamanioBytes: number;
  fechaSubida: string;
  urlDescarga: string;
  esGeneral: boolean;
}

export interface MetricasCampania {
  totalLeads: number;
  leadsContactados: number;
  leadsPendientes: number;
  porcentajeAvance: number;
  totalLlamadas: number;
  duracionPromedio: number;
  distribucionResultados: Record<string, ResultadoDistribucion>;
  llamadasPorDia: LlamadasPorDia[];
  llamadasPorHora: Record<number, number>;
  rendimientoPorAgente: RendimientoAgente[];
  tasaContactoGlobal: number;
  tasaEfectividad: number;
  duracionPromedioEfectivas: number;
  duracionPromedioNoEfectivas: number;
  leadsPorPrioridad: Record<string, number>;
  leadsPorEstado: Record<string, number>;
}

export interface ResultadoDistribucion {
  resultado: string;
  nombre: string;
  count: number;
  porcentaje: number;
}

export interface LlamadasPorDia {
  fecha: string;
  totalLlamadas: number;
  llamadasEfectivas: number;
}

export interface RendimientoAgente {
  idAgente: number;
  nombreAgente: string;
  llamadasRealizadas: number;
  contactosEfectivos: number;
  tasaExito: number;
  duracionPromedio: number;
  llamadasHoy: number;
}

export interface EnvioEncuesta {
  id: number;
  idLlamada: number;
  idEncuesta: number;
  idLead: number;
  telefonoDestino: string;
  urlEncuesta: string;
  fechaEnvio: string;
  estado: 'ENVIADA' | 'ERROR' | 'PENDIENTE';
  metodoComunicacion: 'SMS' | 'WHATSAPP' | 'EMAIL';
  mensajeError?: string;
  nombreLead?: string;
  nombreCampania?: string;
  tituloEncuesta?: string;
}
