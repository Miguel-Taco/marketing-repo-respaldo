package pe.unmsm.crm.marketing.shared.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.unmsm.crm.marketing.shared.domain.model.Provincia;
import java.util.List;

public interface ProvinciaRepository extends JpaRepository<Provincia, String> {
    List<Provincia> findByDepartamento_Id(String departamentoId);

    // Query personalizada con TRIM para manejar espacios en nombres de BD
    @Query("SELECT p FROM Provincia p WHERE UPPER(TRIM(p.nombre)) = UPPER(TRIM(:nombre)) AND p.departamento.id = :departamentoId")
    java.util.Optional<Provincia> findByNombreIgnoreCaseAndDepartamentoId(@Param("nombre") String nombre,
            @Param("departamentoId") String departamentoId);
}
