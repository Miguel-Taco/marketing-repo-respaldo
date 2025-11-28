package pe.unmsm.crm.marketing.campanas.gestor.infra.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.unmsm.crm.marketing.campanas.gestor.domain.port.output.IConsultaRecursosPort;

import java.time.LocalDateTime;

/**
 * Adaptador que implementa la consulta de recursos externos
 * combinando los 3 clientes HTTP (Segmento, Agente, Encuesta).
 */
@Component
@RequiredArgsConstructor
public class ConsultaRecursosAdapter implements IConsultaRecursosPort {

    private final SegmentoClientAdapter segmentoClient;
    private final AgenteClientAdapter agenteClient;
    private final EncuestaClientAdapter encuestaClient;

    @Override
    public boolean existeSegmento(Long idSegmento) {
        return segmentoClient.existeSegmento(idSegmento);
    }

    @Override
    public boolean isAgenteDisponible(Integer idAgente, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return agenteClient.isAgenteDisponible(idAgente, fechaInicio, fechaFin);
    }

    @Override
    public boolean existeEncuesta(Integer idEncuesta) {
        return encuestaClient.existeEncuesta(idEncuesta);
    }
}
