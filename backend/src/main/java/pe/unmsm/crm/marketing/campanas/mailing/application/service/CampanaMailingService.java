package pe.unmsm.crm.marketing.campanas.mailing.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.unmsm.crm.marketing.campanas.mailing.api.dto.request.ActualizarContenidoRequest;
import pe.unmsm.crm.marketing.campanas.mailing.api.dto.request.CrearCampanaMailingRequest;
import pe.unmsm.crm.marketing.campanas.mailing.api.dto.request.ReprogramarCampanaRequest;
import pe.unmsm.crm.marketing.campanas.mailing.api.dto.response.MetricasMailingResponse;
import pe.unmsm.crm.marketing.campanas.mailing.application.mapper.MailingMapper;
import pe.unmsm.crm.marketing.campanas.mailing.domain.model.*;
import pe.unmsm.crm.marketing.campanas.mailing.infra.persistence.repository.*;
import pe.unmsm.crm.marketing.shared.infra.exception.*;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CampanaMailingService {

    private final JpaCampanaMailingRepository campanaRepo;
    private final JpaMetricaMailingRepository metricasRepo;
    private final MailingMapper mapper;

    // ========================================================================
    // OPERACIONES DE ESCRITURA (Invalidan caché)
    // ========================================================================

    /**
     * Crea una nueva campaña de mailing.
     * Invalida el caché de listados porque hay una nueva campaña pendiente.
     */
    @CacheEvict(value = "mailing_campanias_lista", allEntries = true)
    public CampanaMailing crearCampana(CrearCampanaMailingRequest req) {
        log.info(" Creando campaña: {}", req.getNombre());
        
        if (req.getPrioridad() == null || req.getPrioridad().trim().isEmpty()) {
            throw new ValidationException("La prioridad es obligatoria");
        }
        
        CampanaMailing c = mapper.toEntity(req);
        CampanaMailing saved = campanaRepo.save(c);

        // Crear métricas iniciales
        MetricaCampana m = MetricaCampana.builder()
                .campanaMailing(saved)
                .enviados(0)
                .entregados(0)
                .aperturas(0)
                .clics(0)
                .rebotes(0)
                .bajas(0)
                .build();
        metricasRepo.save(m);

        log.info("✓ Campaña {} creada con ID: {}", req.getNombre(), saved.getId());
        return saved;
    }

    /**
     * Guarda borrador de una campaña.
     * Invalida el caché del detalle de esa campaña específica.
     */
    @Caching(evict = {
        @CacheEvict(value = "mailing_campania_detalle", key = "#id"),
        @CacheEvict(value = "mailing_campanias_lista", allEntries = true)
    })
    public void guardarBorrador(Integer id, ActualizarContenidoRequest req) {
        log.info(" Guardando borrador para campaña {}", id);
        
        CampanaMailing c = obtenerDetalleSinCache(id);

        if (req.getAsunto() != null)
            c.setAsunto(req.getAsunto());
        if (req.getCuerpo() != null)
            c.setCuerpo(req.getCuerpo());
        if (req.getCtaTexto() != null)
            c.setCtaTexto(req.getCtaTexto());

        // Si estaba en LISTO, regresa a PENDIENTE
        if (c.getIdEstado().equals(2)) {
            log.info("  Campaña {} regresando de LISTO a PENDIENTE por edición", id);
            c.setIdEstado(1);
        }

        campanaRepo.save(c);
        log.info("✓ Borrador guardado para campaña {}", id);
    }

    /**
     * Marca una campaña como LISTO.
     * Invalida cachés de listado y detalle.
     */
    @Caching(evict = {
        @CacheEvict(value = "mailing_campania_detalle", key = "#id"),
        @CacheEvict(value = "mailing_campanias_lista", allEntries = true)
    })
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void marcarListo(Integer id) {
        log.info(" Marcando campaña {} como LISTO", id);
        
        CampanaMailing c = obtenerDetalleSinCache(id);

        // Validar contenido completo
        validarContenidoCompleto(c);

        c.setIdEstado(2); // LISTO
        campanaRepo.save(c);
        
        log.info("✓ Campaña {} marcada como LISTO", id);
    }

    /**
     * Pausa una campaña desde el Gestor.
     */
    @Caching(evict = {
        @CacheEvict(value = "mailing_campania_detalle", allEntries = true),
        @CacheEvict(value = "mailing_campanias_lista", allEntries = true)
    })
    public void pausarPorGestor(Long idCampanaGestion) {
        log.info("⏸ Pausando campaña desde Gestor: {}", idCampanaGestion);

        CampanaMailing c = campanaRepo.findByIdCampanaGestion(idCampanaGestion)
                .orElseThrow(() -> new NotFoundException(
                        "CampanaMailing con idCampanaGestion", idCampanaGestion));

        if (c.getIdEstado().equals(6)) {
            log.info("  Campaña {} ya estaba cancelada", c.getId());
            return;
        }

        c.setIdEstado(6);
        campanaRepo.save(c);

        log.info("✓ Campaña {} pausada por Gestor (estado=CANCELADO)", c.getId());
    }

    /**
     * Cancela una campaña desde el Gestor.
     */
    @Caching(evict = {
        @CacheEvict(value = "mailing_campania_detalle", allEntries = true),
        @CacheEvict(value = "mailing_campanias_lista", allEntries = true)
    })
    public void cancelarPorGestor(Long idCampanaGestion) {
        log.info(" Cancelando campaña desde Gestor: {}", idCampanaGestion);

        CampanaMailing c = campanaRepo.findByIdCampanaGestion(idCampanaGestion)
                .orElseThrow(() -> new NotFoundException(
                        "CampanaMailing con idCampanaGestion", idCampanaGestion));

        if (c.getIdEstado().equals(6)) {
            log.info("  Campaña {} ya estaba cancelada", c.getId());
            return;
        }

        c.setIdEstado(6); // CANCELADO
        campanaRepo.save(c);

        log.info("✓ Campaña {} cancelada (idCampanaGestion={})", c.getId(), idCampanaGestion);
    }

    /**
     * Reprograma una campaña desde el Gestor.
     */
    @Caching(evict = {
        @CacheEvict(value = "mailing_campania_detalle", allEntries = true),
        @CacheEvict(value = "mailing_campanias_lista", allEntries = true)
    })
    public void reprogramarPorGestor(Long idCampanaGestion, ReprogramarCampanaRequest req) {
        log.info(" Reprogramando campaña desde Gestor: {}", idCampanaGestion);

        CampanaMailing c = campanaRepo.findByIdCampanaGestion(idCampanaGestion)
                .orElseThrow(() -> new NotFoundException(
                        "CampanaMailing con idCampanaGestion", idCampanaGestion));

        if (req.getFechaInicio().isAfter(req.getFechaFin())) {
            throw new ValidationException("Fecha de inicio debe ser anterior a fecha de fin");
        }

        if (c.getIdEstado().equals(3) || c.getIdEstado().equals(5)) {
            throw new ValidationException(
                    "No se puede reprogramar una campaña en estado " +
                            EstadoCampanaMailing.fromId(c.getIdEstado()).getNombre());
        }

        c.setFechaInicio(req.getFechaInicio());
        c.setFechaFin(req.getFechaFin());

        if (c.getIdEstado().equals(6) || c.getIdEstado().equals(4) || c.getIdEstado().equals(2)) {
            c.setIdEstado(1);
            log.info("  Campaña {} regresada a PENDIENTE", c.getId());
        }

        campanaRepo.save(c);
        log.info("✓ Campaña {} reprogramada: {} a {}", c.getId(), req.getFechaInicio(), req.getFechaFin());
    }

    // ========================================================================
    // OPERACIONES DE LECTURA (AQUÍ VA @Cacheable)
    // ========================================================================

    /**
     * Lista campañas en estado PENDIENTE - CACHEADO 10 minutos
     */
    @Cacheable(value = "mailing_campanias_lista", key = "'pendientes_' + #campaniasPermitidas.hashCode()")
    @Transactional(readOnly = true)
    public List<CampanaMailing> listarPendientes(List<Integer> campaniasPermitidas) {
        log.info("📋 Consultando campañas PENDIENTES");
        if (isEmpty(campaniasPermitidas)) {
            return List.of();
        }
        return campanaRepo.findByIdInAndIdEstado(campaniasPermitidas, 1);
    }

    /**
     * Lista campañas en estado LISTO - CACHEADO 10 minutos
     */
    @Cacheable(value = "mailing_campanias_lista", key = "'listos_' + #campaniasPermitidas.hashCode()")
    @Transactional(readOnly = true)
    public List<CampanaMailing> listarListos(List<Integer> campaniasPermitidas) {
        log.info(" Consultando campañas LISTAS");
        if (isEmpty(campaniasPermitidas)) {
            return List.of();
        }
        return campanaRepo.findByIdInAndIdEstado(campaniasPermitidas, 2);
    }

    /**
     * Lista campañas en estado ENVIADO - CACHEADO 10 minutos
     */
    @Cacheable(value = "mailing_campanias_lista", key = "'enviados_' + #campaniasPermitidas.hashCode()")
    @Transactional(readOnly = true)
    public List<CampanaMailing> listarEnviados(List<Integer> campaniasPermitidas) {
        log.info(" Consultando campañas ENVIADAS");
        if (isEmpty(campaniasPermitidas)) {
            return List.of();
        }
        return campanaRepo.findByIdInAndIdEstado(campaniasPermitidas, 3);
    }

    /**
     * Lista campañas en estado FINALIZADO - CACHEADO 10 minutos
     */
    @Cacheable(value = "mailing_campanias_lista", key = "'finalizados_' + #campaniasPermitidas.hashCode()")
    @Transactional(readOnly = true)
    public List<CampanaMailing> listarFinalizados(List<Integer> campaniasPermitidas) {
        log.info(" Consultando campañas FINALIZADAS");
        if (isEmpty(campaniasPermitidas)) {
            return List.of();
        }
        return campanaRepo.findByIdInAndIdEstado(campaniasPermitidas, 5);
    }

    /**
     * Lista todas las campañas - CACHEADO 10 minutos
     */
    @Cacheable(value = "mailing_campanias_lista", key = "'todas_' + #campaniasPermitidas.hashCode()")
    @Transactional(readOnly = true)
    public List<CampanaMailing> listarTodas(List<Integer> campaniasPermitidas) {
        log.info(" Consultando TODAS las campañas");
        if (isEmpty(campaniasPermitidas)) {
            return List.of();
        }
        return campanaRepo.findByIdInOrderByFechaInicio(campaniasPermitidas);
    }

    /**
     * Obtiene detalle de una campaña - CACHEADO 10 minutos
     */
    @Cacheable(value = "mailing_campania_detalle", key = "#id")
    @Transactional(readOnly = true)
    public CampanaMailing obtenerDetalle(Integer id) {
        log.info(" Consultando detalle de campaña {}", id);
        return obtenerDetalleSinCache(id);
    }

    /**
     * Obtiene métricas de una campaña - CACHEADO 10 minutos
     */
    @Cacheable(value = "mailing_metricas", key = "#id")
    @Transactional(readOnly = true)
    public MetricasMailingResponse obtenerMetricas(Integer id) {
        log.info(" Consultando métricas de campaña {}", id);
        MetricaCampana m = metricasRepo.findByCampanaMailingId(id)
                .orElseThrow(() -> new NotFoundException("Métricas", id.longValue()));
        return mapper.toMetricasResponse(m);
    }

    // ========================================================================
    // MÉTODOS AUXILIARES
    // ========================================================================

    /**
     * Obtiene detalle sin usar caché (para uso interno en operaciones de escritura)
     */
    private CampanaMailing obtenerDetalleSinCache(Integer id) {
        return campanaRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("CampanaMailing", id.longValue()));
    }

    private boolean isEmpty(List<Integer> ids) {
        return ids == null || ids.isEmpty();
    }

    private void validarContenidoCompleto(CampanaMailing c) {
        if (c.getAsunto() == null || c.getAsunto().trim().isEmpty()) {
            throw new ValidationException("Asunto es obligatorio");
        }
        if (c.getCuerpo() == null || c.getCuerpo().trim().isEmpty()) {
            throw new ValidationException("Cuerpo es obligatorio");
        }
        if (c.getCtaTexto() == null || c.getCtaTexto().trim().isEmpty()) {
            throw new ValidationException("Texto CTA es obligatorio");
        }
    }
}
