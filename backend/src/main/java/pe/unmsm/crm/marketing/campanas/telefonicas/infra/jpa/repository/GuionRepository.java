package pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.entity.GuionEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface GuionRepository extends JpaRepository<GuionEntity, Integer> {

    Optional<GuionEntity> findById(Integer id);

    List<GuionEntity> findByActivoTrue();
}
