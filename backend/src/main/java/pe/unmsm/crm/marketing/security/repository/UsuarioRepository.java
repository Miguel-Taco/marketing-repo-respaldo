package pe.unmsm.crm.marketing.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.unmsm.crm.marketing.security.domain.UsuarioEntity;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {

    /**
     * Busca un usuario por su username
     * 
     * @param username el nombre de usuario
     * @return Optional conteniendo el usuario si existe
     */
    Optional<UsuarioEntity> findByUsername(String username);

    /**
     * Verifica si existe un usuario con el username especificado
     * 
     * @param username el nombre de usuario
     * @return true si existe, false si no
     */
    boolean existsByUsername(String username);
}
