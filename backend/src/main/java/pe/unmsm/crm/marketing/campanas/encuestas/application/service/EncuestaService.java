package pe.unmsm.crm.marketing.campanas.encuestas.application.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.unmsm.crm.marketing.campanas.encuestas.api.dto.CreateEncuestaDto;
import pe.unmsm.crm.marketing.campanas.encuestas.api.dto.EncuestaCompletaDto;
import pe.unmsm.crm.marketing.campanas.encuestas.api.dto.EncuestaDto;
import pe.unmsm.crm.marketing.campanas.encuestas.domain.builder.EncuestaBuilder;
import pe.unmsm.crm.marketing.campanas.encuestas.domain.model.Encuesta;
import pe.unmsm.crm.marketing.campanas.encuestas.domain.repository.EncuestaRepository;
import pe.unmsm.crm.marketing.shared.logging.AccionLog;
import pe.unmsm.crm.marketing.shared.logging.AuditoriaService;
import pe.unmsm.crm.marketing.shared.logging.ModuloLog;
import pe.unmsm.crm.marketing.security.service.UserAuthorizationService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EncuestaService {

        @Autowired
        private EncuestaRepository encuestaRepository;

        @Autowired
        private pe.unmsm.crm.marketing.campanas.encuestas.domain.repository.CampanaExternalRepository campanaRepository;

        @Autowired
        private AuditoriaService auditoriaService;

        @Autowired
        private UserAuthorizationService userAuthorizationService;

        @Transactional
        public void archivarEncuesta(Integer id) {
                Encuesta encuesta = encuestaRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Encuesta no encontrada con ID: " + id));

                List<pe.unmsm.crm.marketing.campanas.gestor.domain.model.Campana> campanas = campanaRepository
                                .findByIdEncuesta(id);

                if (!campanas.isEmpty()) {
                        boolean todasFinalizadas = campanas.stream()
                                        .allMatch(c -> "Finalizada".equals(c.getEstado().getNombre()));

                        if (!todasFinalizadas) {
                                throw new IllegalStateException(
                                                "No se puede archivar la encuesta porque tiene campañas asociadas que no están finalizadas.");
                        }
                }

                encuesta.setEstado(Encuesta.EstadoEncuesta.ARCHIVADA);
                encuesta.setEstado(Encuesta.EstadoEncuesta.ARCHIVADA);
                encuestaRepository.save(encuesta);

                // AUDITORÍA: Registrar cambio de estado
                auditoriaService.registrarEvento(
                                ModuloLog.ENCUESTAS,
                                AccionLog.CAMBIAR_ESTADO,
                                encuesta.getIdEncuesta().longValue(),
                                userAuthorizationService.requireCurrentUsuario().getIdUsuario(),
                                String.format("Encuesta archivada (ID: %d)", encuesta.getIdEncuesta()));
        }

        public List<pe.unmsm.crm.marketing.campanas.gestor.domain.model.Campana> listarCampanasAsociadas(Integer id) {
                return campanaRepository.findByIdEncuesta(id);
        }

        public List<EncuestaDto> obtenerTodasConEstadisticas() {
                List<Object[]> results = encuestaRepository.findAllWithResponseCount();
                return results.stream().map(result -> {
                        Encuesta encuesta = (Encuesta) result[0];
                        Long count = (Long) result[1];
                        return new EncuestaDto(
                                        encuesta.getIdEncuesta(),
                                        encuesta.getTitulo(),
                                        encuesta.getDescripcion(),
                                        encuesta.getEstado(),
                                        encuesta.getFechaModificacion(),
                                        count);
                }).collect(Collectors.toList());
        }

        @Transactional
        public EncuestaCompletaDto crearEncuesta(CreateEncuestaDto dto) {
                Encuesta encuesta = EncuestaBuilder.nueva()
                                .conTitulo(dto.getTitulo())
                                .conDescripcion(dto.getDescripcion())
                                .conEstado(dto.getEstado())
                                .conPreguntas(dto.getPreguntas())
                                .build();

                Encuesta saved = encuestaRepository.save(encuesta);

                // AUDITORÍA: Registrar creación
                auditoriaService.registrarEvento(
                                ModuloLog.ENCUESTAS,
                                AccionLog.CREAR,
                                saved.getIdEncuesta().longValue(),
                                userAuthorizationService.requireCurrentUsuario().getIdUsuario(),
                                String.format("Encuesta creada: '%s'", saved.getTitulo()));

                return convertirAEncuestaCompletaDto(saved);
        }

        public EncuestaCompletaDto obtenerPorId(Integer id) {
                Encuesta encuesta = encuestaRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Encuesta no encontrada con ID: " + id));
                return convertirAEncuestaCompletaDto(encuesta);
        }

        @Transactional
        public EncuestaCompletaDto actualizarEncuesta(Integer id, CreateEncuestaDto dto) {
                Encuesta encuesta = encuestaRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Encuesta no encontrada con ID: " + id));

                // Clear existing questions (orphanRemoval will delete them)
                encuesta.getPreguntas().clear();
                encuestaRepository.saveAndFlush(encuesta);

                // Rebuild the encuesta using the builder pattern
                encuesta.setTitulo(dto.getTitulo());
                encuesta.setDescripcion(dto.getDescripcion());
                encuesta.setEstado(dto.getEstado());

                // Use builder to reconstruct questions and options
                Encuesta encuestaReconstruida = EncuestaBuilder.nueva()
                                .conPreguntas(dto.getPreguntas())
                                .build();

                // Transfer the reconstructed questions to the existing entity
                encuesta.getPreguntas().addAll(encuestaReconstruida.getPreguntas());

                // Update the encuesta reference for each question
                encuesta.getPreguntas().forEach(pregunta -> pregunta.setEncuesta(encuesta));

                Encuesta saved = encuestaRepository.save(encuesta);

                // AUDITORÍA: Registrar actualización
                auditoriaService.registrarEvento(
                                ModuloLog.ENCUESTAS,
                                AccionLog.ACTUALIZAR,
                                saved.getIdEncuesta().longValue(),
                                userAuthorizationService.requireCurrentUsuario().getIdUsuario(),
                                String.format("Encuesta actualizada: '%s'", saved.getTitulo()));

                return convertirAEncuestaCompletaDto(saved);
        }

        /**
         * Convierte una entidad Encuesta a DTO para evitar problemas de serialización
         * JSON.
         */
        private EncuestaCompletaDto convertirAEncuestaCompletaDto(Encuesta encuesta) {
                List<EncuestaCompletaDto.PreguntaDto> preguntasDto = encuesta.getPreguntas().stream()
                                .map(pregunta -> {
                                        List<EncuestaCompletaDto.OpcionDto> opcionesDto = pregunta.getOpciones()
                                                        .stream()
                                                        .map(opcion -> new EncuestaCompletaDto.OpcionDto(
                                                                        opcion.getIdOpcion(),
                                                                        opcion.getTextoOpcion(),
                                                                        opcion.getOrden(),
                                                                        opcion.getEsAlertaUrgente()))
                                                        .collect(Collectors.toList());

                                        return new EncuestaCompletaDto.PreguntaDto(
                                                        pregunta.getIdPregunta(),
                                                        pregunta.getTextoPregunta(),
                                                        pregunta.getTipoPregunta(),
                                                        pregunta.getOrden(),
                                                        opcionesDto);
                                })
                                .collect(Collectors.toList());

                return new EncuestaCompletaDto(
                                encuesta.getIdEncuesta(),
                                encuesta.getTitulo(),
                                encuesta.getDescripcion(),
                                encuesta.getEstado(),
                                encuesta.getFechaCreacion(),
                                encuesta.getFechaModificacion(),
                                0L, // totalRespuestas - por ahora en 0
                                preguntasDto);
        }
}
