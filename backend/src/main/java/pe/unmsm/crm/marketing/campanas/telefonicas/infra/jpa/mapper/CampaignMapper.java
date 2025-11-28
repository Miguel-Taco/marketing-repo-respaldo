package pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.unmsm.crm.marketing.campanas.telefonicas.api.dto.*;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.entity.*;
import pe.unmsm.crm.marketing.leads.domain.model.Lead;
import pe.unmsm.crm.marketing.leads.domain.repository.LeadRepository;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CampaignMapper {

    private final LeadRepository leadRepository;

    /**
     * Convierte CampaniaTelefonicaEntity a DTO
     */
    public CampaniaTelefonicaDTO toDTO(CampaniaTelefonicaEntity entity) {
        if (entity == null)
            return null;

        return CampaniaTelefonicaDTO.builder()
                .id(entity.getId().longValue())
                .nombre(entity.getNombre())
                .fechaInicio(entity.getFechaInicio())
                .fechaFin(entity.getFechaFin())
                .estado(entity.getEstado())
                .prioridad(entity.getPrioridad() != null ? entity.getPrioridad().name() : null)
                .idGuion(entity.getIdGuion() != null ? entity.getIdGuion().longValue() : null)
                .idsAgentes(entity.getAgentes() != null ? entity.getAgentes().stream()
                        .map(a -> a.getIdAgente().longValue())
                        .collect(Collectors.toList()) : null)
                .build();
    }

    /**
     * Convierte ColaLlamadaEntity a ContactoDTO
     */
    public ContactoDTO toContactoDTO(ColaLlamadaEntity entity) {
        if (entity == null)
            return null;

        ContactoDTO.ContactoDTOBuilder builder = ContactoDTO.builder()
                .id(entity.getId().longValue())
                .idLead(entity.getIdLead())
                .estadoCampania(entity.getEstadoEnCola())
                .prioridad(entity.getPrioridadCola())
                .numeroIntentos(0); // TODO: calcular desde historial

        // Obtener datos del Lead
        if (entity.getIdLead() != null) {
            Optional<Lead> leadOpt = leadRepository.findById(entity.getIdLead());
            if (leadOpt.isPresent()) {
                Lead lead = leadOpt.get();
                builder.nombreCompleto(lead.getNombre());

                // Mapear datos de contacto
                if (lead.getContacto() != null) {
                    builder.telefono(lead.getContacto().getTelefono());
                    builder.email(lead.getContacto().getEmail());
                }

                // Empresa: Lead no tiene campo empresa, dejar null
            }
        }

        return builder.build();
    }

    /**
     * Convierte LlamadaEntity a LlamadaDTO
     */
    public LlamadaDTO toLlamadaDTO(LlamadaEntity entity) {
        if (entity == null)
            return null;

        Duration duracion = Duration.between(entity.getInicio(), entity.getFin());

        return LlamadaDTO.builder()
                .id(entity.getId().longValue())
                .idCampania(entity.getIdCampania().longValue())
                .idAgente(entity.getIdAgente().longValue())
                .fechaHora(entity.getInicio())
                .duracionSegundos((int) duracion.getSeconds())
                .resultado(entity.getResultado() != null ? entity.getResultado().getNombre() : null)
                .notas(entity.getNotas())
                .build();
    }

    /**
     * Convierte GuionEntity a GuionDTO
     */
    public GuionDTO toGuionDTO(GuionEntity entity) {
        if (entity == null)
            return null;

        return GuionDTO.builder()
                .id(entity.getId().longValue())
                .nombre(entity.getNombre())
                .descripcion(entity.getContenido())
                .build();
    }

    /**
     * Convierte métricas agregadas a MetricasAgenteDTO
     */
    public MetricasAgenteDTO toMetricasAgenteDTO(Map<String, Object> metricas) {
        if (metricas == null)
            return null;

        Long totalLlamadas = metricas.get("totalLlamadas") != null
                ? ((Number) metricas.get("totalLlamadas")).longValue()
                : 0L;

        Double duracionPromedio = metricas.get("duracionPromedio") != null
                ? ((Number) metricas.get("duracionPromedio")).doubleValue()
                : 0.0;

        return MetricasAgenteDTO.builder()
                .llamadasRealizadas(totalLlamadas.intValue())
                .duracionPromedio(duracionPromedio.intValue())
                .build();
    }

    /**
     * Convierte lista de entidades a lista de DTOs
     */
    public List<ContactoDTO> toContactoDTOList(List<ColaLlamadaEntity> entities) {
        if (entities == null)
            return null;

        return entities.stream()
                .map(this::toContactoDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convierte lista de llamadas a DTOs
     */
    public List<LlamadaDTO> toLlamadaDTOList(List<LlamadaEntity> entities) {
        if (entities == null)
            return null;

        return entities.stream()
                .map(this::toLlamadaDTO)
                .collect(Collectors.toList());
    }
}
