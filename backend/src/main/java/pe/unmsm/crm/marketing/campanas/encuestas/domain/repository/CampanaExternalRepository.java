package pe.unmsm.crm.marketing.campanas.encuestas.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.Campana;

import java.util.List;

@Repository
public interface CampanaExternalRepository extends JpaRepository<Campana, Long> {

    /**
     * Busca campañas asociadas a una encuesta.
     * Definido aquí para evitar modificar el módulo 'gestor'.
     */
    List<Campana> findByIdEncuesta(Integer idEncuesta);
}
