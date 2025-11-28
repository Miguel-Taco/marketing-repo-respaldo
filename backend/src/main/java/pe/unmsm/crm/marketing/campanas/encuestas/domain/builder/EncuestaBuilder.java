package pe.unmsm.crm.marketing.campanas.encuestas.domain.builder;

import pe.unmsm.crm.marketing.campanas.encuestas.api.dto.CreateEncuestaDto;
import pe.unmsm.crm.marketing.campanas.encuestas.domain.model.Encuesta;
import pe.unmsm.crm.marketing.campanas.encuestas.domain.model.Encuesta.EstadoEncuesta;
import pe.unmsm.crm.marketing.campanas.encuestas.domain.model.Opcion;
import pe.unmsm.crm.marketing.campanas.encuestas.domain.model.Pregunta;

import java.util.List;

/**
 * Builder para construir objetos complejos de tipo Encuesta.
 * Implementa el patrón Builder GoF para desacoplar la lógica de construcción
 * del objeto de la lógica de negocio del servicio.
 */
public class EncuestaBuilder {

    private final Encuesta encuesta;

    /**
     * Constructor privado que inicializa una nueva instancia de Encuesta.
     */
    private EncuestaBuilder() {
        this.encuesta = new Encuesta();
    }

    /**
     * Método estático para iniciar la construcción de una Encuesta.
     * 
     * @return una nueva instancia del builder
     */
    public static EncuestaBuilder nueva() {
        return new EncuestaBuilder();
    }

    /**
     * Establece el título de la encuesta.
     * 
     * @param titulo el título de la encuesta
     * @return el builder para encadenamiento fluido
     */
    public EncuestaBuilder conTitulo(String titulo) {
        this.encuesta.setTitulo(titulo);
        return this;
    }

    /**
     * Establece la descripción de la encuesta.
     * 
     * @param descripcion la descripción de la encuesta
     * @return el builder para encadenamiento fluido
     */
    public EncuestaBuilder conDescripcion(String descripcion) {
        this.encuesta.setDescripcion(descripcion);
        return this;
    }

    /**
     * Establece el estado de la encuesta.
     * 
     * @param estado el estado de la encuesta (BORRADOR, ACTIVA, ARCHIVADA)
     * @return el builder para encadenamiento fluido
     */
    public EncuestaBuilder conEstado(EstadoEncuesta estado) {
        this.encuesta.setEstado(estado);
        return this;
    }

    /**
     * Método inteligente que encapsula toda la lógica de conversión de DTOs a
     * entidades.
     * Convierte una lista de PreguntaDto en entidades Pregunta con sus respectivas
     * Opciones,
     * estableciendo correctamente las relaciones bidireccionales.
     * 
     * @param preguntasDto lista de DTOs de preguntas
     * @return el builder para encadenamiento fluido
     */
    public EncuestaBuilder conPreguntas(List<CreateEncuestaDto.PreguntaDto> preguntasDto) {
        if (preguntasDto == null || preguntasDto.isEmpty()) {
            return this;
        }

        for (CreateEncuestaDto.PreguntaDto preguntaDto : preguntasDto) {
            Pregunta pregunta = construirPregunta(preguntaDto);
            this.encuesta.getPreguntas().add(pregunta);
        }

        return this;
    }

    /**
     * Construye una entidad Pregunta a partir de un DTO.
     * 
     * @param preguntaDto el DTO de la pregunta
     * @return la entidad Pregunta construida
     */
    private Pregunta construirPregunta(CreateEncuestaDto.PreguntaDto preguntaDto) {
        Pregunta pregunta = new Pregunta();
        pregunta.setEncuesta(this.encuesta);
        pregunta.setTextoPregunta(preguntaDto.getTextoPregunta());
        pregunta.setTipoPregunta(preguntaDto.getTipoPregunta());
        pregunta.setOrden(preguntaDto.getOrden());

        if (preguntaDto.getOpciones() != null && !preguntaDto.getOpciones().isEmpty()) {
            for (CreateEncuestaDto.OpcionDto opcionDto : preguntaDto.getOpciones()) {
                Opcion opcion = construirOpcion(opcionDto, pregunta);
                pregunta.getOpciones().add(opcion);
            }
        }

        return pregunta;
    }

    /**
     * Construye una entidad Opcion a partir de un DTO.
     * 
     * @param opcionDto el DTO de la opción
     * @param pregunta  la pregunta padre a la que pertenece esta opción
     * @return la entidad Opcion construida
     */
    private Opcion construirOpcion(CreateEncuestaDto.OpcionDto opcionDto, Pregunta pregunta) {
        Opcion opcion = new Opcion();
        opcion.setPregunta(pregunta);
        opcion.setTextoOpcion(opcionDto.getTextoOpcion());
        opcion.setOrden(opcionDto.getOrden());
        opcion.setEsAlertaUrgente(
                opcionDto.getEsAlertaUrgente() != null ? opcionDto.getEsAlertaUrgente() : false);
        return opcion;
    }

    /**
     * Construye y retorna la entidad Encuesta completa.
     * 
     * @return la entidad Encuesta construida
     */
    public Encuesta build() {
        return this.encuesta;
    }
}
