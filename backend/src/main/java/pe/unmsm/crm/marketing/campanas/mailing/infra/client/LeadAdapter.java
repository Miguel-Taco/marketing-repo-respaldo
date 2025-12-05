package pe.unmsm.crm.marketing.campanas.mailing.infra.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import pe.unmsm.crm.marketing.campanas.mailing.api.dto.response.LeadInfoDTO;
import pe.unmsm.crm.marketing.campanas.mailing.domain.port.output.ILeadPort;

import java.util.Optional;

/**
 * Adapter para obtener información de leads desde la BD.
 * 
 * ESTRUCTURA DE LA TABLA leads (según tu BD):
 * - lead_id: BIGINT (PK)
 * - nombre_completo: VARCHAR(255)
 * - email: VARCHAR(255) UNIQUE
 * - telefono: VARCHAR(20)
 * - (posiblemente otros campos como nombres, apellidos separados)
 * 
 * NOTA: Ajusta los nombres de las columnas según tu estructura real.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LeadAdapter implements ILeadPort {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Busca el ID de un lead por su email.
     */
    @Override
    public Long findLeadIdByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            log.warn("Email vacío o nulo recibido");
            return null;
        }
        
        try {
            String sql = "SELECT lead_id FROM leads WHERE email = ? LIMIT 1";
            Long leadId = jdbcTemplate.queryForObject(sql, Long.class, email.trim().toLowerCase());
            
            log.debug("Lead encontrado: email={}, lead_id={}", email, leadId);
            return leadId;
            
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            log.debug("No se encontró lead con email: {}", email);
            return null;
        } catch (Exception e) {
            log.error("Error buscando lead por email '{}': {}", email, e.getMessage());
            return null;
        }
    }

    /**
     * Obtiene información completa del lead por su email.
     */
    @Override
    public Optional<LeadInfoDTO> findLeadInfoByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }

        try {
            // NOTA: Ajusta los nombres de columnas según tu tabla real
            // Opciones comunes:
            // - nombre_completo (un solo campo)
            // - nombres + apellidos (campos separados)
            // - primer_nombre + segundo_nombre + apellido_paterno + apellido_materno
            
            String sql = """
                SELECT 
                    lead_id,
                    COALESCE(nombre_completo, CONCAT(COALESCE(nombres, ''), ' ', COALESCE(apellidos, ''))) as nombre_completo,
                    nombres,
                    apellidos,
                    email,
                    telefono
                FROM leads 
                WHERE email = ? 
                LIMIT 1
                """;
            
            return jdbcTemplate.query(sql, rs -> {
                if (rs.next()) {
                    return Optional.of(LeadInfoDTO.builder()
                            .leadId(rs.getLong("lead_id"))
                            .nombreCompleto(rs.getString("nombre_completo"))
                            .nombres(rs.getString("nombres"))
                            .apellidos(rs.getString("apellidos"))
                            .email(rs.getString("email"))
                            .telefono(rs.getString("telefono"))
                            .build());
                }
                return Optional.<LeadInfoDTO>empty();
            }, email.trim().toLowerCase());

        } catch (Exception e) {
            log.error("Error obteniendo info de lead por email '{}': {}", email, e.getMessage());
            // Intentar con query simplificada si la anterior falla
            return findLeadInfoByEmailSimple(email);
        }
    }

    /**
     * Obtiene información completa del lead por su ID.
     */
    @Override
    public Optional<LeadInfoDTO> findLeadInfoById(Long leadId) {
        if (leadId == null) {
            return Optional.empty();
        }

        try {
            String sql = """
                SELECT 
                    lead_id,
                    COALESCE(nombre_completo, CONCAT(COALESCE(nombres, ''), ' ', COALESCE(apellidos, ''))) as nombre_completo,
                    nombres,
                    apellidos,
                    email,
                    telefono
                FROM leads 
                WHERE lead_id = ? 
                LIMIT 1
                """;
            
            return jdbcTemplate.query(sql, rs -> {
                if (rs.next()) {
                    return Optional.of(LeadInfoDTO.builder()
                            .leadId(rs.getLong("lead_id"))
                            .nombreCompleto(rs.getString("nombre_completo"))
                            .nombres(rs.getString("nombres"))
                            .apellidos(rs.getString("apellidos"))
                            .email(rs.getString("email"))
                            .telefono(rs.getString("telefono"))
                            .build());
                }
                return Optional.<LeadInfoDTO>empty();
            }, leadId);

        } catch (Exception e) {
            log.error("Error obteniendo info de lead por ID '{}': {}", leadId, e.getMessage());
            return findLeadInfoByIdSimple(leadId);
        }
    }

    // ========================================================================
    // MÉTODOS DE FALLBACK (si la estructura de la tabla es diferente)
    // ========================================================================

    /**
     * Query simplificada si la tabla no tiene todos los campos esperados
     */
    private Optional<LeadInfoDTO> findLeadInfoByEmailSimple(String email) {
        try {
            log.debug("Usando query simplificada para email: {}", email);
            
            String sql = "SELECT lead_id, nombre_completo, email, telefono FROM leads WHERE email = ? LIMIT 1";
            
            return jdbcTemplate.query(sql, rs -> {
                if (rs.next()) {
                    return Optional.of(LeadInfoDTO.builder()
                            .leadId(rs.getLong("lead_id"))
                            .nombreCompleto(rs.getString("nombre_completo"))
                            .email(rs.getString("email"))
                            .telefono(rs.getString("telefono"))
                            .build());
                }
                return Optional.<LeadInfoDTO>empty();
            }, email.trim().toLowerCase());

        } catch (Exception e) {
            log.error("Error en query simplificada: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<LeadInfoDTO> findLeadInfoByIdSimple(Long leadId) {
        try {
            log.debug("Usando query simplificada para lead_id: {}", leadId);
            
            String sql = "SELECT lead_id, nombre_completo, email, telefono FROM leads WHERE lead_id = ? LIMIT 1";
            
            return jdbcTemplate.query(sql, rs -> {
                if (rs.next()) {
                    return Optional.of(LeadInfoDTO.builder()
                            .leadId(rs.getLong("lead_id"))
                            .nombreCompleto(rs.getString("nombre_completo"))
                            .email(rs.getString("email"))
                            .telefono(rs.getString("telefono"))
                            .build());
                }
                return Optional.<LeadInfoDTO>empty();
            }, leadId);

        } catch (Exception e) {
            log.error("Error en query simplificada por ID: {}", e.getMessage());
            return Optional.empty();
        }
    }
}