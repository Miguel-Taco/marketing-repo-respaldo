package pe.unmsm.crm.marketing.campanas.mailing.api.dto.response;

import lombok.*;

/**
 * DTO con los datos del lead necesarios para derivar a Ventas.
 * 
 * Se obtiene consultando la tabla `leads` por email o por lead_id.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadInfoDTO {

    /**
     * ID del lead (leads.lead_id)
     */
    private Long leadId;

    /**
     * Nombre completo del lead (puede venir en un solo campo)
     */
    private String nombreCompleto;

    /**
     * Nombres (si están separados)
     */
    private String nombres;

    /**
     * Apellidos (si están separados)
     */
    private String apellidos;

    /**
     * Email del lead
     */
    private String email;

    /**
     * Teléfono del lead
     */
    private String telefono;

    // ========================================================================
    // MÉTODOS DE UTILIDAD
    // ========================================================================

    /**
     * Obtiene los nombres, extrayéndolos del nombre completo si es necesario
     */
    public String getNombresParaVentas() {
        // Si ya tenemos nombres separados, usarlos
        if (nombres != null && !nombres.isBlank()) {
            return nombres;
        }
        
        // Si tenemos nombre completo, intentar extraer nombres
        if (nombreCompleto != null && !nombreCompleto.isBlank()) {
            String[] partes = nombreCompleto.trim().split("\\s+");
            if (partes.length >= 2) {
                // Asumimos que los primeros 1-2 son nombres
                return partes[0];
            }
            return nombreCompleto;
        }
        
        return "Sin nombre";
    }

    /**
     * Obtiene los apellidos, extrayéndolos del nombre completo si es necesario
     */
    public String getApellidosParaVentas() {
        // Si ya tenemos apellidos separados, usarlos
        if (apellidos != null && !apellidos.isBlank()) {
            return apellidos;
        }
        
        // Si tenemos nombre completo, intentar extraer apellidos
        if (nombreCompleto != null && !nombreCompleto.isBlank()) {
            String[] partes = nombreCompleto.trim().split("\\s+");
            if (partes.length >= 2) {
                // Asumimos que del segundo en adelante son apellidos
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < partes.length; i++) {
                    if (sb.length() > 0) sb.append(" ");
                    sb.append(partes[i]);
                }
                return sb.toString();
            }
        }
        
        return "";
    }

    /**
     * Obtiene el teléfono formateado para Ventas
     */
    public String getTelefonoParaVentas() {
        if (telefono == null || telefono.isBlank()) {
            return null; // Ventas acepta null
        }
        
        // Limpiar y formatear teléfono
        String telefonoLimpio = telefono.replaceAll("[^0-9+]", "");
        
        // Si no tiene código de país y parece peruano (9 dígitos empezando con 9)
        if (!telefonoLimpio.startsWith("+") && telefonoLimpio.length() == 9 && telefonoLimpio.startsWith("9")) {
            return "+51" + telefonoLimpio;
        }
        
        return telefonoLimpio;
    }
}