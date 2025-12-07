package pe.unmsm.crm.marketing.campanas.mailing.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.unmsm.crm.marketing.campanas.mailing.domain.model.CampanaMailing;
import pe.unmsm.crm.marketing.campanas.mailing.domain.model.EventoInteraccion;
import pe.unmsm.crm.marketing.campanas.mailing.domain.model.MetricaCampana;
import pe.unmsm.crm.marketing.campanas.mailing.domain.port.output.IMailingPort;
import pe.unmsm.crm.marketing.campanas.mailing.domain.port.output.ISegmentoPort;
import pe.unmsm.crm.marketing.campanas.mailing.domain.service.ValidacionMailingService;
import pe.unmsm.crm.marketing.campanas.mailing.infra.persistence.repository.JpaCampanaMailingRepository;
import pe.unmsm.crm.marketing.campanas.mailing.infra.persistence.repository.JpaMetricaMailingRepository;
import pe.unmsm.crm.marketing.shared.infra.exception.ExternalServiceException;
import pe.unmsm.crm.marketing.shared.infra.exception.NotFoundException;
import pe.unmsm.crm.marketing.shared.infra.exception.ValidationException;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class EnvioMailingService {

    private final JpaCampanaMailingRepository campanaRepo;
    private final JpaMetricaMailingRepository metricasRepo;
    private final IMailingPort mailPort;
    private final ISegmentoPort segmentoPort;
    private final ValidacionMailingService validacionService;

    /**
     * Envía una campaña de mailing
     * 1. Valida que esté en estado LISTO
     * 2. Obtiene emails del segmento
     * 3. Envía via Resend
     * 4. Actualiza estado a ENVIADO
     */
    public void enviarCampana(Integer idCampana) {
        log.info("Iniciando envío de campaña: {}", idCampana);
        
        // Obtener campaña
        CampanaMailing campana = campanaRepo.findById(idCampana)
                .orElseThrow(() -> new NotFoundException("CampanaMailing", idCampana.longValue()));
        
        // Validar que pueda enviarse
        validacionService.validarParaEnviar(campana);
        
        try {
            // Obtener emails del segmento
            List<String> emails = segmentoPort.obtenerEmailsSegmento(campana.getIdSegmento());
            
            if (emails.isEmpty()) {
                throw new ValidationException("El segmento no tiene emails para enviar");
            }
            
            log.info("Enviando {} emails para campaña {}", emails.size(), idCampana);
            
            // Enviar via SendGrid
            mailPort.enviarEmails(campana, emails);
            
            // Actualizar estado a ENVIADO (3)
            campana.setIdEstado(3);
            campanaRepo.save(campana);
            
            // Actualizar métricas: cantidad enviada
            MetricaCampana metricas = metricasRepo.findByCampanaMailingId(idCampana)
                    .orElseThrow(() -> new NotFoundException("Métricas", idCampana.longValue()));
            
            metricas.setEnviados(emails.size());
            metricasRepo.save(metricas);
            
            log.info("✓ Campaña {} enviada exitosamente a {} destinatarios", idCampana, emails.size());
            
        } catch (ExternalServiceException e) {
            log.error("Error en servicio externo al enviar campaña {}: {}", idCampana, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado enviando campaña {}: {}", idCampana, e.getMessage(), e);
            throw new RuntimeException("Error al enviar campaña: " + e.getMessage(), e);
        }
    }

    /**
     * Envía múltiples campañas (usado por scheduler)
     */
    public void enviarCampanas(List<CampanaMailing> campanas) {
        for (CampanaMailing c : campanas) {
            try {
                enviarCampana(c.getId());
            } catch (Exception e) {
                log.error("Error enviando campaña {}: continuando con siguiente", c.getId(), e);
                // Continuar con siguiente campaña
            }
        }
    }

    /**
     * Obtiene preview de cuántos emails se enviarían
     */
    public Integer obtenerCantidadDestinatarios(Integer idCampana) {
        CampanaMailing campana = campanaRepo.findById(idCampana)
                .orElseThrow(() -> new NotFoundException("CampanaMailing", idCampana.longValue()));
        
        return segmentoPort.contarMiembros(campana.getIdSegmento());
    }
}