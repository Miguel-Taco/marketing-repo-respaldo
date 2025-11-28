package pe.unmsm.crm.marketing.leads.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.unmsm.crm.marketing.leads.domain.model.HistorialEstadoLead;

public interface HistorialRepository extends JpaRepository<HistorialEstadoLead, Long> {
    java.util.List<HistorialEstadoLead> findByLeadIdOrderByFechaCambioDesc(Long leadId);
}
