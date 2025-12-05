package pe.unmsm.crm.marketing.campanas.mailing.domain.port.output;

import pe.unmsm.crm.marketing.campanas.mailing.api.dto.request.LeadVentasRequest;

/**
 * Puerto para integración con el módulo de Ventas.
 * 
 * Se usa para derivar leads interesados cuando hacen clic en el CTA del correo.
 */
public interface IVentasPort {

    /**
     * Deriva un lead interesado al módulo de Ventas.
     * 
     * @param request DTO con toda la información requerida por Ventas
     * @return true si se derivó correctamente, false si hubo error
     */
    boolean derivarLeadInteresado(LeadVentasRequest request);

    /**
     * @deprecated Usar derivarLeadInteresado(LeadVentasRequest) en su lugar
     * 
     * Método legacy mantenido por compatibilidad con código existente.
     */
    @Deprecated
    default void derivarInteresado(Integer idCampanaMailingId, Integer idAgenteAsignado, 
                                   Long idLead, Long idSegmento, Long idCampanaGestion) {
        // Implementación vacía - el código actual debería usar derivarLeadInteresado
    }
}