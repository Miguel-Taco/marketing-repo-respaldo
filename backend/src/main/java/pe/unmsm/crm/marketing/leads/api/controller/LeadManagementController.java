package pe.unmsm.crm.marketing.leads.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import pe.unmsm.crm.marketing.leads.api.dto.CambioEstadoRequest;
import pe.unmsm.crm.marketing.leads.api.dto.CambioEstadoLoteRequest;
import pe.unmsm.crm.marketing.leads.api.dto.LeadResponse;
import pe.unmsm.crm.marketing.leads.api.mapper.LeadMapper;
import pe.unmsm.crm.marketing.leads.application.service.LeadManagementService;
import pe.unmsm.crm.marketing.leads.application.service.LeadExportService;
import pe.unmsm.crm.marketing.leads.domain.enums.EstadoLead;
import pe.unmsm.crm.marketing.leads.domain.enums.TipoFuente;
import pe.unmsm.crm.marketing.leads.domain.model.Lead;
import pe.unmsm.crm.marketing.leads.domain.repository.HistorialRepository;
import pe.unmsm.crm.marketing.leads.domain.repository.LeadRepository;
import pe.unmsm.crm.marketing.shared.utils.PaginationUtils;
import pe.unmsm.crm.marketing.shared.utils.ResponseUtils;
import pe.unmsm.crm.marketing.shared.infra.exception.NotFoundException;
import pe.unmsm.crm.marketing.shared.application.service.UbigeoService;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/leads")
@RequiredArgsConstructor
@Validated
public class LeadManagementController {

    private final LeadManagementService managementService;
    private final LeadRepository leadRepository;
    private final HistorialRepository historialRepository;
    private final UbigeoService ubigeoService;
    private final LeadExportService exportService;

    // --- ENDPOINT 1: LISTAR CON FILTROS ---
    @GetMapping
    public ResponseEntity<Map<String, Object>> listarLeads(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "fechaCreacion") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            // Filtros opcionales
            @RequestParam(required = false) EstadoLead estado,
            @RequestParam(required = false) TipoFuente fuenteTipo,
            @RequestParam(required = false) String search) {

        // Mapeo de campos de ordenamiento para Query Nativa
        String dbSort = sort;
        if ("fechaCreacion".equals(sort)) {
            dbSort = "fecha_creacion";
        } else if ("nombre".equals(sort)) {
            dbSort = "nombre_completo";
        } else if ("estado".equals(sort)) {
            dbSort = "estado_lead_id";
        }

        Pageable pageable = PaginationUtils.buildPageable(page, size, dbSort, direction);

        // Usamos el método 'buscarLeads' con todos los filtros
        Page<Lead> leadsPage = leadRepository.buscarLeads(estado, fuenteTipo, search, pageable);

        Page<LeadResponse> responsePage = leadsPage.map(LeadMapper::toResponse);

        return ResponseUtils.success(responsePage, "Listado de leads recuperado correctamente");
    }

    // --- ENDPOINT 2: DETALLE POR ID ---
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerLead(@PathVariable Long id) {

        Lead lead = managementService.findLeadById(id);

        if (lead == null) {
            throw new NotFoundException("Lead", id);
        }

        // Obtener nombres de ubigeo si existe distrito
        Map<String, String> ubigeoNombres = null;
        if (lead.getDemograficos() != null && lead.getDemograficos().getDistrito() != null) {
            ubigeoNombres = ubigeoService.obtenerNombresUbigeo(lead.getDemograficos().getDistrito());
        }

        LeadResponse response = LeadMapper.toResponse(lead, ubigeoNombres);

        // Cargar historial
        var historial = historialRepository.findByLeadIdOrderByFechaCambioDesc(id);
        response.setHistorial(historial.stream()
                .map(LeadMapper::toHistorialDTO)
                .collect(Collectors.toList()));

        // Envolvemos la respuesta para que el Frontend reciba { status: "OK", data: {
        // ... } }
        return ResponseUtils.success(response, "Lead encontrado exitosamente");
    }

    // --- ENDPOINT 3: CAMBIAR ESTADO ---
    @PatchMapping("/{id}/estado")
    public ResponseEntity<Map<String, Object>> cambiarEstado(
            @PathVariable Long id,
            @RequestBody @Valid CambioEstadoRequest request) {

        // Pasamos el motivo al servicio
        managementService.cualificarLead(id, request.getNuevoEstado(), request.getMotivo());

        return ResponseUtils.success(null, "Estado actualizado correctamente");
    }

    // --- ENDPOINT 4: CAMBIAR ESTADO EN LOTE ---
    @PatchMapping("/batch/estado")
    public ResponseEntity<Map<String, Object>> cambiarEstadoEnLote(
            @RequestBody @Valid CambioEstadoLoteRequest request) {

        int actualizados = managementService.cualificarLeadsEnLote(
                request.getIds(),
                request.getNuevoEstado(),
                request.getMotivo());

        return ResponseUtils.success(
                Map.of("actualizados", actualizados, "total", request.getIds().size()),
                actualizados + " lead(s) actualizado(s) correctamente");
    }

    // --- ENDPOINT 5: ELIMINAR LEAD ---
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarLead(@PathVariable Long id) {
        managementService.eliminarLead(id);
        return ResponseEntity.noContent().build();
    }

    // --- ENDPOINT 6: ELIMINAR LEADS EN LOTE ---
    @DeleteMapping("/batch")
    public ResponseEntity<Map<String, Object>> eliminarLeadsEnLote(@RequestBody Map<String, List<Long>> request) {
        List<Long> ids = request.get("ids");
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "La lista de IDs no puede estar vacía"));
        }

        int eliminados = managementService.eliminarLeadsEnLote(ids);

        return ResponseUtils.success(
                Map.of("eliminados", eliminados, "total", ids.size()),
                eliminados + " lead(s) eliminado(s) correctamente");
    }

    // --- ENDPOINT 7: EXPORTAR TODOS LOS LEADS ---
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportarTodosLosLeads(
            @RequestParam(required = false) EstadoLead estado,
            @RequestParam(required = false) TipoFuente fuenteTipo,
            @RequestParam(required = false) String search) throws IOException {

        List<Lead> leads = leadRepository.buscarLeadsParaExportacion(estado, fuenteTipo, search);
        byte[] excelBytes = exportService.exportLeadsToExcel(leads);
        String filename = "leads_" + java.time.LocalDate.now() + ".xlsx";

        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .body(excelBytes);
    }

    // --- ENDPOINT 8: EXPORTAR LEADS SELECCIONADOS ---
    @PostMapping("/export/selected")
    public ResponseEntity<byte[]> exportarLeadsSeleccionados(@RequestBody Map<String, List<Long>> request)
            throws IOException {
        List<Long> ids = request.get("ids");
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<Lead> leads = leadRepository.findAllById(ids);
        byte[] excelBytes = exportService.exportLeadsToExcel(leads);
        String filename = "leads_selected_" + java.time.LocalDate.now() + ".xlsx";

        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .body(excelBytes);
    }
}