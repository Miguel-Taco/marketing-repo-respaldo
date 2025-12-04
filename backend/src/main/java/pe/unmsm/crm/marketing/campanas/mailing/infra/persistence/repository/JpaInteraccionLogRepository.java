package pe.unmsm.crm.marketing.campanas.mailing.infra.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.unmsm.crm.marketing.campanas.mailing.domain.model.InteraccionLog;

import java.util.List;

/**
 * Repositorio para gestionar interacciones de emails.
 * 
 * MÉTODOS PRINCIPALES:
 * - findByCampanaMailingId: Obtener todas las interacciones de una campaña
 * - findByCampanaAndTipo: Filtrar por tipo de interacción
 * - existsByIdCampanaMailingIdAndIdContactoCrmAndIdTipoEvento: DEDUPLICACIÓN
 */
@Repository
public interface JpaInteraccionLogRepository extends JpaRepository<InteraccionLog, Integer> {

    /**
     * Obtiene todas las interacciones de una campaña ordenadas por fecha
     */
    @Query("SELECT i FROM InteraccionLog i WHERE i.idCampanaMailingId = :idCampana ORDER BY i.fechaEvento DESC")
    List<InteraccionLog> findByCampanaMailingId(@Param("idCampana") Integer idCampana);

    /**
     * Obtiene interacciones de una campaña filtradas por tipo
     */
    @Query("SELECT i FROM InteraccionLog i WHERE i.idCampanaMailingId = :idCampana AND i.idTipoEvento = :idTipo")
    List<InteraccionLog> findByCampanaAndTipo(@Param("idCampana") Integer idCampana, @Param("idTipo") Integer idTipo);

    /**
     * ✅ MÉTODO DE DEDUPLICACIÓN
     * 
     * Verifica si ya existe una interacción con la misma combinación de:
     * - ID de campaña
     * - ID de contacto (lead)
     * - Tipo de evento
     * 
     * USADO POR: WebhookResendService para evitar registrar eventos duplicados
     * 
     * IMPORTANTE: Esto reemplaza la deduplicación en memoria (Set estático)
     * que se perdía al reiniciar el servidor.
     * 
     * @param idCampana ID de la campaña de mailing
     * @param idContacto ID del lead/contacto
     * @param idTipo ID del tipo de interacción (1=apertura, 2=clic, 3=rebote, 4=baja)
     * @return true si ya existe, false si no
     */
    boolean existsByIdCampanaMailingIdAndIdContactoCrmAndIdTipoEvento(
        Integer idCampana, 
        Long idContacto, 
        Integer idTipo
    );

    /**
     * Cuenta interacciones por campaña y tipo
     * Útil para métricas rápidas
     */
    @Query("SELECT COUNT(i) FROM InteraccionLog i WHERE i.idCampanaMailingId = :idCampana AND i.idTipoEvento = :idTipo")
    Long countByCampanaAndTipo(@Param("idCampana") Integer idCampana, @Param("idTipo") Integer idTipo);

    /**
     * Obtiene la última interacción de una campaña
     * Útil para saber cuándo fue la última actividad
     */
    @Query("SELECT i FROM InteraccionLog i WHERE i.idCampanaMailingId = :idCampana ORDER BY i.fechaEvento DESC LIMIT 1")
    InteraccionLog findUltimaInteraccion(@Param("idCampana") Integer idCampana);
}