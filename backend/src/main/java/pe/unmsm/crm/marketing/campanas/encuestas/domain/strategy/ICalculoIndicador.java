package pe.unmsm.crm.marketing.campanas.encuestas.domain.strategy;

import pe.unmsm.crm.marketing.campanas.encuestas.domain.model.Pregunta;
import pe.unmsm.crm.marketing.campanas.encuestas.domain.model.Respuesta_Detalle;

import java.util.List;

public interface ICalculoIndicador {
    AnalisisResultadoDto calcular(Pregunta pregunta, List<Respuesta_Detalle> respuestas);
}
