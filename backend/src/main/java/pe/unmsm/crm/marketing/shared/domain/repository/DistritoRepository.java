package pe.unmsm.crm.marketing.shared.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.unmsm.crm.marketing.shared.domain.model.Distrito;
import java.util.List;

public interface DistritoRepository extends JpaRepository<Distrito, String> {
        List<Distrito> findByProvincia_Id(String provinciaId);

        // Query personalizada con TRIM para manejar espacios en nombres de BD
        @Query("SELECT d FROM Distrito d WHERE UPPER(TRIM(d.nombre)) = UPPER(TRIM(:nombre)) AND d.provincia.id = :provinciaId")
        java.util.Optional<Distrito> findByNombreIgnoreCaseAndProvinciaId(@Param("nombre") String nombre,
                        @Param("provinciaId") String provinciaId);

        // Optimización: Traer todos los nombres en una sola consulta
        @Query("SELECT new map(d.nombre as distrito, d.provincia.nombre as provincia, d.provincia.departamento.nombre as departamento) "
                        +
                        "FROM Distrito d " +
                        "WHERE d.id = :distritoId")
        java.util.Map<String, String> findNombresCompletos(@Param("distritoId") String distritoId);

        // NUEVO: Buscar ID por nombres de Distrito, Provincia y Departamento en UNA
        // sola consulta
        @Query("SELECT d.id FROM Distrito d " +
                        "WHERE UPPER(TRIM(d.nombre)) = UPPER(TRIM(:distrito)) " +
                        "AND UPPER(TRIM(d.provincia.nombre)) = UPPER(TRIM(:provincia)) " +
                        "AND UPPER(TRIM(d.provincia.departamento.nombre)) = UPPER(TRIM(:departamento))")
        java.util.Optional<String> findIdByNombres(@Param("distrito") String distrito,
                        @Param("provincia") String provincia,
                        @Param("departamento") String departamento);
}
