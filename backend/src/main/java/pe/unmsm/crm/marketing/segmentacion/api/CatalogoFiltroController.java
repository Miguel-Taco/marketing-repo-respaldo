package pe.unmsm.crm.marketing.segmentacion.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.unmsm.crm.marketing.segmentacion.infra.persistence.JpaCatalogoFiltroEntity;
import pe.unmsm.crm.marketing.segmentacion.infra.persistence.JpaCatalogoFiltroRepository;

import java.util.Arrays;
import java.util.List;

/**
 * Controlador para el catálogo de filtros de segmentación
 */
@RestController
@RequestMapping("/api/v1/catalogo-filtros")
@RequiredArgsConstructor
public class CatalogoFiltroController {

    private final JpaCatalogoFiltroRepository catalogoRepository;

    /**
     * Obtiene todos los filtros disponibles
     */
    @GetMapping
    public ResponseEntity<List<JpaCatalogoFiltroEntity>> obtenerTodos() {
        return ResponseEntity.ok(catalogoRepository.findAll());
    }

    /**
     * Obtiene filtros disponibles para un tipo de audiencia específico
     * 
     * @param tipoAudiencia LEAD, CLIENTE, o MIXTO
     */
    @GetMapping("/por-audiencia")
    public ResponseEntity<List<JpaCatalogoFiltroEntity>> obtenerPorAudiencia(
            @RequestParam String tipoAudiencia) {

        List<String> tiposPermitidos;
        if ("MIXTO".equals(tipoAudiencia)) {
            // Para MIXTO, mostrar todos los filtros
            tiposPermitidos = Arrays.asList("LEAD", "CLIENTE", "AMBOS");
        } else {
            // Para LEAD o CLIENTE, mostrar los específicos + AMBOS
            tiposPermitidos = Arrays.asList(tipoAudiencia, "AMBOS");
        }

        List<JpaCatalogoFiltroEntity> filtros = catalogoRepository.findByTipoAudienciaIn(tiposPermitidos);
        return ResponseEntity.ok(filtros);
    }
}
