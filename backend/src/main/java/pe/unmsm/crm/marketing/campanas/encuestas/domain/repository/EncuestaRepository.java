package pe.unmsm.crm.marketing.campanas.encuestas.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.unmsm.crm.marketing.campanas.encuestas.domain.model.Encuesta;

import java.util.List;

@Repository
public interface EncuestaRepository extends JpaRepository<Encuesta, Integer> {

    @Query("SELECT e, COUNT(r) FROM Encuesta e LEFT JOIN RespuestaEncuesta r ON e.idEncuesta = r.encuesta.idEncuesta GROUP BY e.idEncuesta")
    List<Object[]> findAllWithResponseCount();

    /**
     * Encuentra todas las encuestas en estado ACTIVA.
     * Retorna solo el id_encuesta y titulo para optimizar la consulta.
     * 
     * @return Lista de arrays de objetos donde [0] = idEncuesta, [1] = titulo
     */
    @Query("SELECT e.idEncuesta, e.titulo FROM Encuesta e WHERE e.estado = 'ACTIVA'")
    List<Object[]> findActiveSurveys();
}
