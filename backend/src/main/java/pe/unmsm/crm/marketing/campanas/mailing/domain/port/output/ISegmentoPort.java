package pe.unmsm.crm.marketing.campanas.mailing.domain.port.output;

public interface ISegmentoPort {
    List<String> obtenerEmailsSegmento(Long idSegmento);
    Integer contarMiembros(Long idSegmento);
}