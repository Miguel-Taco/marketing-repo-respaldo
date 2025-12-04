package pe.unmsm.crm.marketing.campanas.gestor.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.PlantillaCampana;
import pe.unmsm.crm.marketing.campanas.gestor.domain.port.input.IPlantillaUseCase;
import pe.unmsm.crm.marketing.campanas.gestor.domain.port.output.PlantillaRepositoryPort;
import pe.unmsm.crm.marketing.shared.infra.exception.NotFoundException;
import pe.unmsm.crm.marketing.shared.logging.AccionLog;
import pe.unmsm.crm.marketing.shared.logging.AuditoriaService;
import pe.unmsm.crm.marketing.shared.logging.ModuloLog;

import java.util.List;

/**
 * Servicio de aplicación para la gestión de plantillas de campanas.
 * Implementa el caso de uso IPlantillaUseCase.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PlantillaService implements IPlantillaUseCase {

    private final PlantillaRepositoryPort plantillaRepository;
    private final AuditoriaService auditoriaService;

    @Override
    public PlantillaCampana crear(PlantillaCampana plantilla) {
        log.debug("Creando plantilla: {}", plantilla.getNombre());
        PlantillaCampana saved = plantillaRepository.save(plantilla);

        // AUDITORÍA
        auditoriaService.registrarEvento(ModuloLog.CAMPANIAS_GESTOR, AccionLog.CREAR,
                saved.getIdPlantilla().longValue(), null,
                "Plantilla creada: " + saved.getNombre());

        log.info("Plantilla creada con ID: {}", saved.getIdPlantilla());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public PlantillaCampana obtenerPorId(Integer idPlantilla) {
        return plantillaRepository.findById(idPlantilla)
                .orElseThrow(() -> new NotFoundException("Plantilla", idPlantilla.longValue()));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PlantillaCampana> listar(String nombre, String canalEjecucion, int page, int size) {
        return plantillaRepository.findByFiltros(nombre, canalEjecucion,
                PageRequest.of(page, size, org.springframework.data.domain.Sort
                        .by(org.springframework.data.domain.Sort.Direction.DESC, "fechaCreacion")));
    }

    @Override
    public PlantillaCampana editar(Integer idPlantilla, PlantillaCampana datosActualizados) {
        PlantillaCampana existente = obtenerPorId(idPlantilla);

        // Actualizar campos
        if (datosActualizados.getNombre() != null) {
            existente.setNombre(datosActualizados.getNombre());
        }
        if (datosActualizados.getTematica() != null) {
            existente.setTematica(datosActualizados.getTematica());
        }
        if (datosActualizados.getDescripcion() != null) {
            existente.setDescripcion(datosActualizados.getDescripcion());
        }
        if (datosActualizados.getCanalEjecucion() != null) {
            existente.setCanalEjecucion(datosActualizados.getCanalEjecucion());
        }
        if (datosActualizados.getIdSegmento() != null) {
            existente.setIdSegmento(datosActualizados.getIdSegmento());
        }
        if (datosActualizados.getIdEncuesta() != null) {
            existente.setIdEncuesta(datosActualizados.getIdEncuesta());
        }

        PlantillaCampana updated = plantillaRepository.save(existente);

        // AUDITORÍA
        auditoriaService.registrarEvento(ModuloLog.CAMPANIAS_GESTOR, AccionLog.ACTUALIZAR, idPlantilla.longValue(),
                null,
                "Plantilla actualizada: " + existente.getNombre());

        log.info("Plantilla {} actualizada", idPlantilla);
        return updated;
    }

    @Override
    public void eliminar(Integer idPlantilla) {
        if (!plantillaRepository.existsById(idPlantilla)) {
            throw new NotFoundException("Plantilla", idPlantilla.longValue());
        }

        plantillaRepository.deleteById(idPlantilla);

        // AUDITORÍA
        auditoriaService.registrarEvento(ModuloLog.CAMPANIAS_GESTOR, AccionLog.ELIMINAR, idPlantilla.longValue(), null,
                "Plantilla eliminada");

        log.info("Plantilla {} eliminada", idPlantilla);
    }
}
