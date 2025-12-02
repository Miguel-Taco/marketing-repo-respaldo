package pe.unmsm.crm.marketing.campanas.mailing.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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

    public CampanaMailing crearCampana(CrearCampanaMailingRequest req) {
        log.info("Creando campaña: {}", req.getNombre());
        CampanaMailing c = mapper.toEntity(req);
        CampanaMailing saved = campanaRepo.save(c);

        if (req.getPrioridad() == null || req.getPrioridad().trim().isEmpty()) {
            throw new ValidationException("La prioridad es obligatoria");
        }
        MetricaCampana m = MetricaCampana.builder()
                .campanaMailing(saved)
                .enviados(0)
                .build();
        metricasRepo.save(m);

        return saved;
    }

    public List<CampanaMailing> listarPendientes(Integer idAgente) {
        return campanaRepo.findByAgenteAndEstado(idAgente, 1);
    }

    public List<CampanaMailing> listarListos(Integer idAgente) {
        return campanaRepo.findByAgenteAndEstado(idAgente, 2);
    }

    public List<CampanaMailing> listarEnviados(Integer idAgente) {
        return campanaRepo.findByAgenteAndEstado(idAgente, 3);
    }

    public List<CampanaMailing> listarFinalizados(Integer idAgente) {
        return campanaRepo.findByAgenteAndEstado(idAgente, 5);
    }

    public List<CampanaMailing> listarTodas(Integer idAgente) {
        return campanaRepo.findByAgenteOrderByFechaInicio(idAgente);
    }

    public CampanaMailing obtenerDetalle(Integer id) {
        return campanaRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("CampanaMailing", id.longValue()));
    }

    public void guardarBorrador(Integer id, ActualizarContenidoRequest req) {
        CampanaMailing c = obtenerDetalle(id);

        if (req.getAsunto() != null)
            c.setAsunto(req.getAsunto());
        if (req.getCuerpo() != null)
            c.setCuerpo(req.getCuerpo());
        if (req.getCtaTexto() != null)
            c.setCtaTexto(req.getCtaTexto());

        if (c.getIdEstado().equals(2)) {
            log.info("Campaña {} estaba en LISTO, regresando a PENDIENTE por edición", id);
            c.setIdEstado(1);
        }

        campanaRepo.save(c);
        log.info("Borrador guardado para campaña {}", id);
    }

    public void marcarListo(Integer id) {
        CampanaMailing c = obtenerDetalle(id);

        if (c.getAsunto() == null || c.getAsunto().trim().isEmpty()) {
            throw new ValidationException("Asunto es obligatorio");
        }
        if (c.getCuerpo() == null || c.getCuerpo().trim().isEmpty()) {
            throw new ValidationException("Cuerpo es obligatorio");
        }
        if (c.getCtaTexto() == null || c.getCtaTexto().trim().isEmpty()) {
            throw new ValidationException("Texto CTA es obligatorio");
        }

        c.setIdEstado(2); // LISTO
        campanaRepo.save(c);
        log.info("Campaña {} marcada como LISTO", id);
    }

    public MetricasMailingResponse obtenerMetricas(Integer id) {
        MetricaCampana m = metricasRepo.findByCampanaMailingId(id)
                .orElseThrow(() -> new NotFoundException("Métricas", id.longValue()));
        return mapper.toMetricasResponse(m);
    }

    public void pausarPorGestor(Long idCampanaGestion) {
        log.info("Pausando campaña desde Gestor: {}", idCampanaGestion);

        CampanaMailing c = campanaRepo.findByIdCampanaGestion(idCampanaGestion)
                .orElseThrow(() -> new NotFoundException(
                        "CampanaMailing con idCampanaGestion",
                        idCampanaGestion));

        // Ya está cancelada/pausada?
        if (c.getIdEstado().equals(6)) {
            log.info("Campaña {} ya estaba cancelada", c.getId());
            return;
        }

        // Si ya fue ENVIADA (3), no se puede pausar
        if (c.getIdEstado().equals(3)) {
            log.warn("Campaña {} ya fue ENVIADA, no se puede pausar. Se marca como CANCELADA de todas formas.",
                    c.getId());
        }

        // Cambiar a CANCELADO (6)
        c.setIdEstado(6);
        campanaRepo.save(c);

        log.info("✓ Campaña {} pausada por Gestor (estado=CANCELADO)", c.getId());
    }

    public void cancelarPorGestor(Long idCampanaGestion) {
        log.info("Cancelando campaña desde Gestor: {}", idCampanaGestion);

        CampanaMailing c = campanaRepo.findByIdCampanaGestion(idCampanaGestion)
                .orElseThrow(() -> new NotFoundException(
                        "CampanaMailing con idCampanaGestion",
                        idCampanaGestion));

        // Ya está cancelada?
        if (c.getIdEstado().equals(6)) {
            log.info("Campaña {} ya estaba cancelada", c.getId());
            return;
        }

        // Cambiar a CANCELADO (6)
        c.setIdEstado(6);
        campanaRepo.save(c);

        log.info("✓ Campaña {} cancelada por Gestor (idCampanaGestion={})",
                c.getId(), idCampanaGestion);
    }

    public void reprogramarPorGestor(Long idCampanaGestion, ReprogramarCampanaRequest req) {
        log.info("Reprogramando campaña desde Gestor: {}", idCampanaGestion);

        CampanaMailing c = campanaRepo.findByIdCampanaGestion(idCampanaGestion)
                .orElseThrow(() -> new NotFoundException(
                        "CampanaMailing con idCampanaGestion",
                        idCampanaGestion));

        // Validar fechas
        if (req.getFechaInicio().isAfter(req.getFechaFin())) {
            throw new ValidationException("Fecha de inicio debe ser anterior a fecha de fin");
        }

        // Actualizar fechas
        c.setFechaInicio(req.getFechaInicio());
        c.setFechaFin(req.getFechaFin());

        // Si estaba CANCELADA (6), volver a PENDIENTE (1)
        if (c.getIdEstado().equals(6)) {
            c.setIdEstado(1);
            log.info("Campaña {} reprogramada: CANCELADO → PENDIENTE", c.getId());
        }

        // Si estaba VENCIDA (4), volver a PENDIENTE (1)
        if (c.getIdEstado().equals(4)) {
            c.setIdEstado(1);
            log.info("Campaña {} reprogramada: VENCIDO → PENDIENTE", c.getId());
        }

        // Si estaba en LISTO (2), volver a PENDIENTE (1) para que revise el contenido
        if (c.getIdEstado().equals(2)) {
            c.setIdEstado(1);
            log.info("Campaña {} reprogramada: LISTO → PENDIENTE (debe revisar contenido)", c.getId());
        }

        // Si ya fue ENVIADA (3) o FINALIZADA (5), no se puede reprogramar
        if (c.getIdEstado().equals(3) || c.getIdEstado().equals(5)) {
            throw new ValidationException(
                    "No se puede reprogramar una campaña en estado " +
                            EstadoCampanaMailing.fromId(c.getIdEstado()).getNombre());
        }

        campanaRepo.save(c);

        log.info("✓ Campaña {} reprogramada: {} a {}",
                c.getId(), req.getFechaInicio(), req.getFechaFin());
    }
}