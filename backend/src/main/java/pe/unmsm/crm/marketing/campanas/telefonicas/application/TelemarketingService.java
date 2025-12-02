package pe.unmsm.crm.marketing.campanas.telefonicas.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.unmsm.crm.marketing.campanas.telefonicas.api.dto.*;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.command.RegistrarResultadoCommand;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.command.CallCommandBus;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.event.CallResultRegisteredEvent;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.event.TelemarketingEventPublisher;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.handler.CallResultHandler;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.model.*;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.CampaignDataProvider;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.model.CallStatus;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.strategy.assignment.CallAssignmentStrategy;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.strategy.retry.RetryStrategy;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.memento.ScriptSession;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.memento.ScriptSessionMemento;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.ScriptSessionStore;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.metrics.TelemarketingMetrics;

import java.util.List;

/**
 * Servicio de logica de negocio para campanas telefonicas.
 * Combina datos del proveedor mock y del proveedor in-memory.
 */
@Service
@RequiredArgsConstructor
public class TelemarketingService {

    private final CampaignDataProvider campaignDataProvider;
    private final CallCommandBus callCommandBus;
    private final CallResultHandler callResultHandlerChain;
    private final CallAssignmentStrategy callAssignmentStrategy;
    private final RetryStrategy retryStrategy;
    private final TelemarketingEventPublisher eventPublisher;
    private final ScriptSessionStore scriptSessionStore;
    private final TelemarketingMetrics telemarketingMetrics;
    private final pe.unmsm.crm.marketing.campanas.telefonicas.application.service.EncuestaLlamadaService encuestaLlamadaService;

    // === CAMPANAS ===

    public List<CampaniaTelefonicaDTO> obtenerCampaniasPorAgente(Long idAgente) {
        return campaignDataProvider.obtenerCampaniasPorAgente(idAgente);
    }

    public CampaniaTelefonicaDTO obtenerCampaniaPorId(Long id) {
        return campaignDataProvider.obtenerCampaniaPorId(id);
    }

    public CampaniaTelefonicaDTO crearCampania(CreateCampaniaTelefonicaRequest request) {
        return campaignDataProvider.crearCampania(request);
    }

    // === CONTACTOS ===

    public List<ContactoDTO> obtenerContactosDeCampania(Long idCampania) {
        return campaignDataProvider.obtenerContactosDeCampania(idCampania);
    }

    public ContactoDTO obtenerContactoPorId(Long idContacto) {
        return campaignDataProvider.obtenerContactoPorId(idContacto);
    }

    // === COLA ===

    public List<ContactoDTO> obtenerCola(Long idCampania) {
        return campaignDataProvider.obtenerCola(idCampania);
    }

    public ContactoDTO obtenerSiguienteContacto(Long idCampania, Long idAgente) {
        CampaniaTelefonicaDTO campania = obtenerCampaniaPorId(idCampania);
        if (campania == null)
            return null;

        // Delegar la lógica de "siguiente" al provider si es posible,
        // o mantener la estrategia de asignación si es compleja.
        // El JpaCampaignDataProvider ya implementa obtenerSiguienteContacto con query
        // optimizada.
        // Sin embargo, la estrategia de asignación (CallAssignmentStrategy) podría
        // tener lógica extra.
        // Por ahora, delegamos al provider que es lo más eficiente en BD.
        return campaignDataProvider.obtenerSiguienteContacto(idCampania, idAgente);
    }

    public ContactoDTO tomarContacto(Long idCampania, Long idContacto, Long idAgente) {
        return campaignDataProvider.tomarContacto(idCampania, idContacto, idAgente);
    }

    public void pausarCola(Long idAgente, Long idCampania) {
        campaignDataProvider.pausarCola(idAgente, idCampania);
    }

    public void reanudarCola(Long idAgente, Long idCampania) {
        campaignDataProvider.reanudarCola(idAgente, idCampania);
    }

    // === LLAMADAS ===

    public LlamadaDTO obtenerLlamada(Long idLlamada) {
        return campaignDataProvider.obtenerLlamada(idLlamada);
    }

