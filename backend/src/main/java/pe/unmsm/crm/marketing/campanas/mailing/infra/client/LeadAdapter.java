package pe.unmsm.crm.marketing.campanas.mailing.infra.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import pe.unmsm.crm.marketing.campanas.mailing.domain.port.output.ILeadPort;

@Component
@RequiredArgsConstructor
@Slf4j
public class LeadAdapter implements ILeadPort {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Long findLeadIdByEmail(String email) {
        try {
            String sql = "SELECT lead_id FROM leads WHERE email = ? LIMIT 1";
            return jdbcTemplate.queryForObject(sql, Long.class, email);
        } catch (Exception e) {
            log.warn("No se encontró lead con email: {}", email);
            return null;
        }
    }
}