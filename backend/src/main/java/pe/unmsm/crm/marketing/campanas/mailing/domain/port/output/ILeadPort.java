package pe.unmsm.crm.marketing.campanas.mailing.domain.port.output;

import pe.unmsm.crm.marketing.campanas.mailing.api.dto.response.LeadInfoDTO;

import java.util.Optional;

/**
 * Puerto para obtener información de leads.
 * 
 * Implementado por LeadAdapter que consulta la tabla `leads`.
 */
public interface ILeadPort {

    /**
     * Busca el ID de un lead por su email.
     * 
     * @param email Email del lead
     * @return lead_id si existe, null si no
     */
    Long findLeadIdByEmail(String email);

    /**
     * Obtiene información completa del lead por su email.
     * Incluye: nombres, apellidos, teléfono, etc.
     * 
     * @param email Email del lead
     * @return LeadInfoDTO con los datos, o empty si no existe
     */
    Optional<LeadInfoDTO> findLeadInfoByEmail(String email);

    /**
     * Obtiene información completa del lead por su ID.
     * 
     * @param leadId ID del lead
     * @return LeadInfoDTO con los datos, o empty si no existe
     */
    Optional<LeadInfoDTO> findLeadInfoById(Long leadId);
}