package pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.entity.CampaniaAgenteEntity;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.entity.CampaniaAgentePK;

import java.util.List;

@Repository
public interface CampaniaAgenteRepository extends JpaRepository<CampaniaAgenteEntity, CampaniaAgentePK> {

    List<CampaniaAgenteEntity> findByIdCampania(Integer idCampania);

    List<CampaniaAgenteEntity> findByIdAgente(Integer idAgente);

    boolean existsByIdCampaniaAndIdAgente(Integer idCampania, Integer idAgente);

    @Query("SELECT DISTINCT ca.idCampania FROM CampaniaAgenteEntity ca WHERE ca.idAgente = :idAgente")
    List<Integer> findCampaniaIdsByAgente(Integer idAgente);
}
