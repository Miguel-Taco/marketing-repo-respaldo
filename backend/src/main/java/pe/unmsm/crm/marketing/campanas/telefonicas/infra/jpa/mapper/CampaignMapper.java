package pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.unmsm.crm.marketing.campanas.telefonicas.api.dto.*;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.entity.*;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.repository.ColaLlamadaRepository;
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
    private final ColaLlamadaRepository colaLlamadaRepository;

    /**
     * Convierte CampaniaTelefonicaEntity a DTO
     */
    public CampaniaTelefonicaDTO toDTO(CampaniaTelefonicaEntity entity) {
        if (entity == null)
            return null;

        // Calcular métricas de progreso
        Long totalLeads = colaLlamadaRepository.countTotalByCampaign(entity.getId());
        Long leadsContactados = colaLlamadaRepository.countCompletadosByCampaign(entity.getId());
        Long leadsPendientes = colaLlamadaRepository.countByEstadoAndCampaign(entity.getId(), "PENDIENTE");

        // Calcular porcentaje de avance
        Double porcentajeAvance = 0.0;
        if (totalLeads != null && totalLeads > 0 && leadsContactados != null) {
            porcentajeAvance = (leadsContactados.doubleValue() / totalLeads.doubleValue()) * 100.0;
        }

        return CampaniaTelefonicaDTO.builder()
                .id(entity.getId().longValue())
                .codigo("CAMP-" + entity.getId()) // Generar código basado en ID
                .nombre(entity.getNombre())
                .descripcion(null) // No disponible en la entidad
                .fechaInicio(entity.getFechaInicio())
                .fechaFin(entity.getFechaFin())
                .estado(entity.getEstado())
                .prioridad(entity.getPrioridad() != null ? entity.getPrioridad().name() : null)
                .idGuion(null) // REMOVED: id_guion column doesn't exist in database
                .totalLeads(totalLeads != null ? totalLeads.intValue() : 0)
                .leadsPendientes(leadsPendientes != null ? leadsPendientes.intValue() : 0)
                .leadsContactados(leadsContactados != null ? leadsContactados.intValue() : 0)
                .porcentajeAvance(porcentajeAvance)
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
                .fechaUltimaLlamada(entity.getFechaUltimaLlamada())
                .resultadoUltimaLlamada(entity.getResultadoUltimaLlamada())
                .numeroIntentos(0); // TODO: calcular desde historial

        // Obtener información del agente actual si está asignado
        if (entity.getIdAgenteActual() != null) {
            builder.idAgenteActual(entity.getIdAgenteActual().longValue());

            // Intentar obtener el nombre del agente desde la relación
            if (entity.getAgenteActual() != null) {
                builder.nombreAgenteActual(entity.getAgenteActual().getNombre());
            }
        }

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
        String nombreContacto = null;
        String telefonoContacto = null;

        if (entity.getIdLead() != null) {
            Optional<Lead> leadOpt = leadRepository.findById(entity.getIdLead());
            if (leadOpt.isPresent()) {
                Lead lead = leadOpt.get();
                nombreContacto = lead.getNombre();
                if (lead.getContacto() != null) {
                    telefonoContacto = lead.getContacto().getTelefono();
                }
            }
        }

        return LlamadaDTO.builder()
                .id(entity.getId().longValue())
                .idCampania(entity.getIdCampania().longValue())
                .idAgente(entity.getIdAgente().longValue())
                .fechaHora(entity.getInicio())
                .duracionSegundos((int) duracion.getSeconds())
                .resultado(entity.getResultado() != null ? entity.getResultado().getNombre() : null)
                .notas(entity.getNotas())
                .nombreContacto(nombreContacto)
                .telefonoContacto(telefonoContacto)
                // Campos de encuesta
                .encuestaEnviada(entity.getEncuestaEnviada())
                .estadoEncuesta(entity.getEncuestaEnviada() != null && entity.getEncuestaEnviada()
                        ? "ENVIADA"
                        : "NO_ENVIADA")
                .fechaEnvioEncuesta(entity.getFechaEnvioEncuesta())
                .urlEncuesta(entity.getUrlEncuesta())
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
                .objetivo(entity.getObjetivo())
                .tipo(entity.getTipo())
                .notasInternas(entity.getNotasInternas())
                .estado(entity.getActivo() ? "ACTIVO" : "INACTIVO")
                .pasos(entity.getSecciones() != null ? entity.getSecciones().stream()
                        .map(this::toSeccionGuionDTO)
                        .collect(Collectors.toList()) : List.of())
                .build();
    }

    /**
     * Convierte GuionSeccionEntity a SeccionGuionDTO
     */
    public SeccionGuionDTO toSeccionGuionDTO(GuionSeccionEntity entity) {
        if (entity == null)
            return null;

        return SeccionGuionDTO.builder()
                .id(entity.getId())
                .tipoSeccion(entity.getTipoSeccion())
                .contenido(entity.getContenido())
                .orden(entity.getOrden())
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
