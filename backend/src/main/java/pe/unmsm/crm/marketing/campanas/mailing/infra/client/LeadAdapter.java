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
 * ESTRUCTURA REAL DE LA TABLA leads:
 * - lead_id: BIGINT (PK)
 * - nombre_completo: VARCHAR(255)  ← UN SOLO CAMPO, no hay nombres/apellidos separados
 * - email: VARCHAR(255) UNIQUE
 * - telefono: VARCHAR(255)
 * - fecha_creacion: DATETIME
 * - estado_lead_id: INT
 * - utm_source, utm_medium, utm_campaign, etc.
 * - distrito_id, edad, genero, estado_civil, nivel_educativo
 * - fuente_tipo, envio_formulario_id, registro_importado_id
 * - created_at, updated_at
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
     * 
     * NOTA: La tabla leads tiene nombre_completo como un solo campo.
     * Los métodos getNombresParaVentas() y getApellidosParaVentas() del DTO
     * se encargan de extraer nombres/apellidos del nombre completo.
     */
    @Override
    public Optional<LeadInfoDTO> findLeadInfoByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }

        try {
            String sql = """
                SELECT 
                    lead_id,
                    nombre_completo,
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
                            .email(rs.getString("email"))
                            .telefono(rs.getString("telefono"))
                            .build());
                }
                return Optional.<LeadInfoDTO>empty();
            }, email.trim().toLowerCase());

        } catch (Exception e) {
            log.error("Error obteniendo info de lead por email '{}': {}", email, e.getMessage());
            return Optional.empty();
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
                    nombre_completo,
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
                            .email(rs.getString("email"))
                            .telefono(rs.getString("telefono"))
                            .build());
                }
                return Optional.<LeadInfoDTO>empty();
            }, leadId);

        } catch (Exception e) {
            log.error("Error obteniendo info de lead por ID '{}': {}", leadId, e.getMessage());
            return Optional.empty();
        }
    }
}
