package pe.unmsm.crm.marketing.leads.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.unmsm.crm.marketing.leads.domain.model.staging.LoteImportacion;

public interface LoteRepository extends JpaRepository<LoteImportacion, Long> {
    // Para el historial con paginación: traer los últimos primero
    Page<LoteImportacion> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
