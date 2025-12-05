package pe.unmsm.crm.marketing.campanas.telefonicas.domain;

import pe.unmsm.crm.marketing.campanas.telefonicas.api.dto.*;

import java.util.List;

/**
 * Interfaz para abstrac la fuente de datos de campanas telefónicas.
 * Permite cambiar fácilmente entre datos mock y datos reales de API externa.
 */
public interface CampaignDataProvider {

    // Campañas
    List<CampaniaTelefonicaDTO> obtenerCampaniasPorAgente(Long idAgente);

    List<CampaniaTelefonicaDTO> obtenerTodasLasCampanias();

    CampaniaTelefonicaDTO obtenerCampaniaPorId(Long id);

    CampaniaTelefonicaDTO crearCampania(CreateCampaniaTelefonicaRequest request);

    // Contactos
    List<ContactoDTO> obtenerContactosDeCampania(Long idCampania);

    ContactoDTO obtenerContactoPorId(Long idContacto);

    // Cola
    List<ContactoDTO> obtenerCola(Long idCampania);

    ContactoDTO obtenerSiguienteContacto(Long idCampania, Long idAgente);

    ContactoDTO tomarContacto(Long idCampania, Long idContacto, Long idAgente);

    void pausarCola(Long idAgente, Long idCampania);

    void reanudarCola(Long idAgente, Long idCampania);

    List<ContactoDTO> obtenerLlamadasProgramadas(Long idAgente);

    // Llamadas
    LlamadaDTO obtenerLlamada(Long idLlamada);

    LlamadaDTO registrarResultadoLlamada(Long idCampania, Long idAgente, ResultadoLlamadaRequest request);

    List<LlamadaDTO> obtenerHistorialLlamadas(Long idCampania, Long idAgente);

    // Guiones
    GuionDTO obtenerGuionDeCampania(Long idCampania);

    List<GuionDTO> listarTodosLosGuiones();

    // Métricas
    MetricasAgenteDTO obtenerMetricasAgente(Long idCampania, Long idAgente);

    MetricasDiariasDTO obtenerMetricasDiarias(Long idCampania, Long idAgente);

    MetricasCampaniaDTO obtenerMetricasCampania(Long idCampania, Integer dias);

    // Contactos urgentes (integración con gestor de encuestas)
    /**
     * Agrega un contacto urgente a la cola con prioridad ALTA.
     * La campaña se determina a partir del id_encuesta.
     * 
     * @param request Solicitud con idLead e idEncuesta
     * @return ContactoDTO del lead agregado a la cola
     * @throws IllegalArgumentException si no existe campaña para la encuesta
     */
    ContactoDTO agregarContactoUrgente(AddUrgentContactRequest request);
}
