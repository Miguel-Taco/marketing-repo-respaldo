package pe.unmsm.crm.marketing.leads.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.unmsm.crm.marketing.leads.api.dto.LeadIntegrationDTO;
import pe.unmsm.crm.marketing.leads.domain.enums.EstadoLead;
import pe.unmsm.crm.marketing.leads.domain.model.Lead;
import pe.unmsm.crm.marketing.leads.domain.repository.LeadRepository;
import pe.unmsm.crm.marketing.shared.domain.model.Distrito;
import pe.unmsm.crm.marketing.shared.domain.repository.DistritoRepository;
import pe.unmsm.crm.marketing.shared.logging.AuditoriaService;
import pe.unmsm.crm.marketing.shared.logging.ModuloLog;
import pe.unmsm.crm.marketing.shared.logging.AccionLog;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeadIntegrationService {

        private final LeadRepository leadRepository;
        private final DistritoRepository distritoRepository;
        private final AuditoriaService auditoriaService;

        @Transactional(readOnly = true)
        public List<LeadIntegrationDTO> obtenerLeadsParaSegmentacion(
                        LocalDate fechaDesde, LocalDate fechaHasta,
                        Integer edadMin, Integer edadMax,
                        String genero,
                        String distritoId, String provinciaId, String departamentoId,
                        String nivelEducativo, String estadoCivil) {

                // 1. Definir rango de fechas (inicio del día desde, fin del día hasta)
                LocalDateTime inicio = fechaDesde.atStartOfDay();
                LocalDateTime fin = fechaHasta.atTime(LocalTime.MAX);

                // 2. Definir estados permitidos (NUEVO y CALIFICADO)
                List<EstadoLead> estadosPermitidos = Arrays.asList(EstadoLead.NUEVO, EstadoLead.CALIFICADO);

                // 3. Buscar en repositorio con filtros dinámicos
                List<Lead> leads = leadRepository.filtrarLeadsParaSegmentacion(
                                estadosPermitidos, inicio, fin,
                                edadMin, edadMax, genero,
                                distritoId, provinciaId, departamentoId,
                                nivelEducativo, estadoCivil);

                // 4. Mapear a DTO
                List<LeadIntegrationDTO> resultado = leads.stream()
                                .map(this::mapToDTO)
                                .collect(Collectors.toList());

                // AUDITORÍA: Registrar consulta para segmentación
                auditoriaService.registrarEvento(
                                ModuloLog.LEADS,
                                AccionLog.ACTUALIZAR, // Operación de lectura/consulta
                                null, // No hay un solo lead, es una consulta general
                                null,
                                String.format("Leads consultados para segmentación: %d leads encontrados. Filtros - Fecha: %s a %s, Edad: %s-%s, Género: %s",
                                                resultado.size(), fechaDesde, fechaHasta,
                                                edadMin != null ? edadMin : "N/A",
                                                edadMax != null ? edadMax : "N/A",
                                                genero != null ? genero : "N/A"));

                return resultado;
        }

        @SuppressWarnings("null")
        private LeadIntegrationDTO mapToDTO(Lead lead) {
                // Obtener nombres de ubicación si hay distrito
                String distritoNombre = "";
                String provinciaNombre = "";
                String departamentoNombre = "";

                if (lead.getDemograficos() != null && lead.getDemograficos().getDistrito() != null) {
                        String distritoId = lead.getDemograficos().getDistrito();

                        // Obtener nombre del distrito desde el repositorio
                        distritoNombre = distritoRepository.findById(distritoId)
                                        .map(Distrito::getNombre)
                                        .orElse("");

                        // Mapeo temporal basado en códigos conocidos
                        // Formato: DDPPDD (Departamento-Provincia-Distrito)
                        // Ejemplo: "150101" = Lima (15), Lima (01), Cercado (01)
                        if (distritoId.startsWith("15")) {
                                departamentoNombre = "Lima";
                                if (distritoId.startsWith("1501")) {
                                        provinciaNombre = "Lima";
                                }
                        }
                }

                return LeadIntegrationDTO.builder()
                                .id(lead.getId())
                                .nombre(lead.getNombre())
                                .email(lead.getContacto().getEmail())
                                .telefono(lead.getContacto().getTelefono())
                                .estado(lead.getEstado().name())
                                .fechaCreacion(lead.getFechaCreacion())
                                // Demográficos
                                .edad(lead.getDemograficos() != null ? lead.getDemograficos().getEdad() : null)
                                .genero(lead.getDemograficos() != null ? lead.getDemograficos().getGenero() : null)
                                .distritoId(lead.getDemograficos() != null ? lead.getDemograficos().getDistrito()
                                                : null)
                                .distritoNombre(distritoNombre)
                                .provinciaNombre(provinciaNombre)
                                .departamentoNombre(departamentoNombre)
                                .nivelEducativo(lead.getDemograficos() != null
                                                ? lead.getDemograficos().getNivelEducativo()
                                                : null)
                                .estadoCivil(lead.getDemograficos() != null ? lead.getDemograficos().getEstadoCivil()
                                                : null)
                                // Tracking
                                .utmSource(lead.getTracking() != null ? lead.getTracking().getSource() : null)
                                .utmMedium(lead.getTracking() != null ? lead.getTracking().getMedium() : null)
                                .utmCampaign(lead.getTracking() != null ? lead.getTracking().getCampaign() : null)
                                .tipoFuente(lead.getFuenteTipo() != null ? lead.getFuenteTipo().name() : null)
                                .build();
        }
}
