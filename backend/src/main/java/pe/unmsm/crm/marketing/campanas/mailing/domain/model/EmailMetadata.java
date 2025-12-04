package pe.unmsm.crm.marketing.campanas.mailing.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Tabla auxiliar para mapear email_id de Resend con metadata de campaña.
 * CRÍTICO: Resend NO envía custom metadata en webhooks, 
 * por eso necesitamos este mapeo interno.
 * 
 * RELACIÓN: 1 email enviado (email_metadata) → N interacciones (interacciones_log)
 */
@Entity
@Table(name = "email_metadata", indexes = {
    @Index(name = "idx_resend_email_id", columnList = "resend_email_id", unique = true),
    @Index(name = "idx_campana_email", columnList = "id_campana_mailing, email_destinatario"),
    @Index(name = "idx_campana_mailing", columnList = "id_campana_mailing"),
    @Index(name = "idx_fecha_envio", columnList = "fecha_envio")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID del email en Resend (retornado al enviar)
     */
    @Column(name = "resend_email_id", nullable = false, unique = true, length = 100)
    private String resendEmailId;

    /**
     * ID de la campaña de mailing
     */
    @Column(name = "id_campana_mailing", nullable = false)
    private Integer idCampanaMailing;

    /**
     * Email del destinatario
     */
    @Column(name = "email_destinatario", nullable = false, length = 255)
    private String emailDestinatario;

    /**
     * ID del lead en el sistema
     */
    @Column(name = "id_lead")
    private Long idLead;

    /**
     * Fecha de envío del email
     */
    @Column(name = "fecha_envio", nullable = false)
    private LocalDateTime fechaEnvio;

    @PrePersist
    protected void onCreate() {
        if (fechaEnvio == null) {
            fechaEnvio = LocalDateTime.now();
        }
    }
}