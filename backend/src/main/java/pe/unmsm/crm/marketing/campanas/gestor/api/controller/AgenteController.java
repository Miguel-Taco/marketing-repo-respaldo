package pe.unmsm.crm.marketing.campanas.gestor.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.unmsm.crm.marketing.campanas.gestor.api.dto.request.AgenteCreateRequest;
import pe.unmsm.crm.marketing.campanas.gestor.api.dto.response.AgenteResponse;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.Agente;
import pe.unmsm.crm.marketing.campanas.gestor.domain.port.output.AgenteRepositoryPort;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/campanas/agentes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AgenteController {

    private final AgenteRepositoryPort agenteRepository;

    @GetMapping
    public ResponseEntity<List<AgenteResponse>> listarAgentesActivos() {
        List<Agente> agentes = agenteRepository.findAllActive();

        List<AgenteResponse> response = agentes.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Crea o actualiza un agente.
     * Si idAgente es proporcionado y existe, se actualiza; de lo contrario, se crea
     * uno nuevo.
     */
    @PostMapping
    public ResponseEntity<AgenteResponse> crearOActualizarAgente(@Valid @RequestBody AgenteCreateRequest request) {
        Agente agente = Agente.builder()
                .idAgente(request.getIdAgente())
                .nombre(request.getNombre())
                .email(request.getEmail())
                .telefono(request.getTelefono())
                .activo(request.getActivo() != null ? request.getActivo() : true)
                .build();

        Agente saved = agenteRepository.save(agente);

        HttpStatus status = (request.getIdAgente() != null
                && agenteRepository.findById(request.getIdAgente()).isPresent())
                        ? HttpStatus.OK
                        : HttpStatus.CREATED;

        return ResponseEntity.status(status).body(toResponse(saved));
    }

    private AgenteResponse toResponse(Agente agente) {
        return AgenteResponse.builder()
                .idAgente(agente.getIdAgente())
                .nombre(agente.getNombre())
                .email(agente.getEmail())
                .telefono(agente.getTelefono())
                .activo(agente.getActivo())
                .build();
    }
}
