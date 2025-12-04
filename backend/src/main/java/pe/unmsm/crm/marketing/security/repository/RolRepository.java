package pe.unmsm.crm.marketing.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.unmsm.crm.marketing.security.domain.RolEntity;

import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<RolEntity, Long> {

    /**
     * Busca un rol por su nombre
     * 
     * @param nombre el nombre del rol (ej: "ADMIN", "AGENTE")
     * @return Optional conteniendo el rol si existe
     */
    Optional<RolEntity> findByNombre(String nombre);
}
