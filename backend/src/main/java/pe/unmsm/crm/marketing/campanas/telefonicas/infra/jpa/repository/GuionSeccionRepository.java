package pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.entity.GuionSeccionEntity;

@Repository
public interface GuionSeccionRepository extends JpaRepository<GuionSeccionEntity, Integer> {
}
