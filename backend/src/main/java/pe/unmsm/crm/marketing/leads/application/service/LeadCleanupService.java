package pe.unmsm.crm.marketing.leads.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.unmsm.crm.marketing.leads.domain.enums.EstadoLead;
import pe.unmsm.crm.marketing.leads.domain.model.Lead;
import pe.unmsm.crm.marketing.leads.domain.repository.LeadRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeadCleanupService {

    private final LeadRepository leadRepository;

    /**
     * Se ejecuta todos los días a las 3:00 AM
     * Cron: "0 0 3 * * ?" = segundo 0, minuto 0, hora 3, todos los días
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void eliminarLeadsDescartadosAntiguos() {
        log.info("Iniciando limpieza automática de leads descartados...");

        // Configuración: Borrar si llevan más de 30 días descartados (sin actualizarse)
        Instant fechaCorte = Instant.now().minus(30, ChronoUnit.DAYS);

        List<Lead> leadsViejos = leadRepository.findByEstadoAndUpdatedAtBefore(EstadoLead.DESCARTADO, fechaCorte);

        if (!leadsViejos.isEmpty()) {
            leadRepository.deleteAll(leadsViejos);
            log.info("Eliminados {} leads descartados antiguos.", leadsViejos.size());
        } else {
            log.info("No se encontraron leads antiguos para eliminar.");
        }
    }
}
