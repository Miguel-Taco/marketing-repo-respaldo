package pe.unmsm.crm.marketing.campanas.mailing.domain.port.output;

import java.util.List;

public interface ISegmentoPort {
    List<String> obtenerEmailsSegmento(Long idSegmento);
    Integer contarMiembros(Long idSegmento);
}