package pe.unmsm.crm.marketing.campanas.mailing.infra.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.unmsm.crm.marketing.campanas.mailing.domain.model.MetricaCampana;

import java.util.Optional;

@Repository
public interface JpaMetricaMailingRepository extends JpaRepository<MetricaCampana, Integer> {

    @Query("SELECT m FROM MetricaCampana m WHERE m.campanaMailing.id = :idCampana")
    Optional<MetricaCampana> findByCampanaMailingId(@Param("idCampana") Integer idCampana);
}
