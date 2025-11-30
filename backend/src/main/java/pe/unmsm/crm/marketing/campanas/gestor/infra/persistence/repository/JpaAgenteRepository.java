package pe.unmsm.crm.marketing.campanas.gestor.infra.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.unmsm.crm.marketing.campanas.gestor.infra.persistence.entity.AgenteEntity;

import java.util.List;

@Repository
public interface JpaAgenteRepository extends JpaRepository<AgenteEntity, Integer> {

    /**
     * Busca todos los agentes activos
     */
    List<AgenteEntity> findByActivoTrue();
}
