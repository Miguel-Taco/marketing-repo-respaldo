package pe.unmsm.crm.marketing.campanas.telefonicas.infra.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.unmsm.crm.marketing.campanas.telefonicas.api.dto.CreateCampaniaTelefonicaRequest;
import pe.unmsm.crm.marketing.campanas.telefonicas.application.TelemarketingService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CampaignSyncService {

    private final JdbcTemplate jdbcTemplate;
    private final TelemarketingService telemarketingService;

    @Transactional
    public void syncCampaigns() {
        log.info("Iniciando sincronización de campañas telefónicas...");

        // 1. Buscar campañas programadas de tipo 'Llamadas'
        // Nota: Ajustar nombres de columnas según Script_marketing_3.sql
        String sql = "SELECT id_campana, nombre, fecha_programada_inicio, fecha_programada_fin, " +
                "id_segmento, id_plantilla, id_agente, prioridad " +
                "FROM campana " +
                "WHERE canal_ejecucion = 'Llamadas' AND estado = 'Programada'";

        List<Map<String, Object>> campaigns;
        try {
            campaigns = jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            log.error("Error consultando tabla 'campana'. Verifique que existe y tiene las columnas esperadas.", e);
            return;
        }

        log.info("Encontradas {} campañas para sincronizar", campaigns.size());

        for (Map<String, Object> row : campaigns) {
            try {
                processCampaign(row);
            } catch (Exception e) {
                log.error("Error procesando campaña ID: " + row.get("id_campana"), e);
            }
        }
    }

    private void processCampaign(Map<String, Object> row) {
        Long idCampana = ((Number) row.get("id_campana")).longValue();

        // Verificar si ya existe en campania_telefonica
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM campania_telefonica WHERE id_campana_gestion = ?",
                Integer.class, idCampana);

        if (count != null && count > 0) {
            log.info("Campaña {} ya existe en telemarketing, omitiendo.", idCampana);
            return;
        }

        // Obtener leads del segmento
        Long idSegmento = row.get("id_segmento") != null ? ((Number) row.get("id_segmento")).longValue() : null;
        List<Long> leadIds = Collections.emptyList();

        if (idSegmento != null) {
            try {
                leadIds = jdbcTemplate.queryForList(
                        "SELECT id_miembro FROM segmento_miembro WHERE id_segmento = ? AND tipo_miembro = 'LEAD'",
                        Long.class, idSegmento);
            } catch (Exception e) {
                log.warn("No se pudieron obtener leads del segmento {}. Tabla segmento_miembro podría no existir.",
                        idSegmento);
            }
        }

        // Construir request
        CreateCampaniaTelefonicaRequest request = new CreateCampaniaTelefonicaRequest();
        request.setNombre((String) row.get("nombre"));

        // Fechas (manejo de nulos y tipos)
        Object inicioObj = row.get("fecha_programada_inicio");
        Object finObj = row.get("fecha_programada_fin");

        LocalDate fechaInicio = LocalDate.now();
        if (inicioObj instanceof java.sql.Timestamp) {
            fechaInicio = ((java.sql.Timestamp) inicioObj).toLocalDateTime().toLocalDate();
        } else if (inicioObj instanceof java.time.LocalDateTime) {
            fechaInicio = ((java.time.LocalDateTime) inicioObj).toLocalDate();
        }

        LocalDate fechaFin = LocalDate.now().plusDays(30);
        if (finObj instanceof java.sql.Timestamp) {
            fechaFin = ((java.sql.Timestamp) finObj).toLocalDateTime().toLocalDate();
        } else if (finObj instanceof java.time.LocalDateTime) {
            fechaFin = ((java.time.LocalDateTime) finObj).toLocalDate();
        }

        request.setFechaInicio(fechaInicio);
        request.setFechaFin(fechaFin);

        request.setIdSegmento(idSegmento != null ? idSegmento : 0L); // 0L si es nulo para evitar error de validación
        request.setIdCampanaGestion(idCampana);
        request.setEstado("BORRADOR");

        Long idPlantilla = row.get("id_plantilla") != null ? ((Number) row.get("id_plantilla")).longValue() : null;
        request.setIdGuion(idPlantilla != null ? idPlantilla : 0L); // 0L si es nulo

        Long idAgente = row.get("id_agente") != null ? ((Number) row.get("id_agente")).longValue() : null;
        request.setIdsAgentes(idAgente != null ? List.of(idAgente) : Collections.emptyList());

        request.setLeadsIniciales(leadIds);

        String prioridad = (String) row.get("prioridad");
        request.setPrioridadColaDefault(prioridad != null ? prioridad.toUpperCase() : "MEDIA");

        // Crear campaña
        telemarketingService.crearCampania(request);
        log.info("Campaña {} sincronizada exitosamente.", idCampana);
    }
}
