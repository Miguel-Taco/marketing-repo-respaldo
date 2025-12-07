package pe.unmsm.crm.marketing.segmentacion.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.unmsm.crm.marketing.segmentacion.application.dto.MarketingClienteDTO;
import pe.unmsm.crm.marketing.segmentacion.infra.adapter.RestClienteAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador temporal para probar la integración con la API de Clientes
 * ELIMINAR después de verificar que funciona
 */
@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class TestClienteIntegrationController {

    private final RestClienteAdapter clienteAdapter;

    /**
     * Endpoint de prueba para verificar que la API de Clientes funciona
     * GET http://localhost:8080/api/v1/test/clientes
     */
    @GetMapping("/clientes")
    public ResponseEntity<Map<String, Object>> testClientesFetch() {
        try {
            // Esto internamente llama a fetchAllClientes() que hace la paginación
            List<MarketingClienteDTO> clientes = clienteAdapter.findClientesBySegmento(null)
                    .stream()
                    .limit(100) // Limitar a 100 para no sobrecargar la respuesta
                    .map(id -> {
                        // Aquí solo devolvemos los IDs, pero podrías devolver objetos completos
                        MarketingClienteDTO dto = new MarketingClienteDTO();
                        dto.setClienteId(id);
                        return dto;
                    })
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalClientesCargados", clientes.size());
            response.put("mensaje", "API de Clientes funcionando correctamente");
            response.put("primerosClientes", clientes.stream().limit(10).toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            error.put("mensaje", "Error al conectar con la API de Clientes");
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Endpoint para ver un cliente de ejemplo con todos sus campos
     * GET http://localhost:8080/api/v1/test/clientes/sample
     */
    @GetMapping("/clientes/sample")
    public ResponseEntity<Map<String, Object>> getSampleCliente() {
        try {
            // Forzar la carga para obtener al menos un cliente
            java.lang.reflect.Method method = RestClienteAdapter.class.getDeclaredMethod("fetchAllClientes");
            method.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<MarketingClienteDTO> clientes = (List<MarketingClienteDTO>) method.invoke(clienteAdapter);

            Map<String, Object> response = new HashMap<>();
            if (clientes != null && !clientes.isEmpty()) {
                response.put("success", true);
                response.put("totalClientes", clientes.size());
                response.put("clienteEjemplo", clientes.get(0));
            } else {
                response.put("success", false);
                response.put("mensaje", "No se encontraron clientes");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}
