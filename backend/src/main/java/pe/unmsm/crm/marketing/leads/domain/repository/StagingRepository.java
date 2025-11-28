package pe.unmsm.crm.marketing.leads.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.unmsm.crm.marketing.leads.domain.model.staging.EnvioFormulario;

public interface StagingRepository extends JpaRepository<EnvioFormulario, Long> {
    // save() and findById() methods are automatically available from JpaRepository
}