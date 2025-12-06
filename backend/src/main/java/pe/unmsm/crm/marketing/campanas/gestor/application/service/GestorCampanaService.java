package pe.unmsm.crm.marketing.campanas.gestor.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.unmsm.crm.marketing.campanas.gestor.application.event.CampanaEstadoCambiadoEvent;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.Campana;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.TipoAccion;
import pe.unmsm.crm.marketing.campanas.gestor.domain.port.input.IGestorCampanaUseCase;
import pe.unmsm.crm.marketing.campanas.gestor.domain.port.output.CampanaRepositoryPort;
import pe.unmsm.crm.marketing.shared.application.service.UserContextService;
import pe.unmsm.crm.marketing.campanas.gestor.infra.scheduler.CampaignActivationManager;
import pe.unmsm.crm.marketing.shared.logging.AccionLog;
import pe.unmsm.crm.marketing.shared.logging.AuditoriaService;
import pe.unmsm.crm.marketing.shared.logging.ModuloLog;

import pe.unmsm.crm.marketing.campanas.gestor.domain.port.output.ICanalEjecucionPort;
import pe.unmsm.crm.marketing.campanas.gestor.domain.port.output.IConsultaRecursosPort;
import pe.unmsm.crm.marketing.shared.infra.exception.BusinessException;
import pe.unmsm.crm.marketing.shared.infra.exception.NotFoundException;
import pe.unmsm.crm.marketing.shared.infra.exception.ValidationException;

import java.time.LocalDateTime;

