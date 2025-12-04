package pe.unmsm.crm.marketing.campanas.mailing.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entidad para registrar interacciones de usuarios con los emails.
 * 
 * TABLA: interacciones_log
 * 
 * ESTRUCTURA BD:
 * - id: INT AUTO_INCREMENT (PK)
 * - id_campana_mailing: INT NOT NULL
 * - id_tipo_evento: INT NOT NULL
 * - id_contacto_crm: INT NOT NULL  <-- IMPORTANTE: Es INT en BD, pero usamos Long por flexibilidad
 * - fecha_evento: TIMESTAMP
 * 
 * NOTA: Aunque id_contacto_crm es INT en la BD, usamos Long en Java para 
 * compatibilidad con lead_id (que es BIGINT). JPA maneja la conversión automáticamente.
 */
@Entity
@Table(name = "interacciones_log", indexes = {
    @Index(name = "idx_logs_campana_id", columnList = "id_campana_mailing"),
    @Index(name = "idx_logs_contacto_evento", columnList = "id_contacto_crm, id_tipo_evento")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InteraccionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_campana_mailing", nullable = false)
    private Integer idCampanaMailingId;

    @Column(name = "id_tipo_evento", nullable = false)
    private Integer idTipoEvento;

    /**
     * ID del contacto/lead en el CRM.
     * 
     * Referencia a leads.lead_id (BIGINT en BD).
     * La tabla interacciones_log tiene esta columna como INT,
     * pero por compatibilidad con el sistema de leads usamos Long.
     */
    @Column(name = "id_contacto_crm", nullable = false)
    private Long idContactoCrm;

    @Column(name = "fecha_evento", nullable = false, updatable = false)
    private LocalDateTime fechaEvento;

    @PrePersist
    protected void onCreate() {
        if (fechaEvento == null) {
            fechaEvento = LocalDateTime.now();
        }
    }
}