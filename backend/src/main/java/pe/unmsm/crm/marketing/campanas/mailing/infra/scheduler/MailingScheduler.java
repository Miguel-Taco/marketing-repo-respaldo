package pe.unmsm.crm.marketing.campanas.mailing.infra.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
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
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Scheduler para envío automático de campañas de mailing.
 * 
 * Ejecuta cada 5 minutos y realiza 3 tareas:
 * 
 * 1. ENVIAR: Campañas en estado LISTO cuya fecha_inicio ya llegó
 *    - Obtiene emails del segmento
 *    - Envía via RESEND (antes era SendGrid)
 *    - Cambia estado a ENVIADO
 *    - Actualiza métricas con cantidad enviada
 * 
 * 2. VENCER: Campañas en estado PENDIENTE cuya fecha_inicio ya pasó
 *    - Cambia estado a VENCIDO
 *    - Notifica al Gestor de Campañas para pausar
 * 
 * 3. FINALIZAR: Campañas en estado ENVIADO cuya fecha_fin ya pasó
 *    - Cambia estado a FINALIZADO
 * 
 * NOTA: La inyección de IMailingSendGridPort ahora inyecta ResendMailAdapter
 * (el nombre de la interfaz se mantiene por compatibilidad)
 */
@Component
@Profile("!console")  // No ejecutar en modo consola/tests
@RequiredArgsConstructor
@Slf4j
public class MailingScheduler {

    private final JpaCampanaMailingRepository campanaRepo;
    private final JpaMetricaMailingRepository metricasRepo;
    private final IMailingSendGridPort mailAdapter;  // → ResendMailAdapter
    private final ISegmentoPort segmentoPort;
    private final IGestorCampanaPort gestorPort;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Ejecuta cada 5 minutos (300000 ms)
     */
    @Scheduled(fixedDelay = 300000)
    @Transactional
    public void ejecutarTareasScheduler() {
        LocalDateTime ahora = LocalDateTime.now();
        
        log.info("╔════════════════════════════════════════════════════════════════╗");
        log.info("║              SCHEDULER MAILING - RESEND                        ║");
        log.info("╠════════════════════════════════════════════════════════════════╣");
        log.info("║  Hora actual: {}", ahora.format(DTF));
        log.info("╚════════════════════════════════════════════════════════════════╝");

        // Tarea 1: Enviar campañas LISTO que llegaron a fecha_inicio
        enviarCampanasListas(ahora);

        // Tarea 2: Marcar como VENCIDO las PENDIENTE que pasaron fecha_inicio
        marcarComoVencidas(ahora);

        // Tarea 3: Marcar como FINALIZADO las ENVIADO que pasaron fecha_fin
        marcarComoFinalizadas(ahora);

        log.info("════════════════════════════════════════════════════════════════");
        log.info("  SCHEDULER COMPLETADO");
        log.info("════════════════════════════════════════════════════════════════");
    }

    /**
     * TAREA 1: Enviar campañas que están LISTAS y cuya fecha_inicio ya llegó
     */
    private void enviarCampanasListas(LocalDateTime ahora) {
        log.info("┌─────────────────────────────────────────────────────────────┐");
        log.info("│  TAREA 1: Buscando campañas LISTAS para enviar...          │");
        log.info("└─────────────────────────────────────────────────────────────┘");
        
        // Estado LISTO = 2
        List<CampanaMailing> listosParaEnviar = campanaRepo.findListosParaEnviar(2, ahora);

        if (listosParaEnviar.isEmpty()) {
            log.info("    No hay campañas listas para enviar");
            return;
        }

        log.info("    Encontradas {} campañas para enviar", listosParaEnviar.size());

        for (CampanaMailing campana : listosParaEnviar) {
            enviarCampanaIndividual(campana);
        }
    }

