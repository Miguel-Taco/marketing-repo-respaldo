package pe.unmsm.crm.marketing.campanas.telefonicas.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.entity.*;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.repository.*;
import pe.unmsm.crm.marketing.segmentacion.application.SegmentoService;
import pe.unmsm.crm.marketing.shared.logging.AuditoriaService;
import pe.unmsm.crm.marketing.shared.logging.ModuloLog;
import pe.unmsm.crm.marketing.shared.logging.AccionLog;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Servicio de fachada para la creación de campañas telefónicas desde otros
 * módulos.
 * Proporciona una interfaz simplificada para la integración con el gestor de
 * campañas.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CampaniaTelefonicaFacadeService {

    private final CampaniaTelefonicaRepository campaniaRepository;
    private final CampaniaTelefonicaConfigRepository configRepository;
    private final CampaniaAgenteRepository campaniaAgenteRepository;
    private final ColaLlamadaRepository colaLlamadaRepository;
    private final SegmentoService segmentoService;
    private final AuditoriaService auditoriaService;

    /**
     * Crea una campaña telefónica completa con:
     * - Registro en campania_telefonica
     * - Configuración por defecto
     * - Asignación de agente
     * - Población de cola de llamadas con leads del segmento
     *
     * @param idCampanaGestion ID de la campaña del gestor general
     * @param nombre           Nombre de la campaña
     * @param idSegmento       ID del segmento de leads
     * @param idEncuesta       ID de la encuesta (opcional)
     * @param fechaInicio      Fecha de inicio
     * @param fechaFin         Fecha de fin
     * @param idAgente         ID del agente asignado
     * @param prioridad        Prioridad (Alta, Media, Baja)
     * @return ID de la campaña telefónica creada
     */
    @Transactional
    public Integer crearCampaniaTelefonicaDesdeGestor(
            Long idCampanaGestion,
            String nombre,
            Long idSegmento,
            Integer idEncuesta,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            Integer idAgente,
            String prioridad) {
        // 1. Crear registro principal de campaña telefónica
        CampaniaTelefonicaEntity campaniaTelefonica = new CampaniaTelefonicaEntity();
        campaniaTelefonica.setIdCampanaGestion(idCampanaGestion);
        campaniaTelefonica.setIdSegmento(idSegmento);
        campaniaTelefonica.setIdEncuesta(idEncuesta);
        campaniaTelefonica.setNombre(nombre);
        campaniaTelefonica.setFechaInicio(fechaInicio);
        campaniaTelefonica.setFechaFin(fechaFin);
        campaniaTelefonica.setEstado("Programada");
        campaniaTelefonica.setIdEstado(1); // 1 = pendiente en cat_estados_campana
        campaniaTelefonica.setPrioridad(mapearPrioridad(prioridad));
        campaniaTelefonica.setFechaCreacion(LocalDateTime.now());
        campaniaTelefonica.setFechaModificacion(LocalDateTime.now());
        campaniaTelefonica.setEsArchivado(false);

        CampaniaTelefonicaEntity campaniaGuardada = campaniaRepository.save(campaniaTelefonica);
        Integer idCampaniaTelefonica = campaniaGuardada.getId();

        // 2. Crear configuración por defecto
        crearConfiguracionPorDefecto(idCampaniaTelefonica);

        // 3. Asignar agente a la campaña
        if (idAgente != null) {
            asignarAgente(idCampaniaTelefonica, idAgente);
        }

        // 4. Poblar cola de llamadas con leads del segmento
        poblarColaLlamadas(idCampaniaTelefonica, idSegmento);

        // AUDITORÍA: Registrar creación de campaña
        auditoriaService.registrarEvento(
                ModuloLog.CAMPANIAS_TELEFONICAS,
                AccionLog.CREAR,
                idCampaniaTelefonica.longValue(),
                null, // TODO: Agregar ID de usuario cuando esté disponible en el contexto de
                      // seguridad
                String.format("Campaña telefónica creada: %s (Segmento: %d, Agente: %d)",
                        nombre, idSegmento, idAgente));

        return idCampaniaTelefonica;
    }

    /**
     * Actualiza una campaña telefónica existente cuando se edita desde el gestor
     */
    @Transactional
    public void actualizarCampaniaTelefonicaDesdeGestor(
            Long idCampanaGestion,
            String nombre,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            String prioridad) {
        campaniaRepository.findByIdCampanaGestion(idCampanaGestion)
                .ifPresentOrElse(campania -> {
                    log.info("Actualizando campaña telefónica desde gestor: {}", idCampanaGestion);
                    campania.setNombre(nombre);
                    campania.setFechaInicio(fechaInicio);
                    campania.setFechaFin(fechaFin);
                    campania.setPrioridad(mapearPrioridad(prioridad));
                    campania.setFechaModificacion(LocalDateTime.now());
                    campaniaRepository.save(campania);
                    log.info("Campaña telefónica actualizada exitosamente");

                    // AUDITORÍA: Registrar actualización
                    auditoriaService.registrarEvento(
                            ModuloLog.CAMPANIAS_TELEFONICAS,
                            AccionLog.ACTUALIZAR,
                            campania.getId().longValue(),
                            null, // TODO: Agregar ID de usuario
                            String.format("Campaña telefónica actualizada: %s", nombre));
                }, () -> log.warn("No se encontró campaña telefónica para gestión ID: {}", idCampanaGestion));
    }

    /**
     * Elimina una campaña telefónica cuando se elimina desde el gestor
     */
    @Transactional
    public void eliminarCampaniaTelefonicaDesdeGestor(Long idCampanaGestion) {
        campaniaRepository.findByIdCampanaGestion(idCampanaGestion)
                .ifPresent(campania -> {
                    Integer campaniaId = campania.getId();
                    String nombre = campania.getNombre();

                    // Eliminar en cascada (configuración, asignaciones, cola)
                    campaniaRepository.delete(campania);

                    // AUDITORÍA: Registrar eliminación
                    auditoriaService.registrarEvento(
                            ModuloLog.CAMPANIAS_TELEFONICAS,
                            AccionLog.ELIMINAR,
                            campaniaId.longValue(),
                            null, // TODO: Agregar ID de usuario
                            String.format("Campaña telefónica eliminada: %s", nombre));
                });
    }

    @Transactional
    public void activarCampania(Long idCampanaGestion) {
        campaniaRepository.findByIdCampanaGestion(idCampanaGestion)
                .ifPresentOrElse(campania -> {
                    log.info("Activando campaña telefónica desde gestor: {}", idCampanaGestion);
                    String estadoAnterior = campania.getEstado();
                    campania.setEstado("Vigente");
                    campania.setIdEstado(2); // VIGENTE
                    campania.setFechaModificacion(LocalDateTime.now());
                    campaniaRepository.save(campania);
                    log.info("Campaña telefónica activada (Vigente)");

                    // AUDITORÍA: Registrar cambio de estado
                    auditoriaService.registrarEvento(
                            ModuloLog.CAMPANIAS_TELEFONICAS,
                            AccionLog.CAMBIAR_ESTADO,
                            campania.getId().longValue(),
                            null, // TODO: Agregar ID de usuario
                            String.format("Estado cambiado de %s a Vigente", estadoAnterior));
                }, () -> log.warn("No se encontró campaña telefónica para activar. Gestión ID: {}", idCampanaGestion));
    }

    @Transactional
    public void pausarCampania(Long idCampanaGestion) {
        campaniaRepository.findByIdCampanaGestion(idCampanaGestion)
                .ifPresentOrElse(campania -> {
                    log.info("Pausando campaña telefónica desde gestor: {}", idCampanaGestion);
                    String estadoAnterior = campania.getEstado();
                    campania.setEstado("Pausada");
                    campania.setIdEstado(3); // PAUSADA
                    campania.setFechaModificacion(LocalDateTime.now());
                    campaniaRepository.save(campania);
                    log.info("Campaña telefónica pausada");

                    // AUDITORÍA: Registrar cambio de estado
                    auditoriaService.registrarEvento(
                            ModuloLog.CAMPANIAS_TELEFONICAS,
                            AccionLog.CAMBIAR_ESTADO,
                            campania.getId().longValue(),
                            null, // TODO: Agregar ID de usuario
                            String.format("Estado cambiado de %s a Pausada", estadoAnterior));
                }, () -> log.warn("No se encontró campaña telefónica para pausar. Gestión ID: {}", idCampanaGestion));
    }

    @Transactional
    public void reanudarCampania(Long idCampanaGestion) {
        campaniaRepository.findByIdCampanaGestion(idCampanaGestion)
                .ifPresentOrElse(campania -> {
                    log.info("Reanudando campaña telefónica desde gestor: {}", idCampanaGestion);
                    String estadoAnterior = campania.getEstado();
                    campania.setEstado("Vigente");
                    campania.setIdEstado(2); // VIGENTE
                    campania.setFechaModificacion(LocalDateTime.now());
                    campaniaRepository.save(campania);
                    log.info("Campaña telefónica reanudada (Vigente)");

                    // AUDITORÍA: Registrar cambio de estado
                    auditoriaService.registrarEvento(
                            ModuloLog.CAMPANIAS_TELEFONICAS,
                            AccionLog.CAMBIAR_ESTADO,
                            campania.getId().longValue(),
                            null, // TODO: Agregar ID de usuario
                            String.format("Estado cambiado de %s a Vigente (reanudada)", estadoAnterior));
                }, () -> log.warn("No se encontró campaña telefónica para reanudar. Gestión ID: {}", idCampanaGestion));
    }

    @Transactional
    public void cancelarCampania(Long idCampanaGestion) {
        campaniaRepository.findByIdCampanaGestion(idCampanaGestion)
                .ifPresentOrElse(campania -> {
                    log.info("Cancelando campaña telefónica desde gestor: {}", idCampanaGestion);
                    String estadoAnterior = campania.getEstado();
                    campania.setEstado("Cancelada");
                    campania.setIdEstado(5); // CANCELADA
                    campania.setFechaModificacion(LocalDateTime.now());
                    campaniaRepository.save(campania);
                    log.info("Campaña telefónica cancelada");

                    // AUDITORÍA: Registrar cambio de estado
                    auditoriaService.registrarEvento(
                            ModuloLog.CAMPANIAS_TELEFONICAS,
                            AccionLog.CAMBIAR_ESTADO,
                            campania.getId().longValue(),
                            null, // TODO: Agregar ID de usuario
                            String.format("Estado cambiado de %s a Cancelada", estadoAnterior));
                }, () -> log.warn("No se encontró campaña telefónica para cancelar. Gestión ID: {}", idCampanaGestion));
    }

    @Transactional
    public void finalizarCampania(Long idCampanaGestion) {
        campaniaRepository.findByIdCampanaGestion(idCampanaGestion)
                .ifPresentOrElse(campania -> {
                    log.info("Finalizando campaña telefónica desde gestor: {}", idCampanaGestion);
                    String estadoAnterior = campania.getEstado();
                    campania.setEstado("Finalizada");
                    campania.setIdEstado(4); // FINALIZADA
                    campania.setFechaModificacion(LocalDateTime.now());
                    campaniaRepository.save(campania);
                    log.info("Campaña telefónica finalizada");

                    // AUDITORÍA: Registrar cambio de estado
                    auditoriaService.registrarEvento(
                            ModuloLog.CAMPANIAS_TELEFONICAS,
                            AccionLog.CAMBIAR_ESTADO,
                            campania.getId().longValue(),
                            null, // TODO: Agregar ID de usuario
                            String.format("Estado cambiado de %s a Finalizada", estadoAnterior));
                }, () -> log.warn("No se encontró campaña telefónica para finalizar. Gestión ID: {}",
                        idCampanaGestion));
    }

    // === MÉTODOS PRIVADOS ===

    private void crearConfiguracionPorDefecto(Integer idCampaniaTelefonica) {
        CampaniaTelefonicaConfigEntity config = new CampaniaTelefonicaConfigEntity();
        config.setIdCampaniaTelefonica(idCampaniaTelefonica);
        config.setHoraInicioPermitida(LocalTime.of(9, 0));
        config.setHoraFinPermitida(LocalTime.of(21, 0));
        config.setDiasSemanaPermitidos("LUN-MAR-MIE-JUE-VIE");
        config.setMaxIntentos(3);
        config.setIntervaloReintentosMin(60);
        config.setTipoDiscado(CampaniaTelefonicaConfigEntity.TipoDiscadoEnum.Manual);
        config.setModoContacto(CampaniaTelefonicaConfigEntity.ModoContactoEnum.Llamada);
        config.setPermiteSmsRespaldo(false);

        configRepository.save(config);
    }

    private void asignarAgente(Integer idCampaniaTelefonica, Integer idAgente) {
        CampaniaAgenteEntity asignacion = new CampaniaAgenteEntity();
        asignacion.setIdCampania(idCampaniaTelefonica);
        asignacion.setIdAgente(idAgente);

        campaniaAgenteRepository.save(asignacion);
    }

    private void poblarColaLlamadas(Integer idCampaniaTelefonica, Long idSegmento) {
        // Obtener IDs de leads del segmento
        List<Long> leadIds = segmentoService.obtenerMiembrosSegmento(idSegmento);

        // Crear entrada en cola para cada lead
        leadIds.forEach(leadId -> {
            ColaLlamadaEntity cola = new ColaLlamadaEntity();
            cola.setIdCampania(idCampaniaTelefonica);
            cola.setIdLead(leadId);
            cola.setPrioridadCola("MEDIA");
            cola.setEstadoEnCola("PENDIENTE");

            colaLlamadaRepository.save(cola);
        });
    }

    private CampaniaTelefonicaEntity.PrioridadEnum mapearPrioridad(String prioridad) {
        if (prioridad == null)
            return CampaniaTelefonicaEntity.PrioridadEnum.Media;

        return switch (prioridad.toUpperCase()) {
            case "ALTA" -> CampaniaTelefonicaEntity.PrioridadEnum.Alta;
            case "BAJA" -> CampaniaTelefonicaEntity.PrioridadEnum.Baja;
            default -> CampaniaTelefonicaEntity.PrioridadEnum.Media;
        };
    }
}
