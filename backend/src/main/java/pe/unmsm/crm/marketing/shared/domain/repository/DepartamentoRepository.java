package pe.unmsm.crm.marketing.shared.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.unmsm.crm.marketing.shared.domain.model.Departamento;

public interface DepartamentoRepository extends JpaRepository<Departamento, String> {
    java.util.Optional<Departamento> findByNombreIgnoreCase(String nombre);
}