    public LlamadaDTO registrarResultadoLlamada(Long idCampania, Long idAgente, ResultadoLlamadaRequest request) {
        CampaniaTelefonicaDTO campania = obtenerCampaniaPorId(idCampania);
        if (campania == null)
            return null;

        CallOutcome outcome = null;
        try {
            outcome = CallOutcome.valueOf(request.getResultado());
        } catch (Exception ignored) {
            // Mantener null si no coincide con el enum
        }

        CallContext context = CallContext.builder()
                .campaniaId(idCampania)
                .agenteId(idAgente)
                .contactoId(request.getIdContacto())
                .estado(CallStatus.CERRADO)
                .resultadoEsperado(outcome)
                .build();

        RegistrarResultadoCommand command = new RegistrarResultadoCommand(
                context,
                () -> campaignDataProvider.registrarResultadoLlamada(idCampania, idAgente, request));

        long start = System.nanoTime();
        callCommandBus.enqueue(command);
        callCommandBus.processPending(); // mantener contrato sincrónico
        callResultHandlerChain.handle(request, context);
        // List<RetryPlanStep> plan = retryStrategy.planRetries(request, 0); //
        // precálculo; integración futura
        telemarketingMetrics.recordCallResult(request.getResultado(), System.nanoTime() - start);
        eventPublisher.publish(new CallResultRegisteredEvent(idCampania, idAgente, command.getResultado()));
        return command.getResultado();
    }

    // === SESION DE GUION (MEMENTO) ===

    public ScriptSessionDTO guardarSesionGuion(Long idLlamada, Long idAgente, ScriptSessionRequest request) {
        ScriptSession session = ScriptSession.builder()
                .llamadaId(idLlamada)
                .agenteId(idAgente)
                .pasoActual(request.getPasoActual())
                .respuestas(request.getRespuestas())
                .actualizadoEn(java.time.LocalDateTime.now())
                .build();
        ScriptSessionMemento memento = session.createMemento();
        scriptSessionStore.save(memento);
        return toDto(memento);
    }

    public ScriptSessionDTO obtenerSesionGuion(Long idLlamada, Long idAgente) {
        ScriptSessionMemento memento = scriptSessionStore.get(idLlamada);
        if (memento == null || (memento.getAgenteId() != null && !memento.getAgenteId().equals(idAgente))) {
            return null;
        }
        return toDto(memento);
    }

    private ScriptSessionDTO toDto(ScriptSessionMemento memento) {
        return ScriptSessionDTO.builder()
                .llamadaId(memento.getLlamadaId())
                .agenteId(memento.getAgenteId())
                .pasoActual(memento.getPasoActual())
                .respuestas(memento.getRespuestas())
                .actualizadoEn(memento.getActualizadoEn())
                .build();
    }

    public List<LlamadaDTO> obtenerHistorialLlamadas(Long idCampania, Long idAgente) {
        return campaignDataProvider.obtenerHistorialLlamadas(idCampania, idAgente);
    }

    /**
     * Obtiene los detalles del envío de encuesta para una llamada específica.
     */
    public EnvioEncuestaDTO obtenerDetalleEncuesta(Integer idLlamada) {
        return encuestaLlamadaService.obtenerDetalleEnvio(idLlamada);
    }

    // === GUIONES ===

    public GuionDTO obtenerGuionDeCampania(Long idCampania) {
        return campaignDataProvider.obtenerGuionDeCampania(idCampania);
    }

    public List<GuionDTO> listarTodosLosGuiones() {
        return campaignDataProvider.listarTodosLosGuiones();
    }

    // === METRICAS ===

    public MetricasAgenteDTO obtenerMetricasAgente(Long idCampania, Long idAgente) {
        return campaignDataProvider.obtenerMetricasAgente(idCampania, idAgente);
    }

    public MetricasDiariasDTO obtenerMetricasDiarias(Long idCampania, Long idAgente) {
        return campaignDataProvider.obtenerMetricasDiarias(idCampania, idAgente);
    }

    public MetricasCampaniaDTO obtenerMetricasCampania(Long idCampania, Integer dias) {
        return campaignDataProvider.obtenerMetricasCampania(idCampania, dias);
    }

    // === CONTACTOS URGENTES (INTEGRACION CON GESTOR DE ENCUESTAS) ===

    /**
     * Agrega un contacto urgente a la cola con prioridad ALTA.
     * La campaña se determina automáticamente a partir del id_encuesta.
     * 
     * @param request Solicitud con idLead e idEncuesta
     * @return ContactoDTO del lead agregado a la cola
     * @throws IllegalArgumentException si no existe campaña para la encuesta
     */
    public ContactoDTO agregarContactoUrgente(AddUrgentContactRequest request) {
        return campaignDataProvider.agregarContactoUrgente(request);
    }
}
