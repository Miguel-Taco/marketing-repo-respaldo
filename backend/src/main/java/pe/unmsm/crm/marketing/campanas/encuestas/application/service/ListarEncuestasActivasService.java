package pe.unmsm.crm.marketing.campanas.encuestas.application.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.unmsm.crm.marketing.campanas.encuestas.api.dto.EncuestaDisponibleDto;
import pe.unmsm.crm.marketing.campanas.encuestas.domain.repository.EncuestaRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio especializado para listar encuestas activas disponibles.
 * Responsabilidad única: obtener encuestas en estado ACTIVA con información
 * mínima.
 */
@Service
public class ListarEncuestasActivasService {

    @Autowired
    private EncuestaRepository encuestaRepository;

    /**
     * Obtiene todas las encuestas en estado ACTIVA.
     * Retorna solo el ID y título para optimizar la respuesta.
     * 
     * @return Lista de EncuestaDisponibleDto con id_encuesta y titulo
     */
    public List<EncuestaDisponibleDto> obtenerEncuestasActivas() {
        List<Object[]> results = encuestaRepository.findActiveSurveys();
        return results.stream()
                .map(result -> new EncuestaDisponibleDto(
                        (Integer) result[0], // idEncuesta
                        (String) result[1] // titulo
                ))
                .collect(Collectors.toList());
    }
}
