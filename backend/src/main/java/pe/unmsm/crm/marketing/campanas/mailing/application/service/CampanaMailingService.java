package pe.unmsm.crm.marketing.campanas.mailing.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.unmsm.crm.marketing.campanas.mailing.api.dto.request.ActualizarContenidoRequest;
import pe.unmsm.crm.marketing.campanas.mailing.api.dto.request.CrearCampanaMailingRequest;
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
        
        if (req.getAsunto() != null) c.setAsunto(req.getAsunto());
        if (req.getCuerpo() != null) c.setCuerpo(req.getCuerpo());
        if (req.getCtaTexto() != null) c.setCtaTexto(req.getCtaTexto());
        
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
}