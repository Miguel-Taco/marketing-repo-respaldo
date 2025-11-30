package pe.unmsm.crm.marketing.campanas.telefonicas.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.unmsm.crm.marketing.campanas.telefonicas.api.dto.CreateGuionRequest;
import pe.unmsm.crm.marketing.campanas.telefonicas.api.dto.GuionDTO;
import pe.unmsm.crm.marketing.campanas.telefonicas.api.dto.SeccionGuionDTO;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.entity.GuionEntity;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.entity.GuionSeccionEntity;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.repository.GuionRepository;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.mapper.CampaignMapper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar guiones estructurados.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GuionService {

    private final GuionRepository guionRepository;
    private final CampaignMapper mapper;

    /**
     * Crea un guión estructurado con secciones.
     */
    @Transactional
    public GuionDTO crearGuion(CreateGuionRequest request) {
        log.info("Creando guión: {}", request.getNombre());

        // Crear entidad de guión
        GuionEntity guion = new GuionEntity();
        guion.setIdCampania(request.getIdCampania()); // Set campaign ID if provided
        guion.setNombre(request.getNombre());
        guion.setObjetivo(request.getObjetivo());
        guion.setTipo(request.getTipo());
        guion.setNotasInternas(request.getNotasInternas());
        guion.setActivo(true);

        // Crear secciones
        if (request.getSecciones() != null) {
            for (SeccionGuionDTO seccionDTO : request.getSecciones()) {
                GuionSeccionEntity seccion = new GuionSeccionEntity();
                seccion.setGuion(guion);
                seccion.setTipoSeccion(seccionDTO.getTipoSeccion());
                seccion.setContenido(seccionDTO.getContenido());
                seccion.setOrden(seccionDTO.getOrden());
                guion.getSecciones().add(seccion);
            }
        }

        // Guardar
        GuionEntity savedGuion = guionRepository.save(guion);
        log.info("Guión creado con ID: {}", savedGuion.getId());

        return mapper.toGuionDTO(savedGuion);
    }

    /**
     * Actualiza un guión existente.
     */
    @Transactional
    public GuionDTO actualizarGuion(Integer id, CreateGuionRequest request) {
        log.info("Actualizando guión ID: {}", id);

        GuionEntity guion = guionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Guión no encontrado: " + id));

        // Actualizar metadata
        guion.setNombre(request.getNombre());
        guion.setObjetivo(request.getObjetivo());
        guion.setTipo(request.getTipo());
        guion.setNotasInternas(request.getNotasInternas());

        // Actualizar secciones
        // Limpiamos las existentes y agregamos las nuevas
        guion.getSecciones().clear();

        if (request.getSecciones() != null) {
            for (SeccionGuionDTO seccionDTO : request.getSecciones()) {
                GuionSeccionEntity seccion = new GuionSeccionEntity();
                seccion.setGuion(guion);
                seccion.setTipoSeccion(seccionDTO.getTipoSeccion());
                seccion.setContenido(seccionDTO.getContenido());
                seccion.setOrden(seccionDTO.getOrden());
                guion.getSecciones().add(seccion);
            }
        }

        GuionEntity savedGuion = guionRepository.save(guion);
        return mapper.toGuionDTO(savedGuion);
    }

    /**
     * Obtiene un guión por ID.
     */
    @Transactional(readOnly = true)
    public GuionDTO obtenerGuionPorId(Integer id) {
        log.info("Obteniendo guión por ID: {}", id);
        return guionRepository.findById(id)
                .map(mapper::toGuionDTO)
                .orElseThrow(() -> new RuntimeException("Guión no encontrado: " + id));
    }

    /**
     * Obtiene la entidad del guión por ID.
     */
    @Transactional(readOnly = true)
    public GuionEntity obtenerEntidadGuion(Integer id) {
        return guionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Guión no encontrado: " + id));
    }

    /**
     * Lista todos los guiones activos.
     */
    @Transactional(readOnly = true)
    public List<GuionDTO> listarGuiones() {
        log.info("Listando todos los guiones");
        return guionRepository.findAll().stream()
                .filter(GuionEntity::getActivo)
                .map(mapper::toGuionDTO)
                .collect(Collectors.toList());
    }

    /**
     * Genera contenido Markdown a partir de las secciones del guión.
     */
    public String generarMarkdown(CreateGuionRequest request) {
        StringBuilder markdown = new StringBuilder();

        // Encabezado
        markdown.append("# ").append(request.getNombre()).append("\n\n");

        // Metadata
        markdown.append("## Información del Guión\n\n");
        markdown.append("**Objetivo:** ").append(request.getObjetivo()).append("\n\n");
        markdown.append("**Tipo:** ").append(request.getTipo()).append("\n\n");

        if (request.getNotasInternas() != null && !request.getNotasInternas().isEmpty()) {
            markdown.append("**Notas Internas:** ").append(request.getNotasInternas()).append("\n\n");
        }

        markdown.append("---\n\n");

        // Secciones
        if (request.getSecciones() != null) {
            for (SeccionGuionDTO seccion : request.getSecciones()) {
                String titulo = obtenerTituloSeccion(seccion.getTipoSeccion());
                markdown.append("## ").append(titulo).append("\n\n");
                markdown.append(seccion.getContenido()).append("\n\n");
            }
        }

        return markdown.toString();
    }

    /**
     * Genera Markdown desde una entidad GuionEntity.
     */
    public String generarMarkdown(GuionEntity guion) {
        StringBuilder markdown = new StringBuilder();

        // Encabezado
        markdown.append("# ").append(guion.getNombre()).append("\n\n");

        // Metadata
        markdown.append("## Información del Guión\n\n");
        markdown.append("**Objetivo:** ").append(guion.getObjetivo() != null ? guion.getObjetivo() : "").append("\n\n");
        markdown.append("**Tipo:** ").append(guion.getTipo() != null ? guion.getTipo() : "").append("\n\n");

        if (guion.getNotasInternas() != null && !guion.getNotasInternas().isEmpty()) {
            markdown.append("**Notas Internas:** ").append(guion.getNotasInternas()).append("\n\n");
        }

        markdown.append("---\n\n");

        // Secciones
        if (guion.getSecciones() != null && !guion.getSecciones().isEmpty()) {
            for (GuionSeccionEntity seccion : guion.getSecciones()) {
                String titulo = obtenerTituloSeccion(seccion.getTipoSeccion());
                markdown.append("## ").append(titulo).append("\n\n");
                markdown.append(seccion.getContenido() != null ? seccion.getContenido() : "").append("\n\n");
            }
        }

        return markdown.toString();
    }

    /**
     * Obtiene el título legible para un tipo de sección.
     */
    private String obtenerTituloSeccion(String tipoSeccion) {
        if (tipoSeccion == null)
            return "Sección";

        switch (tipoSeccion.toUpperCase()) {
            case "INTRO":
                return "Introducción / Saludo";
            case "DIAGNOSTICO":
                return "Preguntas de Diagnóstico";
            case "OBJECIONES":
                return "Manejo de Objeciones";
            case "CIERRE":
                return "Cierre / Call to Action";
            case "POST_LLAMADA":
                return "Pasos Post-Llamada";
            default:
                return tipoSeccion;
        }
    }
}
