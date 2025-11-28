package pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.entity.CampaniaTelefonicaEntity;

import java.util.List;

@Repository
public interface CampaniaTelefonicaRepository extends JpaRepository<CampaniaTelefonicaEntity, Integer> {

        /**
         * Obtiene las campañas telefónicas activas (modificado para mostrar todas las
         * campañas)
         * Anteriormente filtraba por agente, pero se simplificó para mostrar todas las
         * campañas no archivadas
         */
        @Query("SELECT c FROM CampaniaTelefonicaEntity c " +
                        "WHERE c.esArchivado = false " +
                        "ORDER BY c.fechaInicio DESC")
        List<CampaniaTelefonicaEntity> findByAgenteId(@Param("idAgente") Integer idAgente);

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
}
