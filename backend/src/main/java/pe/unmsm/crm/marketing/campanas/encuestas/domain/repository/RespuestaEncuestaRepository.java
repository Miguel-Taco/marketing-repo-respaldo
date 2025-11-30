package pe.unmsm.crm.marketing.campanas.encuestas.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.unmsm.crm.marketing.campanas.encuestas.domain.model.RespuestaEncuesta;

@Repository
public interface RespuestaEncuestaRepository extends JpaRepository<RespuestaEncuesta, Integer> {

    /**
     * Verifica si un lead ya ha respondido una encuesta específica.
     * 
     * @param leadId     ID del lead
     * @param idEncuesta ID de la encuesta
     * @return true si el lead ya respondió, false en caso contrario
     */
    @Query("SELECT COUNT(r) > 0 FROM RespuestaEncuesta r WHERE r.lead.id = :leadId AND r.encuesta.idEncuesta = :idEncuesta")
    boolean existsByLeadAndEncuesta(@Param("leadId") Long leadId, @Param("idEncuesta") Integer idEncuesta);
}
