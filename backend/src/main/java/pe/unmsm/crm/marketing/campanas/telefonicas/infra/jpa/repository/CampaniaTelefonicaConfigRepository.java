package pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.entity.CampaniaTelefonicaConfigEntity;

/**
 * Repositorio para la configuración de campañas telefónicas.
 */
@Repository
public interface CampaniaTelefonicaConfigRepository extends JpaRepository<CampaniaTelefonicaConfigEntity, Integer> {
    // Métodos básicos heredados de JpaRepository son suficientes
}
