package pe.unmsm.crm.marketing.shared.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.unmsm.crm.marketing.shared.domain.model.Distrito;
import java.util.List;

public interface DistritoRepository extends JpaRepository<Distrito, String> {
        List<Distrito> findByProvinciaId(String provinciaId);

        // Query personalizada con TRIM para manejar espacios en nombres de BD
        @Query("SELECT d FROM Distrito d WHERE UPPER(TRIM(d.nombre)) = UPPER(TRIM(:nombre)) AND d.provinciaId = :provinciaId")
        java.util.Optional<Distrito> findByNombreIgnoreCaseAndProvinciaId(@Param("nombre") String nombre,
                        @Param("provinciaId") String provinciaId);

        // Optimización: Traer todos los nombres en una sola consulta
        @Query("SELECT new map(d.nombre as distrito, p.nombre as provincia, dep.nombre as departamento) " +
                        "FROM Distrito d, Provincia p, Departamento dep " +
                        "WHERE d.id = :distritoId " +
                        "AND d.provinciaId = p.id " +
                        "AND p.departamentoId = dep.id")
        java.util.Map<String, String> findNombresCompletos(@Param("distritoId") String distritoId);

        // NUEVO: Buscar ID por nombres de Distrito, Provincia y Departamento en UNA
        // sola consulta
        @Query("SELECT d.id FROM Distrito d " +
                        "JOIN Provincia p ON d.provinciaId = p.id " +
                        "JOIN Departamento dep ON p.departamentoId = dep.id " +
                        "WHERE UPPER(TRIM(d.nombre)) = UPPER(TRIM(:distrito)) " +
                        "AND UPPER(TRIM(p.nombre)) = UPPER(TRIM(:provincia)) " +
                        "AND UPPER(TRIM(dep.nombre)) = UPPER(TRIM(:departamento))")
        java.util.Optional<String> findIdByNombres(@Param("distrito") String distrito,
                        @Param("provincia") String provincia,
                        @Param("departamento") String departamento);
}
