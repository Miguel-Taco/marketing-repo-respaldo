package pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.entity.EnvioEncuestaEntity;

import java.util.Optional;

@Repository
public interface EnvioEncuestaRepository extends JpaRepository<EnvioEncuestaEntity, Integer> {

    /**
     * Busca el envío de encuesta asociado a una llamada específica
     */
    Optional<EnvioEncuestaEntity> findByIdLlamada(Integer idLlamada);

    /**
     * Busca envíos de encuesta por lead y encuesta
     * Útil para verificar si ya se envió una encuesta específica a un lead
     */
    Optional<EnvioEncuestaEntity> findByIdLeadAndIdEncuesta(Long idLead, Integer idEncuesta);
}
