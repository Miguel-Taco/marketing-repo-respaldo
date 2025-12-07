package pe.unmsm.crm.marketing.segmentacion.infra.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

/**
 * Repositorio optimizado para inserción masiva de filtros de segmento
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class SegmentoFiltroBatchRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Inserta filtros de segmento en batch usando JDBC nativo
     */
    public void batchInsertFiltros(List<JpaSegmentoFiltroEntity> filtros) {
        if (filtros == null || filtros.isEmpty()) {
            log.warn("No hay filtros para insertar");
            return;
        }

        String sql = "INSERT INTO segmento_filtro (id_segmento, id_filtro, operador, valor_texto) VALUES (?, ?, ?, ?)";

        long startTime = System.currentTimeMillis();

        jdbcTemplate.batchUpdate(sql, filtros, filtros.size(),
                (PreparedStatement ps, JpaSegmentoFiltroEntity filtro) -> {
                    ps.setLong(1, filtro.getSegmento().getIdSegmento());
                    ps.setLong(2, filtro.getIdFiltro());
                    ps.setString(3, filtro.getOperador());
                    ps.setString(4, filtro.getValorTexto());
                });

        long duration = System.currentTimeMillis() - startTime;
        log.info("✓ BATCH INSERT filtros completado: {} filtros insertados en {}ms", filtros.size(), duration);
    }
}
