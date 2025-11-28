package pe.unmsm.crm.marketing.leads.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.unmsm.crm.marketing.leads.domain.model.staging.RegistroImportado;

public interface RegistroImportadoRepository extends JpaRepository<RegistroImportado, Long> {
}
