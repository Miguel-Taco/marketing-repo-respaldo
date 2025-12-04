package pe.unmsm.crm.marketing.campanas.mailing.infra.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.unmsm.crm.marketing.campanas.mailing.domain.model.CampanaMailing;
import pe.unmsm.crm.marketing.campanas.mailing.domain.model.MetricaCampana;
import pe.unmsm.crm.marketing.campanas.mailing.domain.port.output.IGestorCampanaPort;
import pe.unmsm.crm.marketing.campanas.mailing.domain.port.output.IMailingSendGridPort;
import pe.unmsm.crm.marketing.campanas.mailing.domain.port.output.ISegmentoPort;
import pe.unmsm.crm.marketing.campanas.mailing.infra.persistence.repository.JpaCampanaMailingRepository;
import pe.unmsm.crm.marketing.campanas.mailing.infra.persistence.repository.JpaMetricaMailingRepository;
import pe.unmsm.crm.marketing.shared.infra.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Component
@org.springframework.context.annotation.Profile("!console")
@RequiredArgsConstructor
@Slf4j
public class MailingScheduler {

    private final JpaCampanaMailingRepository campanaRepo;
    private final JpaMetricaMailingRepository metricasRepo;
    private final IMailingSendGridPort sendGridPort;
    private final ISegmentoPort segmentoPort;
    private final IGestorCampanaPort gestorPort;

    // Ejecuta cada 5 minutos (300000 ms)
    @Scheduled(fixedDelay = 300000)
    @Transactional
    public void ejecutarEnviosCada5Min() {
        log.info("=== SCHEDULER: Iniciando búsqueda de campañas para enviar ===");

        LocalDateTime ahora = LocalDateTime.now();

        // Tarea 1:Enviar campañas LISTO que ya llegaron a fecha_inicio
        enviarCampanasListas(ahora);

        // Tarea 2:Marcar como VENCIDO las PENDIENTE que pasaron fecha_inicio
        marcarComoVencidas(ahora);

        log.info("=== SCHEDULER: Finalizado ===");
    }

    private void enviarCampanasListas(LocalDateTime ahora) {
        List<CampanaMailing> listosParaEnviar = campanaRepo.findListosParaEnviar(2, ahora);

        log.info("Campañas listas para enviar: {}", listosParaEnviar.size());

        for (CampanaMailing c : listosParaEnviar) {
            try {
                log.info("Enviando campaña ID: {} - Nombre: {}", c.getId(), c.getNombre());

                // Obtener emails del segmento
                List<String> emails = segmentoPort.obtenerEmailsSegmento(c.getIdSegmento());

                if (emails.isEmpty()) {
                    log.warn("Segmento {} sin emails, saltando campaña", c.getIdSegmento());
                    continue;
                }

                // Enviar via SendGrid
                sendGridPort.enviarEmails(c, emails);

                // Cambiar a ENVIADO (estado 3)
                c.setIdEstado(3);

                MetricaCampana metricas = metricasRepo.findByCampanaMailingId(c.getId())
                        .orElseThrow(() -> new NotFoundException("Métricas", c.getId().longValue()));

                metricas.setEnviados(emails.size());
                metricasRepo.save(metricas);

                campanaRepo.save(c);
                log.info("✓ Campaña {} enviada exitosamente a {} destinatarios", c.getId(), emails.size());

            } catch (Exception e) {
                log.error("✗ Error enviando campaña {}: {}", c.getId(), e.getMessage(), e);
            }
        }
    }

    private void marcarComoVencidas(LocalDateTime ahora) {
        List<CampanaMailing> vencidas = campanaRepo.findVencidas(ahora);

        log.info("Campañas vencidas encontradas: {}", vencidas.size());

        for (CampanaMailing c : vencidas) {
            try {
                log.warn("Campaña {} vencida. Pasó fecha_inicio sin estar LISTO", c.getId());

                // Cambiar a VENCIDO (estado 4)
                c.setIdEstado(4);
                campanaRepo.save(c);

                // Notificar al Gestor para que pause
                gestorPort.pausarCampana(c.getIdCampanaGestion(),
                        "Campaña vencida: pasó fecha_inicio sin estar en estado LISTO");

                log.info("✓ Campaña {} marcada como VENCIDO y notificada al Gestor", c.getId());

            } catch (Exception e) {
                log.error("✗ Error procesando vencida {}: {}", c.getId(), e.getMessage(), e);
            }
        }
    }
}