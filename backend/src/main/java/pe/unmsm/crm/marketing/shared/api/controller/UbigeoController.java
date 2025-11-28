package pe.unmsm.crm.marketing.shared.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.unmsm.crm.marketing.shared.domain.repository.*;
import pe.unmsm.crm.marketing.shared.utils.ResponseUtils;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ubigeo")
@RequiredArgsConstructor
public class UbigeoController {

    private final DepartamentoRepository depRepo;
    private final ProvinciaRepository provRepo;
    private final DistritoRepository distRepo;

    @GetMapping("/departamentos")
    public ResponseEntity<Map<String, Object>> getDepartamentos() {
        return ResponseUtils.success(depRepo.findAll(), "Departamentos cargados");
    }

    @GetMapping("/provincias/{depId}")
    public ResponseEntity<Map<String, Object>> getProvincias(@PathVariable String depId) {
        return ResponseUtils.success(provRepo.findByDepartamentoId(depId), "Provincias cargadas");
    }

    @GetMapping("/distritos/{provId}")
    public ResponseEntity<Map<String, Object>> getDistritos(@PathVariable String provId) {
        return ResponseUtils.success(distRepo.findByProvinciaId(provId), "Distritos cargados");
    }
}
