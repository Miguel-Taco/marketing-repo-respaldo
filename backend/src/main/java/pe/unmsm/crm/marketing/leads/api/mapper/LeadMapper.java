package pe.unmsm.crm.marketing.leads.api.mapper;

import pe.unmsm.crm.marketing.leads.api.dto.*;
import pe.unmsm.crm.marketing.leads.domain.model.Lead;
import pe.unmsm.crm.marketing.shared.utils.DateTimeUtils; // Importar

import java.util.HashMap;
import java.util.Map;

/**
 * Mapper utility for converting between DTOs and domain entities.
 * 
 * CRITICAL: The key mappings in toStagingMap() MUST match exactly what
 * WebLeadFactory.convertirALead() expects. Incorrect keys will result in null
 * data.
 */
public class LeadMapper {

    /**
     * Converts LeadCaptureRequest to Map<String, String> for staging compatibility.
     * 
     * Key mappings align with WebLeadFactory expectations:
     * - nombre_completo, email, telefono, distrito_id
     * - edad, genero
     * - utm_source, utm_medium, utm_campaign, utm_term, utm_content
     */
    public static Map<String, String> toStagingMap(LeadCaptureRequest request) {
        Map<String, String> map = new HashMap<>();

        // Basic fields
        if (request.getNombreCompleto() != null) {
            map.put("nombre_completo", request.getNombreCompleto());
        }

        // Contact data
        if (request.getContacto() != null) {
            DatosContactoRequest contacto = request.getContacto();
            if (contacto.getEmail() != null) {
                map.put("email", contacto.getEmail());
            }
            if (contacto.getTelefono() != null) {
                map.put("telefono", contacto.getTelefono());
            }
            if (contacto.getDistritoId() != null) {
                map.put("distrito_id", contacto.getDistritoId());
            }
        }

        // Demographics
        if (request.getDemograficos() != null) {
            DatosDemograficosRequest demo = request.getDemograficos();
            if (demo.getEdad() != null) {
                map.put("edad", demo.getEdad().toString());
            }
            if (demo.getGenero() != null) {
                map.put("genero", demo.getGenero());
            }
        }

        // UTM Tracking
        if (request.getTracking() != null) {
            TrackingUTMRequest tracking = request.getTracking();
            if (tracking.getSource() != null) {
                map.put("utm_source", tracking.getSource());
            }
            if (tracking.getMedium() != null) {
                map.put("utm_medium", tracking.getMedium());
            }
            if (tracking.getCampaign() != null) {
                map.put("utm_campaign", tracking.getCampaign());
            }
            if (tracking.getTerm() != null) {
                map.put("utm_term", tracking.getTerm());
            }
            if (tracking.getContent() != null) {
                map.put("utm_content", tracking.getContent());
            }
        }

        return map;
    }

    /**
     * Converts Lead entity to LeadResponse DTO.
     */
    public static LeadResponse toResponse(Lead lead, Map<String, String> ubigeoNombres) {
        if (lead == null) {
            return null;
        }

        // Map contact data
        DatosContactoDTO contactoDTO = null;
        if (lead.getContacto() != null) {
            contactoDTO = DatosContactoDTO.builder()
                    .email(lead.getContacto().getEmail())
                    .telefono(lead.getContacto().getTelefono())
                    .build();
        }

        // Map demographics
        DatosDemograficosDTO demograficosDTO = null;
        if (lead.getDemograficos() != null) {
            var builder = DatosDemograficosDTO.builder()
                    .edad(lead.getDemograficos().getEdad())
                    .genero(lead.getDemograficos().getGenero())
                    .distrito(lead.getDemograficos().getDistrito());

            if (ubigeoNombres != null) {
                builder.distritoNombre(ubigeoNombres.get("distrito"))
                        .provinciaNombre(ubigeoNombres.get("provincia"))
                        .departamentoNombre(ubigeoNombres.get("departamento"));
            }

            demograficosDTO = builder.build();
        }

        // Map tracking
        TrackingUTMDTO trackingDTO = null;
        if (lead.getTracking() != null) {
            trackingDTO = TrackingUTMDTO.builder()
                    .source(lead.getTracking().getSource())
                    .medium(lead.getTracking().getMedium())
                    .campaign(lead.getTracking().getCampaign())
                    .term(lead.getTracking().getTerm())
                    .content(lead.getTracking().getContent())
                    .build();
        }

        return LeadResponse.builder()
                .id(lead.getId())
                .nombreCompleto(lead.getNombre())
                .estado(lead.getEstado() != null ? lead.getEstado().name() : null)
                .fechaCreacion(DateTimeUtils.format(lead.getFechaCreacion())) // USAR DateTimeUtils
                .contacto(contactoDTO)
                .demograficos(demograficosDTO)
                .tracking(trackingDTO)
                .fuenteTipo(lead.getFuenteTipo() != null ? lead.getFuenteTipo().name() : null)
                .build();
    }

    // Sobrecarga para compatibilidad (sin ubigeo)
    public static LeadResponse toResponse(Lead lead) {
        return toResponse(lead, null);
    }

    public static HistorialEstadoLeadDTO toHistorialDTO(
            pe.unmsm.crm.marketing.leads.domain.model.HistorialEstadoLead entity) {
        if (entity == null)
            return null;
        return HistorialEstadoLeadDTO.builder()
                .fecha(DateTimeUtils.format(entity.getFechaCambio()))
                .estado(entity.getEstadoNuevo().name())
                .motivo(entity.getMotivo())
                .build();
    }
}
