package pe.unmsm.crm.marketing.campanas.mailing.infra.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.unmsm.crm.marketing.campanas.mailing.domain.model.EmailMetadata;
import java.util.Optional;

/**
 * Repositorio para almacenar metadata de emails enviados.
 * Permite mapear email_id de Resend con campaign_id interno.
 */
@Repository
public interface JpaEmailMetadataRepository extends JpaRepository<EmailMetadata, Long> {
    
    /**
     * Busca metadata por email_id de Resend.
     * CRÍTICO: Este método se usa en webhooks para obtener la campaña.
     * 
     * @param resendEmailId ID del email en Resend
     * @return Metadata si existe
     */
    Optional<EmailMetadata> findByResendEmailId(String resendEmailId);
    
    /**
     * Busca metadata por campaña y email destinatario.
     * Útil para tracking propio y deduplicación.
     */
    Optional<EmailMetadata> findByIdCampanaMailingAndEmailDestinatario(
        Integer idCampanaMailing, 
        String emailDestinatario
    );
}