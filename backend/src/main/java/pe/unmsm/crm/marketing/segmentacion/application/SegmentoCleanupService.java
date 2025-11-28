package pe.unmsm.crm.marketing.segmentacion.application;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.unmsm.crm.marketing.segmentacion.domain.repository.SegmentoRepository;

import java.time.LocalDateTime;

/**
 * Servicio para tareas programadas de limpieza de segmentos
 */
@Service
public class SegmentoCleanupService {

    private final SegmentoRepository segmentoRepository;

    public SegmentoCleanupService(SegmentoRepository segmentoRepository) {
        this.segmentoRepository = segmentoRepository;
    }

    /**
     * Elimina permanentemente segmentos que han estado en estado ELIMINADO por más
     * de 10 días
     * Se ejecuta diariamente a las 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void eliminarSegmentosAntiguos() {
        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(10);

        var segmentosEliminados = segmentoRepository.findAll().stream()
                .filter(s -> "ELIMINADO".equals(s.getEstado()))
                .filter(s -> s.getFechaActualizacion().isBefore(fechaLimite))
                .toList();

        int count = 0;
        for (var segmento : segmentosEliminados) {
            segmentoRepository.deleteById(segmento.getId());
            count++;
        }

        if (count > 0) {
            System.out.println("✓ Eliminados permanentemente " + count + " segmentos antiguos");
        }
    }
}
