package pe.unmsm.crm.marketing.leads.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import pe.unmsm.crm.marketing.leads.domain.enums.TipoFuente;
import pe.unmsm.crm.marketing.leads.domain.event.ImportProgressUpdate;
import pe.unmsm.crm.marketing.leads.domain.model.staging.LoteImportacion;
import pe.unmsm.crm.marketing.leads.domain.model.staging.RegistroImportado;
import pe.unmsm.crm.marketing.leads.domain.repository.LoteRepository;
import pe.unmsm.crm.marketing.leads.domain.repository.RegistroImportadoRepository;
import pe.unmsm.crm.marketing.leads.domain.enums.EstadoCaptacion;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

/**
 * Servicio separado para procesamiento asíncrono de importaciones.
 * IMPORTANTE: Debe ser una clase separada para que @Async funcione
 * correctamente
 * (Spring AOP no puede interceptar llamadas this.metodoAsync())
 */
@Service
@RequiredArgsConstructor
public class AsyncImportProcessor {

    private final RegistroImportadoRepository registroRepository;
    private final LoteRepository loteRepository;
    private final LeadProcessingService processingService;
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Procesa las filas en segundo plano.
     * IMPORTANTE: Este método se ejecuta async porque es llamado desde
     * ImportService (otra clase)
     */
    @Async
    @SuppressWarnings("null")
    public void procesarFilasAsync(long loteId, List<Map<String, String>> filas) {
        try {
            // Obtener nombre de archivo para el progreso
            LoteImportacion lote = loteRepository.findById(loteId).orElseThrow();
            String nombreArchivo = lote.getNombreArchivo();

            // ** ENVIAR MENSAJE INICIAL DE PROGRESO AL 0% INMEDIATAMENTE **
            ImportProgressUpdate initialProgress = ImportProgressUpdate.builder()
                    .loteId(loteId)
                    .nombreArchivo(nombreArchivo)
                    .totalRegistros(filas.size())
                    .procesados(0)
                    .exitosos(0)
                    .duplicados(0)
                    .conErrores(0)
                    .completado(false)
                    .build();

            messagingTemplate.convertAndSend("/topic/import-progress/" + loteId, (Object) initialProgress);

            // IMPORTANTE: Dar tiempo al frontend para conectarse al WebSocket
            try {
                Thread.sleep(500); // Reducido de 1000 a 500ms
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            int exitosos = 0;
            int duplicados = 0;
            int conErrores = 0;
            int batchSize = 50;

            for (int i = 0; i < filas.size(); i += batchSize) {
                int end = Math.min(i + batchSize, filas.size());
                List<Map<String, String>> batchFilas = filas.subList(i, end);
                List<RegistroImportado> registrosBatch = new java.util.ArrayList<>();

                // 1. Preparar y guardar lote inicial de registros (Bulk Insert)
                for (Map<String, String> fila : batchFilas) {
                    RegistroImportado registro = new RegistroImportado();
                    registro.setLoteId(loteId);
                    registro.setEstadoProcesoId(EstadoCaptacion.EN_PROCESO);
                    try {
                        registro.setDatosJson(objectMapper.writeValueAsString(fila));
                    } catch (Exception e) {
                        registro.setDatosJson("{}");
                    }
                    registrosBatch.add(registro);
                }

                // Guardamos el batch inicial para obtener IDs
                registrosBatch = registroRepository.saveAll(registrosBatch);

                // 2. Procesar cada registro del batch Y enviar actualización después de cada
                // uno
                int procesadosEnBatch = 0;
                for (RegistroImportado registro : registrosBatch) {
                    try {
                        processingService.procesarDesdeStaging(TipoFuente.IMPORTACION, registro);
                        registro.setEstadoProcesoId(EstadoCaptacion.VALIDADO);
                        exitosos++;
                    } catch (pe.unmsm.crm.marketing.shared.infra.exception.DuplicateLeadException e) {
                        registro.setEstadoProcesoId(EstadoCaptacion.RECHAZADO);
                        registro.setMotivoRechazo("DUPLICADO: " + e.getMessage());
                        duplicados++;
                    } catch (Exception e) {
                        registro.setEstadoProcesoId(EstadoCaptacion.RECHAZADO);
                        registro.setMotivoRechazo(
                                e.getMessage() != null
                                        ? e.getMessage().substring(0, Math.min(500, e.getMessage().length()))
                                        : "Error desconocido");
                        conErrores++;
                    }

                    procesadosEnBatch++;
                    int totalProcesado = i + procesadosEnBatch;

                    // ** ENVIAR ACTUALIZACIÓN DESPUÉS DE CADA LEAD **
                    // Esto asegura que el usuario vea progreso en tiempo real
                    ImportProgressUpdate progress = ImportProgressUpdate.builder()
                            .loteId(loteId)
                            .nombreArchivo(nombreArchivo)
                            .totalRegistros(filas.size())
                            .procesados(totalProcesado)
                            .exitosos(exitosos)
                            .duplicados(duplicados)
                            .conErrores(conErrores)
                            .completado(false)
                            .build();

                    messagingTemplate.convertAndSend("/topic/import-progress/" + loteId, (Object) progress);
                }

                // 3. Actualizar estados del batch (Bulk Update)
                registroRepository.saveAll(registrosBatch);

                // --- ACTUALIZACIÓN INCREMENTAL DEL LOTE ---
                // Esto permite que el polling funcione si el WebSocket falla
                lote.setExitosos(exitosos);
                lote.setDuplicados(duplicados);
                lote.setConErrores(conErrores);
                lote.setRechazados(duplicados + conErrores);
                loteRepository.save(lote);
            }

            // Enviar actualización final de completado
            ImportProgressUpdate finalProgress = ImportProgressUpdate.builder()
                    .loteId(loteId)
                    .nombreArchivo(nombreArchivo)
                    .totalRegistros(filas.size())
                    .procesados(filas.size())
                    .exitosos(exitosos)
                    .duplicados(duplicados)
                    .conErrores(conErrores)
                    .completado(true)
                    .build();

            messagingTemplate.convertAndSend("/topic/import-progress/" + loteId, (Object) finalProgress);

        } catch (Exception e) {
            e.printStackTrace();
            // En caso de error fatal, intentar marcar el lote como fallido si fuera posible
            // O al menos loguearlo.
        }
    }

}