/**
 * Servicio de aplicación principal del Gestor de campanas.
 * Orquesta el ciclo de vida completo de las campanas y coordina con otros
 * módulos.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class GestorCampanaService implements IGestorCampanaUseCase {

    private final CampanaRepositoryPort campanaRepository;
    private final IConsultaRecursosPort consultaRecursosPort;
    private final ICanalEjecucionPort canalEjecucionPort;
    private final ApplicationEventPublisher eventPublisher;
    private final UserContextService userContextService;
    private final CampaignActivationManager activationManager;
    private final AuditoriaService auditoriaService;

    @Override
    public Campana crear(Campana campana) {
        log.debug("Creando campaña: {}", campana.getNombre());

        // La campaña se crea en estado Borrador (por defecto en @PrePersist)
        Campana saved = campanaRepository.save(campana);

        // Publicar evento de creación
        publicarEvento(saved.getIdCampana(), null, "Borrador", TipoAccion.CREACION, "Campaña creada");

        // AUDITORÍA
        auditoriaService.registrarEvento(ModuloLog.CAMPANIAS_GESTOR, AccionLog.CREAR, saved.getIdCampana(), null,
                "Campaña creada: " + saved.getNombre());

        log.info("Campaña creada con ID: {}", saved.getIdCampana());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Campana obtenerPorId(Long idCampana) {
        return campanaRepository.findById(idCampana)
                .orElseThrow(() -> new NotFoundException("Campaña", idCampana));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Campana> listar(String nombre, String estado, String prioridad, String canalEjecucion,
            Boolean esArchivado, int page, int size) {
        // Por defecto, excluir archivadas
        if (esArchivado == null) {
            esArchivado = false;
        }

        return campanaRepository.findByFiltros(nombre, estado, prioridad, canalEjecucion, esArchivado,
                PageRequest.of(page, size, org.springframework.data.domain.Sort
                        .by(org.springframework.data.domain.Sort.Direction.DESC, "fechaCreacion")));
    }

    @Override
    public Campana editar(Long idCampana, Campana datosActualizados) {
        Campana existente = obtenerPorId(idCampana);

        // Validar que se puede editar (delegado al estado)
        existente.editar();

        // Actualizar campos permitidos
        if (datosActualizados.getNombre() != null) {
            existente.setNombre(datosActualizados.getNombre());
        }
        if (datosActualizados.getTematica() != null) {
            existente.setTematica(datosActualizados.getTematica());
        }
        if (datosActualizados.getDescripcion() != null) {
            existente.setDescripcion(datosActualizados.getDescripcion());
        }
        if (datosActualizados.getPrioridad() != null) {
            existente.setPrioridad(datosActualizados.getPrioridad());
        }
        if (datosActualizados.getCanalEjecucion() != null) {
            existente.setCanalEjecucion(datosActualizados.getCanalEjecucion());
        }
        if (datosActualizados.getIdAgente() != null) {
            existente.setIdAgente(datosActualizados.getIdAgente());
        }
        if (datosActualizados.getIdSegmento() != null) {
            existente.setIdSegmento(datosActualizados.getIdSegmento());
        }
        if (datosActualizados.getIdEncuesta() != null) {
            existente.setIdEncuesta(datosActualizados.getIdEncuesta());
        }
        if (datosActualizados.getIdPlantilla() != null) {
            existente.setIdPlantilla(datosActualizados.getIdPlantilla());
        }

        Campana updated = campanaRepository.save(existente);

        // Publicar evento de edición
        publicarEvento(idCampana, existente.getEstado().getNombre(),
                existente.getEstado().getNombre(), TipoAccion.EDICION, "Campaña editada");

        // AUDITORÍA
        auditoriaService.registrarEvento(ModuloLog.CAMPANIAS_GESTOR, AccionLog.ACTUALIZAR, idCampana, null,
                "Campaña actualizada: " + existente.getNombre());

        log.info("Campaña {} editada", idCampana);
        return updated;
    }

    @Override
    public Campana programar(Long idCampana, LocalDateTime fechaInicio, LocalDateTime fechaFin,
            Integer idAgente, Long idSegmento, Integer idEncuesta) {
        Campana campana = obtenerPorId(idCampana);
        String estadoAnterior = campana.getEstado().getNombre();

        // Validaciones de negocio
        validarFechas(fechaInicio, fechaFin);
        validarRecursos(idAgente, idSegmento, idEncuesta, fechaInicio, fechaFin);

        // Asignar recursos y fechas
        campana.setFechaProgramadaInicio(fechaInicio);
        campana.setFechaProgramadaFin(fechaFin);
        campana.setIdAgente(idAgente);
        campana.setIdSegmento(idSegmento);
        campana.setIdEncuesta(idEncuesta);

        // Transición de estado (Borrador → Programada)
        campana.programar();

        Campana updated = campanaRepository.save(campana);

        // Delegar programación al canal correspondiente
        canalEjecucionPort.programarCampana(updated);

        // Publicar evento
        publicarEvento(idCampana, estadoAnterior, "Programada", TipoAccion.PROGRAMACION, null);

        // Programar activación dinámica
        activationManager.scheduleActivation(idCampana, fechaInicio);

        // AUDITORÍA
        auditoriaService.registrarEvento(ModuloLog.CAMPANIAS_GESTOR, AccionLog.CAMBIAR_ESTADO, idCampana, null,
                "Estado cambiado a Programada. Inicio: " + fechaInicio);

        log.info("Campaña {} programada para {} - {}", idCampana, fechaInicio, fechaFin);
        return updated;
    }

    @Override
    public Campana activar(Long idCampana) {
        // Cancelar tarea programada si existe (ya que se está activando)
        activationManager.cancelActivation(idCampana);

        Campana campana = obtenerPorId(idCampana);
        String estadoAnterior = campana.getEstado().getNombre();

        // Transición de estado (Programada → Vigente)
        campana.activar();

        campanaRepository.save(campana);

        // Delegar ejecución al canal correspondiente
        boolean exitoEjecucion = canalEjecucionPort.activarCampana(campana);

        if (!exitoEjecucion) {
            log.error("Error al activar campaña {} en canal {}", idCampana, campana.getCanalEjecucion());
            publicarEvento(idCampana, "Vigente", "Vigente", TipoAccion.ERROR_EJECUCION,
                    "Error al delegar ejecución al canal");
        } else {
            publicarEvento(idCampana, estadoAnterior, "Vigente", TipoAccion.ACTIVACION, null);

            // AUDITORÍA
            auditoriaService.registrarEvento(ModuloLog.CAMPANIAS_GESTOR, AccionLog.CAMBIAR_ESTADO, idCampana, null,
                    "Estado cambiado a Vigente");
        }

        log.info("Campaña {} activada", idCampana);
        return campana;
    }

    @Override
    public Campana pausar(Long idCampana, String motivo) {
        activationManager.cancelActivation(idCampana);
        Campana campana = obtenerPorId(idCampana);
        String estadoAnterior = campana.getEstado().getNombre();

        // Transición de estado (Vigente → Pausada)
        campana.pausar();

        campanaRepository.save(campana);

        // Notificar al canal de ejecución
        canalEjecucionPort.notificarPausa(idCampana, motivo);

        // Publicar evento
        publicarEvento(idCampana, estadoAnterior, "Pausada", TipoAccion.PAUSA, motivo);

        // AUDITORÍA
        auditoriaService.registrarEvento(ModuloLog.CAMPANIAS_GESTOR, AccionLog.CAMBIAR_ESTADO, idCampana, null,
                "Estado cambiado a Pausada. Motivo: " + motivo);

        log.info("Campaña {} pausada. Motivo: {}", idCampana, motivo);
        return campana;
    }

    @Override
    public Campana reanudar(Long idCampana) {
        Campana campana = obtenerPorId(idCampana);
        String estadoAnterior = campana.getEstado().getNombre();

        // Transición de estado (Pausada → Vigente)
        campana.reanudar();

        campanaRepository.save(campana);

        // Notificar al canal
        canalEjecucionPort.notificarReanudacion(idCampana);

        // Publicar evento
        publicarEvento(idCampana, estadoAnterior, "Vigente", TipoAccion.REANUDACION, null);

        // AUDITORÍA
        auditoriaService.registrarEvento(ModuloLog.CAMPANIAS_GESTOR, AccionLog.CAMBIAR_ESTADO, idCampana, null,
                "Estado cambiado a Vigente (Reanudada)");

        log.info("Campaña {} reanudada", idCampana);
        return campana;
    }

    @Override
    public Campana cancelar(Long idCampana, String motivo) {
        activationManager.cancelActivation(idCampana);
        Campana campana = obtenerPorId(idCampana);
        String estadoAnterior = campana.getEstado().getNombre();

        // Transición de estado → Cancelada
        campana.cancelar();

        campanaRepository.save(campana);

        // Notificar al canal
        canalEjecucionPort.notificarCancelacion(idCampana, motivo);

        // Publicar evento
        publicarEvento(idCampana, estadoAnterior, "Cancelada", TipoAccion.CANCELACION, motivo);

        // AUDITORÍA
        auditoriaService.registrarEvento(ModuloLog.CAMPANIAS_GESTOR, AccionLog.CAMBIAR_ESTADO, idCampana, null,
                "Estado cambiado a Cancelada. Motivo: " + motivo);

        log.info("Campaña {} cancelada. Motivo: {}", idCampana, motivo);
        return campana;
    }

    @Override
    public Campana finalizar(Long idCampana) {
        activationManager.cancelActivation(idCampana);
        Campana campana = obtenerPorId(idCampana);
        String estadoAnterior = campana.getEstado().getNombre();

        // Transición de estado (Vigente → Finalizada)
        campana.finalizar();

        campanaRepository.save(campana);

        // Publicar evento
        publicarEvento(idCampana, estadoAnterior, "Finalizada", TipoAccion.FINALIZACION, null);

        // AUDITORÍA
        auditoriaService.registrarEvento(ModuloLog.CAMPANIAS_GESTOR, AccionLog.CAMBIAR_ESTADO, idCampana, null,
                "Estado cambiado a Finalizada");

        log.info("Campaña {} finalizada", idCampana);
        return campana;
    }

    @Override
    public Campana reprogramar(Long idCampana, LocalDateTime nuevaFechaInicio, LocalDateTime nuevaFechaFin) {
        Campana campana = obtenerPorId(idCampana);
        String estadoAnterior = campana.getEstado().getNombre();

        // Validar fechas
        validarFechas(nuevaFechaInicio, nuevaFechaFin);

        // Validar disponibilidad de agente en nuevas fechas
        if (campana.getIdAgente() != null) {
            if (!consultaRecursosPort.isAgenteDisponible(campana.getIdAgente(), nuevaFechaInicio, nuevaFechaFin)) {
                throw new BusinessException("INVALID_STATE",
                        "El agente no está disponible en las fechas seleccionadas");
            }
        }

        // Actualizar fechas
        campana.setFechaProgramadaInicio(nuevaFechaInicio);
        campana.setFechaProgramadaFin(nuevaFechaFin);

        // Transición de estado (Programada → Programada)
        campana.reprogramar();

        campanaRepository.save(campana);

        // Notificar al canal
        canalEjecucionPort.reprogramarCampana(campana);

        // Publicar evento
        publicarEvento(idCampana, estadoAnterior, "Programada", TipoAccion.REPROGRAMACION, null);

        // Reprogramar activación dinámica
        activationManager.scheduleActivation(idCampana, nuevaFechaInicio);

        // AUDITORÍA
        auditoriaService.registrarEvento(ModuloLog.CAMPANIAS_GESTOR, AccionLog.CAMBIAR_ESTADO, idCampana, null,
                "Estado cambiado a Programada (Reprogramada). Inicio: " + nuevaFechaInicio);

        log.info("Campaña {} reprogramada para {} - {}", idCampana, nuevaFechaInicio, nuevaFechaFin);
        return campana;
    }

    @Override
    public Campana archivar(Long idCampana) {
        Campana campana = obtenerPorId(idCampana);

        // Transición de estado (solo Finalizada/Cancelada pueden archivarse)
        campana.archivar();

        campanaRepository.save(campana);

        // Publicar evento
        publicarEvento(idCampana, campana.getEstado().getNombre(),
                campana.getEstado().getNombre(), TipoAccion.ARCHIVO, "Campaña archivada");

        // AUDITORÍA
        auditoriaService.registrarEvento(ModuloLog.CAMPANIAS_GESTOR, AccionLog.CAMBIAR_ESTADO, idCampana, null,
                "Campaña archivada");

        log.info("Campaña {} archivada", idCampana);
        return campana;
    }

    @Override
    public Campana duplicar(Long idCampana) {
        Campana original = obtenerPorId(idCampana);

        // Crear copia en estado Borrador
        Campana copia = Campana.builder()
                .nombre("Copia de " + original.getNombre())
                .tematica(original.getTematica())
                .descripcion(original.getDescripcion())
                .prioridad(original.getPrioridad())
                .canalEjecucion(original.getCanalEjecucion())
                .idSegmento(original.getIdSegmento())
                .idEncuesta(original.getIdEncuesta())
                .idPlantilla(original.getIdPlantilla())
                // NO copiar: fechas, agente, estado (inicia en Borrador)
                .build();

        Campana saved = campanaRepository.save(copia);

        // Publicar evento para la copia
        publicarEvento(saved.getIdCampana(), null, "Borrador", TipoAccion.DUPLICACION,
                "Duplicada desde campaña " + idCampana);

        // AUDITORÍA
        auditoriaService.registrarEvento(ModuloLog.CAMPANIAS_GESTOR, AccionLog.CREAR, saved.getIdCampana(), null,
                "Campaña duplicada desde ID " + idCampana);

        log.info("Campaña {} duplicada. Nueva ID: {}", idCampana, saved.getIdCampana());
        return saved;
    }

    @Override
    public void eliminar(Long idCampana) {
        Campana campana = obtenerPorId(idCampana);

        // Validar que está en Borrador
        if (!"Borrador".equals(campana.getEstado().getNombre())) {
            throw new BusinessException("INVALID_STATE", "Solo se pueden eliminar campanas en estado Borrador");
        }

        campanaRepository.deleteById(idCampana);

        // AUDITORÍA
        auditoriaService.registrarEvento(ModuloLog.CAMPANIAS_GESTOR, AccionLog.ELIMINAR, idCampana, null,
                "Campaña eliminada físicamente");

        log.info("Campaña {} eliminada físicamente", idCampana);
    }

    // ========== Métodos Privados de Validación ==========

    private void validarFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw new ValidationException("Las fechas de inicio y fin son obligatorias");
        }

        if (!fechaInicio.isBefore(fechaFin)) {
            throw new ValidationException("La fecha de inicio debe ser anterior a la fecha de fin");
        }

        if (fechaInicio.isBefore(LocalDateTime.now())) {
            throw new ValidationException("No se puede programar una campaña en el pasado");
        }
    }

    private void validarRecursos(Integer idAgente, Long idSegmento, Integer idEncuesta,
            LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        // Segmento es obligatorio
        if (idSegmento == null) {
            throw new ValidationException("El segmento es obligatorio para programar la campaña");
        }

        if (!consultaRecursosPort.existeSegmento(idSegmento)) {
            throw new BusinessException("NOT_FOUND", "El segmento especificado no existe o no tiene miembros");
        }

        // Agente es obligatorio
        if (idAgente == null) {
            throw new ValidationException("El agente es obligatorio para programar la campaña");
        }

        if (!consultaRecursosPort.isAgenteDisponible(idAgente, fechaInicio, fechaFin)) {
            throw new BusinessException("INVALID_STATE", "El agente no está disponible en las fechas seleccionadas");
        }

        // Encuesta es opcional, pero si se proporciona, debe existir
        if (idEncuesta != null && !consultaRecursosPort.existeEncuesta(idEncuesta)) {
            throw new BusinessException("NOT_FOUND", "La encuesta especificada no existe o no está activa");
        }
    }

    private void publicarEvento(Long idCampana, String estadoAnterior, String estadoNuevo,
            TipoAccion tipoAccion, String motivo) {
        CampanaEstadoCambiadoEvent event = CampanaEstadoCambiadoEvent.builder()
                .idCampana(idCampana)
                .estadoAnterior(estadoAnterior)
                .estadoNuevo(estadoNuevo)
                .tipoAccion(tipoAccion.name())
                .timestamp(LocalDateTime.now())
                .motivo(motivo)
                .usuarioResponsable(userContextService.getCurrentUsername().orElse("SISTEMA"))
                .build();

        eventPublisher.publishEvent(event);
    }

}