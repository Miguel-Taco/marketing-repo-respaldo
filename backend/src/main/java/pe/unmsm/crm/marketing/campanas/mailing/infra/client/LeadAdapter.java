package pe.unmsm.crm.marketing.campanas.mailing.infra.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import pe.unmsm.crm.marketing.campanas.mailing.domain.port.output.ILeadPort;

/**
 * Adapter para obtener información de leads desde la BD.
 * 
 * ESTRUCTURA DE LA TABLA leads:
 * - lead_id: BIGINT (PK)
 * - email: VARCHAR(255) UNIQUE
 * - nombre_completo, telefono, etc.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LeadAdapter implements ILeadPort {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Busca el ID de un lead por su email.
     * 
     * @param email Email del lead
     * @return lead_id si existe, null si no
     */
    @Override
    public Long findLeadIdByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            log.warn("Email vacío o nulo recibido");
            return null;
        }
        
        try {
            // Query correcta según la estructura de la tabla leads
            String sql = "SELECT lead_id FROM leads WHERE email = ? LIMIT 1";
            
            Long leadId = jdbcTemplate.queryForObject(sql, Long.class, email.trim().toLowerCase());
            
            log.debug("Lead encontrado: email={}, lead_id={}", email, leadId);
            return leadId;
            
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            // No se encontró el lead - esto es normal, no es un error
            log.debug("No se encontró lead con email: {}", email);
            return null;
        } catch (Exception e) {
            log.error("Error buscando lead por email '{}': {}", email, e.getMessage());
            return null;
        }
    }
}