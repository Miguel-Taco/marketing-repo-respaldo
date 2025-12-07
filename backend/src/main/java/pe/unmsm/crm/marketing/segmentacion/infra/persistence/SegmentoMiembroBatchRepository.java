package pe.unmsm.crm.marketing.segmentacion.infra.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio optimizado para inserción masiva de miembros de segmento
 * Usa JDBC nativo para evitar problemas con claves compuestas en batch insert
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class SegmentoMiembroBatchRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Inserta miembros de segmento en batch usando JDBC nativo
     * Mucho más eficiente que JPA saveAll() con claves compuestas
     */
    public void batchInsertMiembros(Long idSegmento, String tipoMiembro, List<Long> leadIds,
            LocalDateTime fechaAgregado) {
        if (leadIds == null || leadIds.isEmpty()) {
            log.warn("No hay miembros para insertar");
            return;
        }

        String sql = "INSERT INTO segmento_miembro (id_segmento, tipo_miembro, id_miembro, fecha_agregado) VALUES (?, ?, ?, ?)";

        long startTime = System.currentTimeMillis();

        // Procesar en chunks de 500 para mejor rendimiento
        int batchSize = 500;
        int totalInserted = 0;

        for (int i = 0; i < leadIds.size(); i += batchSize) {
            int end = Math.min(i + batchSize, leadIds.size());
            List<Long> chunk = leadIds.subList(i, end);

            jdbcTemplate.batchUpdate(sql, chunk, chunk.size(), (PreparedStatement ps, Long leadId) -> {
                ps.setLong(1, idSegmento);
                ps.setString(2, tipoMiembro);
                ps.setLong(3, leadId);
                ps.setObject(4, fechaAgregado);
            });

            totalInserted += chunk.size();
            log.debug("Insertados {}/{} miembros", totalInserted, leadIds.size());
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("✓ BATCH INSERT completado: {} miembros insertados en {}ms ({} ms/miembro)",
                leadIds.size(), duration, duration / leadIds.size());
    }

    /**
     * Elimina todos los miembros de un segmento
     */
    public void deleteByIdSegmento(Long idSegmento) {
        String sql = "DELETE FROM segmento_miembro WHERE id_segmento = ?";
        int deleted = jdbcTemplate.update(sql, idSegmento);
        log.info("Eliminados {} miembros del segmento {}", deleted, idSegmento);
    }
}
