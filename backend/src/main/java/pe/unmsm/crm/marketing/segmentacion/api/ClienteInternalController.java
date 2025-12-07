package pe.unmsm.crm.marketing.segmentacion.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.unmsm.crm.marketing.segmentacion.application.ClienteServicePort;
import pe.unmsm.crm.marketing.segmentacion.application.dto.MarketingClienteDTO;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/internal/clientes")
@RequiredArgsConstructor
public class ClienteInternalController {

    private final ClienteServicePort clienteServicePort;

    @PostMapping("/batch")
    public ResponseEntity<List<ClientePreviewDTO>> getClientesBatch(@RequestBody List<Long> clienteIds) {
        List<MarketingClienteDTO> allClientes = clienteServicePort.getAllClientes();

        List<ClientePreviewDTO> clientes = allClientes.stream()
                .filter(c -> clienteIds.contains(c.getClienteId()))
                .map(c -> new ClientePreviewDTO(
                        c.getClienteId(),
                        c.getFullName(),
                        c.getEdad() != null ? c.getEdad() : 0,
                        c.getEmail(),
                        "No especificado"))
                .collect(Collectors.toList());

        return ResponseEntity.ok(clientes);
    }

    public record ClientePreviewDTO(
            Long id,
            String nombre,
            Integer edad,
            String correo,
            String telefono) {
    }
}
