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
                List<CampaniaTelefonicaEntity> entities = campaniaRepo.findByAgenteId();
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
        }

        @Override
        @Transactional
        public void reanudarCola(Long idAgente, Long idCampania) {
                log.info("Reanudando cola para agente: {}, campaña: {}", idAgente, idCampania);
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

                // Resolver ID del resultado
                Integer idResultado = null;

                // 1. Intentar obtener del request directo
                if (request.getIdResultado() != null) {
                        idResultado = request.getIdResultado().intValue();
                }
                // 2. Si no viene ID, buscar por código (string)
                else if (request.getResultado() != null) {
                        idResultado = resultadoRepo.findByResultado(request.getResultado())
                                        .map(ResultadoLlamadaEntity::getId)
                                        .orElse(null);

                        if (idResultado == null) {
                                log.warn("Código de resultado no encontrado: {}", request.getResultado());
                        }
                }

                llamada.setIdResultado(idResultado);
                llamada.setNotas(request.getNotas());
                llamada.setDuracionSegundos(request.getDuracionSegundos());

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

                Integer campaniaId = idCampania != null ? idCampania.intValue() : null;
                if (campaniaId == null) {
                        throw new IllegalArgumentException("El id de campaña es requerido para obtener el historial");
                }

                List<LlamadaEntity> llamadas;
                if (idAgente == null) {
                        llamadas = llamadaRepo.findByIdCampaniaOrderByInicioDesc(campaniaId);
                } else {
                        llamadas = llamadaRepo.findByIdCampaniaAndIdAgenteOrderByInicioDesc(
                                        campaniaId,
                                        idAgente.intValue());
                }

                return llamadas.stream()
                                .map(mapper::toLlamadaDTO)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public GuionDTO obtenerGuionDeCampania(Long idCampania) {
                log.info("Obteniendo guión de campaña: {}", idCampania);

                // Buscar guion activo asociado a la campaña
                List<GuionEntity> guiones = guionRepo.findByIdCampaniaAndActivoTrue(idCampania);

                if (guiones.isEmpty()) {
                        log.warn("No se encontró guión activo para campaña: {}", idCampania);
                        return null;
                }

                // Si hay múltiples activos, tomar el más reciente por ID
                GuionEntity guion = guiones.stream()
                                .max((g1, g2) -> Integer.compare(g1.getId(), g2.getId()))
                                .orElse(null);

                return guion != null ? mapper.toGuionDTO(guion) : null;
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

        @Override
        @Transactional(readOnly = true)
        public MetricasDiariasDTO obtenerMetricasDiarias(Long idCampania, Long idAgente) {
                log.info("Obteniendo métricas diarias [campaniaId={}, agenteId={}]",
                                idCampania, idAgente);

                // Calcular rango de "hoy" en Java para evitar problemas de timezone
                LocalDateTime ahora = LocalDateTime.now();
                LocalDateTime inicioDia = ahora.toLocalDate().atStartOfDay();
                LocalDateTime finDia = ahora.toLocalDate().atTime(23, 59, 59);

                // Contar pendientes
                Long pendientes = colaRepo.countByEstadoAndCampaign(idCampania.intValue(), "PENDIENTE");

                // Contar llamadas realizadas hoy (con rango de fechas)
                Long realizadasHoy = llamadaRepo.countLlamadasHoy(
                                idCampania.intValue(),
                                idAgente.intValue(),
                                inicioDia,
                                finDia);

                // Contar llamadas efectivas hoy (con rango de fechas)
                Long efectivasHoy = llamadaRepo.countLlamadasEfectivasHoy(
                                idCampania.intValue(),
                                idAgente.intValue(),
                                inicioDia,
                                finDia);

                return MetricasDiariasDTO.builder()
                                .pendientes(pendientes != null ? pendientes : 0L)
                                .realizadasHoy(realizadasHoy != null ? realizadasHoy : 0L)
                                .efectivasHoy(efectivasHoy != null ? efectivasHoy : 0L)
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        public MetricasCampaniaDTO obtenerMetricasCampania(Long idCampania, Integer dias) {
                log.info("Obteniendo métricas de campaña [campaniaId={}, dias={}]", idCampania, dias);

                Integer campaniaId = idCampania.intValue();

                // 1. Resumen general
                Long totalLeads = colaRepo.countTotalByCampaign(campaniaId);
                Long leadsPendientes = colaRepo.countPendingByCampaign(campaniaId);

                java.util.Map<String, Object> metricasGenerales = llamadaRepo.getMetricasByCampania(campaniaId);
                Long totalLlamadas = (Long) metricasGenerales.get("totalLlamadas");
                Number duracionPromedioNum = (Number) metricasGenerales.get("duracionPromedio");
                Integer duracionPromedio = duracionPromedioNum != null
                                ? duracionPromedioNum.intValue()
                                : 0;

                // 2. Distribución de resultados
                List<Object[]> resultadosRaw = llamadaRepo.countByResultadoAndCampania(campaniaId);
                java.util.Map<String, ResultadoDistribucionDTO> distribucionResultados = new java.util.HashMap<>();

                for (Object[] row : resultadosRaw) {
                        String resultado = (String) row[0]; // Código (ej. "CONTACTADO")
                        String nombre = (String) row[1]; // Nombre display (ej. "Contactado")
                        Long count = (Long) row[2]; // Count

                        // Handle nulls for calls without result (LEFT JOIN)
                        if (resultado == null)
                                resultado = "SIN_RESULTADO";
                        if (nombre == null)
                                nombre = "Sin Resultado";

                        // DEBUG: Log para ver qué valores devuelve la BD
                        log.info("DEBUG - Resultado de BD: resultado='{}', nombre='{}', count={}", resultado, nombre,
                                        count);

                        Double porcentaje = totalLlamadas > 0
                                        ? (count.doubleValue() / totalLlamadas.doubleValue()) * 100
                                        : 0.0;

                        // Usar 'resultado' (código) como key para que el filtro funcione correctamente
                        distribucionResultados.put(resultado, ResultadoDistribucionDTO.builder()
                                        .resultado(resultado)
                                        .nombre(nombre)
                                        .count(count)
                                        .porcentaje(porcentaje)
                                        .build());
                }

                // FIXED: Calcular leads contactados basado en llamadas efectivas (CONTACTADO,
                // INTERESADO)
                Long leadsContactados = distribucionResultados.values().stream()
                                .filter(d -> d.getResultado().equals("CONTACTADO")
                                                || d.getResultado().equals("INTERESADO"))
                                .mapToLong(ResultadoDistribucionDTO::getCount)
                                .sum();

                // DEBUG: Log para ver el resultado del filtrado
                log.info("DEBUG - Leads contactados calculados: {}", leadsContactados);
                log.info("DEBUG - Distribución completa: {}", distribucionResultados.keySet());

                Double porcentajeAvance = totalLeads > 0
                                ? (leadsContactados.doubleValue() / totalLeads.doubleValue()) * 100
                                : 0.0;

                // 3. Análisis temporal
                LocalDateTime fin = LocalDateTime.now().toLocalDate().atTime(23, 59, 59); // End of today
                LocalDateTime inicio = fin.minusDays(dias != null ? dias : 30);

                List<java.util.Map<String, Object>> llamadasDiarias = llamadaRepo.countLlamadasPorDia(
                                campaniaId, inicio, fin);

                List<LlamadasPorDiaDTO> llamadasPorDia = llamadasDiarias.stream()
                                .map(map -> LlamadasPorDiaDTO.builder()
                                                .fecha(((java.sql.Date) map.get("fecha")).toLocalDate())
                                                .totalLlamadas((Long) map.get("total"))
                                                .llamadasEfectivas((Long) map.get("efectivas"))
                                                .build())
                                .collect(Collectors.toList());

                List<Object[]> llamadasPorHoraRaw = llamadaRepo.countLlamadasPorHora(campaniaId);
                java.util.Map<Integer, Long> llamadasPorHora = llamadasPorHoraRaw.stream()
                                .collect(Collectors.toMap(
                                                row -> (Integer) row[0],
                                                row -> (Long) row[1]));

                // 4. Rendimiento por agente
                List<java.util.Map<String, Object>> rendimientoRaw = llamadaRepo.getRendimientoPorAgente(campaniaId);
                List<RendimientoAgenteDTO> rendimientoPorAgente = rendimientoRaw.stream()
                                .map(map -> {
                                        Long llamadas = (Long) map.get("llamadasRealizadas");
                                        Long efectivos = (Long) map.get("contactosEfectivos");
                                        Double tasaExito = llamadas > 0
                                                        ? (efectivos.doubleValue() / llamadas.doubleValue()) * 100
                                                        : 0.0;

                                        // Obtener llamadas hoy para este agente
                                        LocalDateTime hoy = LocalDateTime.now();
                                        LocalDateTime inicioHoy = hoy.toLocalDate().atStartOfDay();
                                        LocalDateTime finHoy = hoy.toLocalDate().atTime(23, 59, 59);
                                        Long llamadasHoy = llamadaRepo.countLlamadasHoy(
                                                        campaniaId,
                                                        (Integer) map.get("idAgente"),
                                                        inicioHoy,
                                                        finHoy);

                                        return RendimientoAgenteDTO.builder()
                                                        .idAgente(((Number) map.get("idAgente")).longValue())
                                                        .nombreAgente((String) map.get("nombreAgente"))
                                                        .llamadasRealizadas(llamadas)
                                                        .contactosEfectivos(efectivos)
                                                        .tasaExito(tasaExito)
                                                        .duracionPromedio(
                                                                        ((Number) map.get("duracionPromedio"))
                                                                                        .intValue())
                                                        .llamadasHoy(llamadasHoy)
                                                        .build();
                                })
                                .collect(Collectors.toList());

                // 5. Métricas de calidad - FIXED: usar llamadas efectivas en lugar de cualquier
                // resultado
                Long llamadasConResultado = (Long) metricasGenerales.get("conResultado");
                Double tasaContactoGlobal = totalLlamadas > 0
                                ? (leadsContactados.doubleValue() / totalLlamadas.doubleValue()) * 100
                                : 0.0;

                Double tasaEfectividad = llamadasConResultado > 0
                                ? (leadsContactados.doubleValue() / llamadasConResultado.doubleValue()) * 100
                                : 0.0;

                java.util.Map<String, Object> duraciones = llamadaRepo.getDuracionPromedioByEfectividad(campaniaId);
                Integer duracionPromedioEfectivas = duraciones.get("duracionEfectivas") != null
                                ? ((Number) duraciones.get("duracionEfectivas")).intValue()
                                : 0;
                Integer duracionPromedioNoEfectivas = duraciones.get("duracionNoEfectivas") != null
                                ? ((Number) duraciones.get("duracionNoEfectivas")).intValue()
                                : 0;

                // 6. Estado de cola
                List<Object[]> porPrioridad = colaRepo.countByPrioridadAndCampaign(campaniaId);
                java.util.Map<String, Long> leadsPorPrioridad = porPrioridad.stream()
                                .collect(Collectors.toMap(
                                                row -> (String) row[0],
                                                row -> (Long) row[1]));

                java.util.Map<String, Long> leadsPorEstado = new java.util.HashMap<>();
                leadsPorEstado.put("PENDIENTE", leadsPendientes);
                leadsPorEstado.put("COMPLETADO", leadsContactados);
                leadsPorEstado.put("EN_PROCESO",
                                colaRepo.countByEstadoAndCampaign(campaniaId, "EN_PROCESO"));

                // Construir DTO final
                return MetricasCampaniaDTO.builder()
                                .totalLeads(totalLeads)
                                .leadsContactados(leadsContactados)
                                .leadsPendientes(leadsPendientes)
                                .porcentajeAvance(porcentajeAvance)
                                .totalLlamadas(totalLlamadas)
                                .duracionPromedio(duracionPromedio)
                                .distribucionResultados(distribucionResultados)
                                .llamadasPorDia(llamadasPorDia)
                                .llamadasPorHora(llamadasPorHora)
                                .rendimientoPorAgente(rendimientoPorAgente)
                                .tasaContactoGlobal(tasaContactoGlobal)
                                .tasaEfectividad(tasaEfectividad)
                                .duracionPromedioEfectivas(duracionPromedioEfectivas)
                                .duracionPromedioNoEfectivas(duracionPromedioNoEfectivas)
                                .leadsPorPrioridad(leadsPorPrioridad)
                                .leadsPorEstado(leadsPorEstado)
                                .build();
        }

        @Override
        @Transactional
        public ContactoDTO agregarContactoUrgente(AddUrgentContactRequest request) {
                log.info("Agregando contacto urgente [idLead={}, idEncuesta={}]",
                                request.getIdLead(), request.getIdEncuesta());

                // 1. Buscar la campaña telefónica asociada a la encuesta
                List<CampaniaTelefonicaEntity> campanias = campaniaRepo.findByIdEncuesta(request.getIdEncuesta());

                if (campanias.isEmpty()) {
                        throw new IllegalArgumentException(
                                        "No existe campaña telefónica asociada a la encuesta ID: "
                                                        + request.getIdEncuesta());
                }

                // Si hay múltiples campañas, tomar la primera activa (no COMPLETADA ni
                // ARCHIVADA)
                CampaniaTelefonicaEntity campania = campanias.stream()
                                .filter(c -> !c.getEstado().equals("COMPLETADA") && !c.getEsArchivado())
                                .findFirst()
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "No hay campañas activas para la encuesta ID: "
                                                                + request.getIdEncuesta()));

                Integer idCampania = campania.getId();

                // 2. Verificar si el lead ya está en la cola
                java.util.Optional<ColaLlamadaEntity> existente = colaRepo.findByIdCampaniaAndIdLead(
                                idCampania,
                                request.getIdLead());

                ColaLlamadaEntity colaEntity;

                if (existente.isPresent()) {
                        // Si ya existe, actualizar su prioridad a ALTA
                        colaEntity = existente.get();
                        colaEntity.setPrioridadCola("ALTA");

                        // Si ya fue completado, volver a PENDIENTE para que sea rellamado
                        if ("COMPLETADO".equals(colaEntity.getEstadoEnCola())) {
                                colaEntity.setEstadoEnCola("PENDIENTE");
                                colaEntity.setIdAgenteActual(null); // Liberar agente
                        }

                        log.info("Actualizando prioridad a ALTA para lead {} en campaña {}",
                                        request.getIdLead(), idCampania);
                } else {
                        // Si no existe, crear nueva entrada con prioridad ALTA
                        colaEntity = new ColaLlamadaEntity();
                        colaEntity.setIdCampania(idCampania);
                        colaEntity.setIdLead(request.getIdLead());
                        colaEntity.setPrioridadCola("ALTA");
                        colaEntity.setEstadoEnCola("PENDIENTE");

                        log.info("Agregando nuevo lead {} a cola de campaña {} con prioridad ALTA",
                                        request.getIdLead(), idCampania);
                }

                // 3. Guardar en la base de datos
                ColaLlamadaEntity saved = colaRepo.save(colaEntity);

                // 4. Convertir a DTO y retornar
                return mapper.toContactoDTO(saved);
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
