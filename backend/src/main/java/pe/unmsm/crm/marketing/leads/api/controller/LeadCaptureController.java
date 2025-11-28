package pe.unmsm.crm.marketing.leads.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import pe.unmsm.crm.marketing.leads.api.dto.LeadCaptureRequest;
import pe.unmsm.crm.marketing.leads.api.mapper.LeadMapper;
import pe.unmsm.crm.marketing.leads.application.service.ImportService;
import pe.unmsm.crm.marketing.leads.application.service.LeadCaptureService;
import pe.unmsm.crm.marketing.leads.application.service.LeadProcessingService;
import pe.unmsm.crm.marketing.leads.domain.model.staging.EnvioFormulario;
import pe.unmsm.crm.marketing.leads.domain.model.staging.LoteImportacion;
import pe.unmsm.crm.marketing.leads.domain.enums.TipoFuente;
import pe.unmsm.crm.marketing.shared.utils.ResponseUtils;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/leads/capture")
@RequiredArgsConstructor
public class LeadCaptureController {

    private final LeadCaptureService captureService;
    private final LeadProcessingService processingService;
    private final ImportService importService;

    // Endpoint existente de Web...
    @PostMapping("/web")
    public ResponseEntity<Map<String, Object>> recibirFormulario(@RequestBody @Valid LeadCaptureRequest request) {
        Map<String, String> payloadStaging = LeadMapper.toStagingMap(request);
        EnvioFormulario envio = captureService.guardarEnvioWeb(payloadStaging);

        // Process the lead - DuplicateLeadException will propagate to
        // GlobalExceptionHandler -> HTTP 409
        processingService.procesarDesdeStaging(TipoFuente.WEB, envio);
        return ResponseUtils.success(Map.of("codigoSeguimiento", envio.getId()), "Lead procesado correctamente");
    }

    // --- NUEVO: Endpoint de Importación Masiva (Async) ---
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importarArchivo(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El archivo está vacío"));
        }

        // Inicia el proceso y retorna inmediatamente
        LoteImportacion lote = importService.iniciarImportacion(file);

        return ResponseUtils.success(lote, "Importación iniciada. Procesando en segundo plano...");
    }

    // --- NUEVO: Endpoint para verificar estado ---
    @GetMapping("/import/{id}")
    public ResponseEntity<Map<String, Object>> obtenerEstadoImportacion(@PathVariable Long id) {
        LoteImportacion lote = importService.obtenerLotePorId(id);
        return ResponseUtils.success(lote, "Estado del lote recuperado");
    }

    // --- NUEVO: Historial de Importaciones con Paginación ---
    @GetMapping("/import/history")
    public ResponseEntity<Map<String, Object>> obtenerHistorialImportaciones(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<LoteImportacion> pageResult = importService.obtenerHistorial(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", pageResult.getContent());
        response.put("currentPage", pageResult.getNumber());
        response.put("totalPages", pageResult.getTotalPages());
        response.put("totalElements", pageResult.getTotalElements());
        response.put("hasNext", pageResult.hasNext());
        response.put("hasPrevious", pageResult.hasPrevious());

        return ResponseEntity.ok(response);
    }
}