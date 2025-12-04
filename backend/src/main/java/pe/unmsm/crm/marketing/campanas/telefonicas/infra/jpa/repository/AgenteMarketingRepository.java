package pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.entity.AgenteMarketingEntity;

import java.util.Optional;

@Repository
public interface AgenteMarketingRepository extends JpaRepository<AgenteMarketingEntity, Integer> {

    Optional<AgenteMarketingEntity> findByIdUsuario(Long idUsuario);
}
