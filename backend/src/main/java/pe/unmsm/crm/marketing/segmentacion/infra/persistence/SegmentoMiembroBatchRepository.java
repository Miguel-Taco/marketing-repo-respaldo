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

        jdbcTemplate.batchUpdate(sql, leadIds, leadIds.size(), (PreparedStatement ps, Long leadId) -> {
            ps.setLong(1, idSegmento);
            ps.setString(2, tipoMiembro);
            ps.setLong(3, leadId);
            ps.setObject(4, fechaAgregado);
        });

        long duration = System.currentTimeMillis() - startTime;
        log.info("✓ BATCH INSERT completado: {} miembros insertados en {}ms", leadIds.size(), duration);
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
