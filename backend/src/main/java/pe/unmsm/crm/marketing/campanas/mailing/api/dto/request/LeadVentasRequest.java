package pe.unmsm.crm.marketing.campanas.mailing.api.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO para enviar un lead interesado al módulo de Ventas.
 * 
 * Este DTO se usa cuando un destinatario hace clic en el CTA del correo,
 * indicando interés en la oferta de la campaña.
 * 
 * ENDPOINT DE VENTAS: POST /api/venta/lead/desde-marketing
 * 
 * CAMPOS REQUERIDOS POR VENTAS:
 * - idLeadMarketing: ID del lead en el sistema de Marketing
 * - nombres: Nombres del lead
 * - apellidos: Apellidos del lead
 * - correo: Email del lead
 * - telefono: Teléfono del lead
 * - canalOrigen: Canal de origen (CAMPANIA_MAILING o CAMPANIA_TELEFONICA)
 * - idCampaniaMarketing: ID de la campaña del Gestor
 * - nombreCampania: Nombre de la campaña
 * - tematica: Temática de la campaña
 * - descripcion: Descripción de la campaña
 * - notasLlamada: Notas/contexto para el vendedor
 * - fechaEnvio: Fecha/hora del envío a Ventas
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadVentasRequest {

    /**
     * ID del lead en el sistema de Marketing (tabla leads.lead_id)
     */
    @JsonProperty("idLeadMarketing")
    private Long idLeadMarketing;

    /**
     * Nombres del lead
     */
    @JsonProperty("nombres")
    private String nombres;

    /**
     * Apellidos del lead
     */
    @JsonProperty("apellidos")
    private String apellidos;

    /**
     * Correo electrónico del lead
     */
    @JsonProperty("correo")
    private String correo;

    /**
     * Teléfono del lead (puede ser null si no está disponible)
     */
    @JsonProperty("telefono")
    private String telefono;

    /**
     * Canal de origen de la derivación.
     * Para mailing siempre es "CAMPANIA_MAILING"
     */
    @JsonProperty("canalOrigen")
    @Builder.Default
    private String canalOrigen = "CAMPANIA_MAILING";

    /**
     * ID de la campaña en el Gestor de Campañas (id_campana_gestion)
     * Este es el ID que Ventas usará para tracking
     */
    @JsonProperty("idCampaniaMarketing")
    private Long idCampaniaMarketing;

    /**
     * Nombre de la campaña
     */
    @JsonProperty("nombreCampania")
    private String nombreCampania;

    /**
     * Temática de la campaña (ej: "Renovación de equipos", "Plan 5G")
     */
    @JsonProperty("tematica")
    private String tematica;

    /**
     * Descripción de la campaña
     */
    @JsonProperty("descripcion")
    private String descripcion;

    /**
     * Notas para el vendedor.
     * En mailing, indicamos el contexto del clic.
     */
    @JsonProperty("notasLlamada")
    private String notasLlamada;

    /**
     * Fecha y hora del envío a Ventas
     */
    @JsonProperty("fechaEnvio")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaEnvio;

    // ========================================================================
    // MÉTODOS DE UTILIDAD
    // ========================================================================

    /**
     * Genera notas automáticas para el vendedor basadas en el contexto
     */
    public static String generarNotasAutomaticas(String nombreCampania, String email) {
        return String.format(
            "Lead interesado desde campaña de mailing '%s'. " +
            "El cliente hizo clic en el CTA del correo enviado a %s. " +
            "Se recomienda contacto prioritario.",
            nombreCampania,
            email
        );
    }

    /**
     * Valida que los campos obligatorios estén presentes
     */
    public boolean esValido() {
        return idLeadMarketing != null 
            && correo != null && !correo.isBlank()
            && idCampaniaMarketing != null
            && nombreCampania != null && !nombreCampania.isBlank();
    }
}