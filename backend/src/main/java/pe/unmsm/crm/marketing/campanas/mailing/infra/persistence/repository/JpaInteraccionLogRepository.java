package pe.unmsm.crm.marketing.campanas.mailing.infra.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.unmsm.crm.marketing.campanas.mailing.domain.model.InteraccionLog;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaInteraccionLogRepository extends JpaRepository<InteraccionLog, Integer> {

    @Query("SELECT i FROM InteraccionLog i WHERE i.idCampanaMailingId = :idCampana ORDER BY i.fechaEvento DESC")
    List<InteraccionLog> findByCampanaMailingId(@Param("idCampana") Integer idCampana);

    @Query("SELECT i FROM InteraccionLog i WHERE i.idCampanaMailingId = :idCampana AND i.idTipoEvento = :idTipo")
    List<InteraccionLog> findByCampanaAndTipo(@Param("idCampana") Integer idCampana, @Param("idTipo") Integer idTipo);
}
