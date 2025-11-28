package pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.unmsm.crm.marketing.campanas.telefonicas.api.dto.*;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.CampaignDataProvider;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.entity.*;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.mapper.CampaignMapper;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.repository.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JpaCampaignDataProvider implements CampaignDataProvider {

        private final CampaniaTelefonicaRepository campaniaRepo;
        private final ColaLlamadaRepository colaRepo;
        private final LlamadaRepository llamadaRepo;
        private final GuionRepository guionRepo;
        private final ResultadoLlamadaRepository resultadoRepo;
        private final CampaniaAgenteRepository campaniaAgenteRepo;
        private final CampaignMapper mapper;

        @Override
        @Transactional(readOnly = true)
        public List<CampaniaTelefonicaDTO> obtenerCampaniasPorAgente(Long idAgente) {
                log.info("Obteniendo campañas para agente: {}", idAgente);
                List<CampaniaTelefonicaEntity> entities = campaniaRepo.findByAgenteId(idAgente.intValue());
                return entities.stream()
                                .map(mapper::toDTO)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public CampaniaTelefonicaDTO obtenerCampaniaPorId(Long id) {
                log.info("Obteniendo campaña por ID: {}", id);
                return campaniaRepo.findById(id.intValue())
                                .map(mapper::toDTO)
                                .orElseThrow(() -> new RuntimeException("Campaña no encontrada: " + id));
        }

        @Override
        @Transactional
        public CampaniaTelefonicaDTO crearCampania(CreateCampaniaTelefonicaRequest request) {
                log.info("Creando nueva campaña: {}", request.getNombre());

                CampaniaTelefonicaEntity entity = new CampaniaTelefonicaEntity();
                entity.setNombre(request.getNombre());
                entity.setFechaInicio(request.getFechaInicio());
                entity.setFechaFin(request.getFechaFin());
                entity.setIdSegmento(request.getIdSegmento());
                entity.setIdCampanaGestion(request.getIdCampanaGestion());
                entity.setEstado("BORRADOR");
                entity.setEsArchivado(false);
                entity.setIdGuion(request.getIdGuion() != null ? request.getIdGuion().intValue() : null);
                entity.setPrioridad(mapPrioridad(request.getPrioridadColaDefault()));

                CampaniaTelefonicaEntity saved = campaniaRepo.save(entity);
                log.info("Campaña creada con ID: {}", saved.getId());

                // Procesar leads iniciales
                if (request.getLeadsIniciales() != null && !request.getLeadsIniciales().isEmpty()) {
                        log.info("Procesando {} leads iniciales", request.getLeadsIniciales().size());
                        List<ColaLlamadaEntity> cola = request.getLeadsIniciales().stream()
                                        .map(leadId -> {
                                                ColaLlamadaEntity item = new ColaLlamadaEntity();
                                                item.setIdCampania(saved.getId());
                                                item.setIdLead(leadId);
                                                item.setPrioridadCola(saved.getPrioridad() != null
                                                                ? saved.getPrioridad().name()
                                                                : "MEDIA");
                                                item.setEstadoEnCola("PENDIENTE");
                                                return item;
                                        })
                                        .collect(Collectors.toList());
                        colaRepo.saveAll(cola);
                }

                return mapper.toDTO(saved);
        }

        @Override
        @Transactional(readOnly = true)
        public List<ContactoDTO> obtenerContactosDeCampania(Long idCampania) {
                log.info("Obteniendo contactos de campaña: {}", idCampania);

                List<ColaLlamadaEntity> cola = colaRepo.findByCampaniaAndEstadoOrderByPrioridad(
                                idCampania.intValue(),
                                Arrays.asList("PENDIENTE", "EN_PROCESO"));

                return cola.stream()
                                .map(mapper::toContactoDTO)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public ContactoDTO obtenerContactoPorId(Long idContacto) {
                log.info("Obteniendo contacto por ID: {}", idContacto);

                return colaRepo.findById(idContacto.intValue())
                                .map(mapper::toContactoDTO)
                                .orElseThrow(() -> new RuntimeException("Contacto no encontrado: " + idContacto));
        }

        @Override
        @Transactional(readOnly = true)
        public List<ContactoDTO> obtenerCola(Long idCampania) {
                log.info("Obteniendo cola de campaña: {}", idCampania);

                List<ColaLlamadaEntity> pendientes = colaRepo.findByCampaniaAndEstadoOrderByPrioridad(
                                idCampania.intValue(),
                                Arrays.asList("PENDIENTE"));

                return pendientes.stream()
                                .map(mapper::toContactoDTO)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional
        public ContactoDTO obtenerSiguienteContacto(Long idCampania, Long idAgente) {
                log.info("Obteniendo siguiente contacto para campaña: {}, agente: {}", idCampania, idAgente);

                return colaRepo.findNextAvailableContact(
                                idCampania.intValue(),
                                idAgente.intValue(),
                                PageRequest.of(0, 1))
                                .stream()
                                .findFirst()
                                .map(mapper::toContactoDTO)
                                .orElse(null);
        }

        @Override
        @Transactional
        public ContactoDTO tomarContacto(Long idCampania, Long idContacto, Long idAgente) {
                log.info("Asignando contacto [campaniaId={}, contactoId={}, agenteId={}]",
                                idCampania, idContacto, idAgente);

                ColaLlamadaEntity contacto = colaRepo.findById(idContacto.intValue())
                                .orElseThrow(() -> new RuntimeException("Contacto no encontrado: " + idContacto));

                // Validar que esté pendiente
                if (!"PENDIENTE".equals(contacto.getEstadoEnCola())) {
                        throw new RuntimeException(
                                        "Contacto ya está en estado: " + contacto.getEstadoEnCola());
                }

                // Asignar al agente
                contacto.setIdAgenteActual(idAgente.intValue());
                contacto.setEstadoEnCola("EN_PROCESO");
                colaRepo.save(contacto);

                log.info("Contacto asignado exitosamente [contactoId={}, agenteId={}]",
                                idContacto, idAgente);

                return mapper.toContactoDTO(contacto);
        }

        @Override
        @Transactional
        public void pausarCola(Long idAgente, Long idCampania) {
                log.info("Pausando cola para agente: {}, campaña: {}", idAgente, idCampania);
                // Implementación según lógica de negocio
                // Por ahora, solo log
        }

        @Override
        @Transactional
        public void reanudarCola(Long idAgente, Long idCampania) {
                log.info("Reanudando cola para agente: {}, campaña: {}", idAgente, idCampania);
                // Implementación según lógica de negocio
                // Por ahora, solo log
        }

        @Override
        @Transactional(readOnly = true)
        public LlamadaDTO obtenerLlamada(Long idLlamada) {
                log.info("Obteniendo llamada por ID: {}", idLlamada);

                return llamadaRepo.findById(idLlamada.intValue())
                                .map(mapper::toLlamadaDTO)
                                .orElseThrow(() -> new RuntimeException("Llamada no encontrada: " + idLlamada));
        }

        @Override
        @Transactional
        public LlamadaDTO registrarResultadoLlamada(Long idCampania, Long idAgente, ResultadoLlamadaRequest request) {
                log.info("Registrando resultado de llamada [campaniaId={}, agenteId={}]",
                                idCampania, idAgente);

                // 1. Crear registro de llamada
                LlamadaEntity llamada = new LlamadaEntity();
                llamada.setInicio(request.getInicio());
                llamada.setFin(request.getFin());
                llamada.setIdAgente(idAgente.intValue());
                llamada.setIdLead(request.getIdLead());
                llamada.setIdCampania(idCampania.intValue());
                llamada.setIdResultado(request.getIdResultado() != null ? request.getIdResultado().intValue() : null);
                llamada.setNotas(request.getNotas());

                LlamadaEntity savedLlamada = llamadaRepo.save(llamada);
                log.info("Llamada registrada con ID: {}", savedLlamada.getId());

                // 2. Actualizar estado en cola si se proporciona el ID del contacto
                if (request.getIdContactoCola() != null) {
                        ColaLlamadaEntity contacto = colaRepo.findById(request.getIdContactoCola().intValue())
                                        .orElseThrow(() -> new RuntimeException("Contacto en cola no encontrado"));

                        contacto.setEstadoEnCola("COMPLETADO");
                        contacto.setIdAgenteActual(null); // Liberar asignación
                        colaRepo.save(contacto);

                        log.info("Estado de contacto actualizado a COMPLETADO [contactoId={}]",
                                        request.getIdContactoCola());
                }

                return mapper.toLlamadaDTO(savedLlamada);
        }

        @Override
        @Transactional(readOnly = true)
        public List<LlamadaDTO> obtenerHistorialLlamadas(Long idCampania, Long idAgente) {
                log.info("Obteniendo historial de llamadas [campaniaId={}, agenteId={}]",
                                idCampania, idAgente);

                List<LlamadaEntity> llamadas = llamadaRepo.findByIdCampaniaAndIdAgenteOrderByInicioDesc(
                                idCampania.intValue(),
                                idAgente.intValue());

                return llamadas.stream()
                                .map(mapper::toLlamadaDTO)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public GuionDTO obtenerGuionDeCampania(Long idCampania) {
                log.info("Obteniendo guión de campaña: {}", idCampania);

                CampaniaTelefonicaEntity campania = campaniaRepo.findById(idCampania.intValue())
                                .orElseThrow(() -> new RuntimeException("Campaña no encontrada: " + idCampania));

                if (campania.getIdGuion() == null) {
                        log.warn("Campaña {} no tiene guión asignado", idCampania);
                        return null;
                }

                return guionRepo.findById(campania.getIdGuion())
                                .map(mapper::toGuionDTO)
                                .orElse(null);
        }

        @Override
        @Transactional(readOnly = true)
        public List<GuionDTO> listarTodosLosGuiones() {
                return guionRepo.findAll().stream()
                                .map(mapper::toGuionDTO)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public MetricasAgenteDTO obtenerMetricasAgente(Long idCampania, Long idAgente) {
                log.info("Obteniendo métricas de agente [campaniaId={}, agenteId={}]",
                                idCampania, idAgente);

                LocalDateTime desde = LocalDateTime.now().minusDays(30);
                var metricas = llamadaRepo.getMetricasByAgenteAndCampania(
                                idAgente.intValue(),
                                idCampania.intValue(),
                                desde);

                return mapper.toMetricasAgenteDTO(metricas);
        }

        /**
         * Helper method to map priority from database (ALTA, MEDIA, BAJA) to enum
         * values (Alta, Media, Baja)
         */
        private CampaniaTelefonicaEntity.PrioridadEnum mapPrioridad(String prioridad) {
                if (prioridad == null) {
                        return CampaniaTelefonicaEntity.PrioridadEnum.Media;
                }

                switch (prioridad.toUpperCase()) {
                        case "ALTA":
                                return CampaniaTelefonicaEntity.PrioridadEnum.Alta;
                        case "BAJA":
                                return CampaniaTelefonicaEntity.PrioridadEnum.Baja;
                        case "MEDIA":
                        default:
                                return CampaniaTelefonicaEntity.PrioridadEnum.Media;
                }
        }
}
