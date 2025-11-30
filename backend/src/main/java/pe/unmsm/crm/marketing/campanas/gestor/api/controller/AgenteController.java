package pe.unmsm.crm.marketing.campanas.gestor.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.unmsm.crm.marketing.campanas.gestor.api.dto.response.AgenteResponse;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.Agente;
import pe.unmsm.crm.marketing.campanas.gestor.domain.port.output.AgenteRepositoryPort;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/campanas/agentes")
@RequiredArgsConstructor
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
