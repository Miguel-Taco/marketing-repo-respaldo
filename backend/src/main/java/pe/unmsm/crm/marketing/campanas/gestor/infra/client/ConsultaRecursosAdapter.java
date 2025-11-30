package pe.unmsm.crm.marketing.campanas.gestor.infra.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.unmsm.crm.marketing.campanas.gestor.domain.port.output.IConsultaRecursosPort;
import pe.unmsm.crm.marketing.campanas.gestor.domain.port.output.AgenteRepositoryPort;

import java.time.LocalDateTime;

/**
 * Adaptador que implementa la consulta de recursos externos.
 * Actualmente usa los adapters de cada módulo, pero centraliza la lógica.
 */
@Component
@RequiredArgsConstructor
public class ConsultaRecursosAdapter implements IConsultaRecursosPort {

    private final SegmentoClientAdapter segmentoAdapter;
    private final EncuestaClientAdapter encuestaAdapter;
    private final AgenteClientAdapter agenteAdapter;
    private final AgenteRepositoryPort agenteRepository;

    @Override
    public boolean existeSegmento(Long idSegmento) {
        return segmentoAdapter.existeSegmento(idSegmento);
    }

    @Override
    public boolean existeEncuesta(Integer idEncuesta) {
        return encuestaAdapter.existeEncuesta(idEncuesta);
    }

    @Override
    public boolean isAgenteDisponible(Integer idAgente, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        // 1. Validar que el agente existe y está activo en BD
        if (!agenteRepository.existsAndActive(idAgente)) {
            return false;
        }

        // 2. Consultar disponibilidad de agenda (Placeholder por ahora)
        return agenteAdapter.isAgenteDisponible(idAgente, fechaInicio, fechaFin);
    }
}