    /**
     * Envía una campaña individual usando Resend
     */
    private void enviarCampanaIndividual(CampanaMailing campana) {
        try {
            log.info("    ┌─ Procesando campaña ID: {} - '{}'", campana.getId(), campana.getNombre());
            log.info("    │  Fecha inicio: {}", campana.getFechaInicio().format(DTF));
            log.info("    │  Segmento ID: {}", campana.getIdSegmento());

            // 1. Obtener emails del segmento
            List<String> emails = segmentoPort.obtenerEmailsSegmento(campana.getIdSegmento());

            if (emails.isEmpty()) {
                log.warn("    │  ⚠ Segmento {} sin emails, saltando campaña", campana.getIdSegmento());
                return;
            }

            log.info("    │  Destinatarios: {}", emails.size());

            // 2. Enviar emails via Resend
            log.info("    │  Enviando via RESEND...");
            mailAdapter.enviarEmails(campana, emails);

            // 3. Actualizar estado a ENVIADO (3)
            campana.setIdEstado(3);
            campanaRepo.save(campana);

            // 4. Actualizar métricas con cantidad enviada
            MetricaCampana metricas = metricasRepo.findByCampanaMailingId(campana.getId())
                    .orElseThrow(() -> new NotFoundException("Métricas", campana.getId().longValue()));

            metricas.setEnviados(emails.size());
            metricas.setEntregados(emails.size()); // Asumimos entrega inicial
            metricas.setActualizadoEn(LocalDateTime.now());
            metricasRepo.save(metricas);

            log.info("    └─ ✓ Campaña {} enviada exitosamente a {} destinatarios", 
                campana.getId(), emails.size());

        } catch (Exception e) {
            log.error("    └─ ✗ Error enviando campaña {}: {}", campana.getId(), e.getMessage(), e);
            // No cambiar estado - se reintentará en próxima ejecución
        }
    }

    /**
     * TAREA 2: Marcar como VENCIDO las campañas PENDIENTE cuya fecha_inicio ya pasó
     */
    private void marcarComoVencidas(LocalDateTime ahora) {
        log.info("┌─────────────────────────────────────────────────────────────┐");
        log.info("│  TAREA 2: Buscando campañas vencidas...                     │");
        log.info("└─────────────────────────────────────────────────────────────┘");
        
        List<CampanaMailing> vencidas = campanaRepo.findVencidas(ahora);

        if (vencidas.isEmpty()) {
            log.info("    No hay campañas vencidas");
            return;
        }

        log.info("    Encontradas {} campañas vencidas", vencidas.size());

        for (CampanaMailing campana : vencidas) {
            try {
                log.warn("    ┌─ Campaña {} vencida (pasó fecha_inicio sin estar LISTO)", campana.getId());
                log.warn("    │  Nombre: {}", campana.getNombre());
                log.warn("    │  Fecha inicio: {}", campana.getFechaInicio().format(DTF));

                // Cambiar a VENCIDO (4)
                campana.setIdEstado(4);
                campanaRepo.save(campana);

                // Notificar al Gestor de Campañas
                try {
                    gestorPort.pausarCampana(
                        campana.getIdCampanaGestion(),
                        "Campaña vencida: pasó fecha_inicio sin estar en estado LISTO"
                    );
                    log.info("    │  Gestor notificado");
                } catch (Exception e) {
                    log.warn("    │  No se pudo notificar al Gestor: {}", e.getMessage());
                }

                log.info("    └─ ✓ Campaña {} marcada como VENCIDO", campana.getId());

            } catch (Exception e) {
                log.error("    └─ ✗ Error procesando campaña vencida {}: {}", campana.getId(), e.getMessage());
            }
        }
    }

    /**
     * TAREA 3: Marcar como FINALIZADO las campañas ENVIADO cuya fecha_fin ya pasó
     */
    private void marcarComoFinalizadas(LocalDateTime ahora) {
        log.info("┌─────────────────────────────────────────────────────────────┐");
        log.info("│  TAREA 3: Buscando campañas para finalizar...               │");
        log.info("└─────────────────────────────────────────────────────────────┘");
        
        // Buscar campañas en estado ENVIADO (3) cuya fecha_fin ya pasó
        List<CampanaMailing> paraFinalizar = campanaRepo.findByIdEstadoAndFechaFinBefore(3, ahora);

        if (paraFinalizar == null || paraFinalizar.isEmpty()) {
            log.info("    No hay campañas para finalizar");
            return;
        }

        log.info("    Encontradas {} campañas para finalizar", paraFinalizar.size());

        for (CampanaMailing campana : paraFinalizar) {
            try {
                log.info("    ┌─ Finalizando campaña ID: {} - '{}'", campana.getId(), campana.getNombre());
                log.info("    │  Fecha fin: {}", campana.getFechaFin().format(DTF));

                // Cambiar a FINALIZADO (5)
                campana.setIdEstado(5);
                campanaRepo.save(campana);

                log.info("    └─ ✓ Campaña {} marcada como FINALIZADO", campana.getId());

            } catch (Exception e) {
                log.error("    └─ ✗ Error finalizando campaña {}: {}", campana.getId(), e.getMessage());
            }
        }
    }
}