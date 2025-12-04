package pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.entity.CampaniaTelefonicaEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampaniaTelefonicaRepository extends JpaRepository<CampaniaTelefonicaEntity, Integer> {

        /**
         * Obtiene las campañas telefónicas visibles para un agente específico.
         */
        @Query("SELECT c FROM CampaniaTelefonicaEntity c " +
                        "JOIN c.agentes ca " +
                        "WHERE ca.idAgente = :idAgente " +
                        "AND c.esArchivado = false " +
                        "AND c.estado NOT IN ('Cancelada', 'Finalizada') " +
                        "ORDER BY c.fechaInicio DESC")
        List<CampaniaTelefonicaEntity> findVisibleByAgenteId(@Param("idAgente") Integer idAgente);

        /**
         * Obtiene todas las campañas activas no archivadas
         */
        @Query("SELECT c FROM CampaniaTelefonicaEntity c " +
                        "WHERE c.estado = 'ACTIVA' AND c.esArchivado = false")
        List<CampaniaTelefonicaEntity> findActiveCampaigns();

        /**
         * Busca campañas por estado y estado de archivado
         */
        List<CampaniaTelefonicaEntity> findByEstadoAndEsArchivado(String estado, Boolean esArchivado);

        /**
         * Busca campañas por segmento
         */
        List<CampaniaTelefonicaEntity> findByIdSegmento(Long idSegmento);

        /**
         * Busca una campaña telefónica por el ID de la campaña del gestor general
         * Utilizado para la integración con el módulo gestor de campañas
         */
        Optional<CampaniaTelefonicaEntity> findByIdCampanaGestion(Long idCampanaGestion);

        /**
         * Busca campañas telefónicas asociadas a una encuesta específica
         * Utilizado para la integración con el gestor de encuestas (contactos urgentes)
         */
        List<CampaniaTelefonicaEntity> findByIdEncuesta(Integer idEncuesta);

        boolean existsByIdAndAgentes_IdAgente(Integer idCampania, Integer idAgente);
}
