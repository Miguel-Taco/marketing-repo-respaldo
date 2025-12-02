package pe.unmsm.crm.marketing.campanas.telefonicas.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.unmsm.crm.marketing.campanas.telefonicas.api.dto.EnvioEncuestaDTO;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.entity.EnvioEncuestaEntity;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.repository.EnvioEncuestaRepository;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EncuestaLlamadaService {

    private final EnvioEncuestaRepository envioEncuestaRepository;

    /**
     * Procesa el envío de encuesta (simulado - no envía SMS real)
     */
    @Transactional
    public EnvioEncuestaEntity procesarEnvioEncuesta(
            Integer idLlamada,
            Integer idEncuesta,
            Long idLead,
            String telefono) {
        log.info("Procesando envío de encuesta simulado - Llamada: {}, Encuesta: {}, Lead: {}",
                idLlamada, idEncuesta, idLead);

        // Generar URL única de encuesta
        String urlEncuesta = generarUrlEncuesta(idEncuesta, idLead, idLlamada);

        // Crear registro de envío
        EnvioEncuestaEntity envio = new EnvioEncuestaEntity();
        envio.setIdLlamada(idLlamada);
        envio.setIdEncuesta(idEncuesta);
        envio.setIdLead(idLead);
        envio.setTelefonoDestino(telefono);
        envio.setUrlEncuesta(urlEncuesta);
        envio.setFechaEnvio(LocalDateTime.now());
        envio.setEstado(EnvioEncuestaEntity.EstadoEnvio.ENVIADA); // Simulado: marcar como enviada inmediatamente
        envio.setMetodoComunicacion(EnvioEncuestaEntity.MetodoComunicacion.SMS);

        EnvioEncuestaEntity saved = envioEncuestaRepository.save(envio);

        log.info("Encuesta simulada 'enviada' exitosamente - ID: {}, URL: {}", saved.getId(), urlEncuesta);

        return saved;
    }

    /**
     * Genera URL única de encuesta con token codificado
     */
    public String generarUrlEncuesta(Integer idEncuesta, Long idLead, Integer idLlamada) {
        // URL proporcionada por el desarrollador de encuestas
        return String.format("http://localhost:5600/q/%d/%d", idEncuesta, idLead);
    }

    /**
     * Obtiene los detalles de un envío de encuesta para mostrar en el modal
     */
    public EnvioEncuestaDTO obtenerDetalleEnvio(Integer idLlamada) {
        log.info("Obteniendo detalles de envío de encuesta para llamada: {}", idLlamada);

        return envioEncuestaRepository.findByIdLlamada(idLlamada)
                .map(this::toDTO)
                .orElse(null);
    }

    /**
     * Convierte EnvioEncuestaEntity a DTO
     */
    private EnvioEncuestaDTO toDTO(EnvioEncuestaEntity entity) {
        return EnvioEncuestaDTO.builder()
                .id(entity.getId())
                .idLlamada(entity.getIdLlamada())
                .idEncuesta(entity.getIdEncuesta())
                .idLead(entity.getIdLead())
                .telefonoDestino(entity.getTelefonoDestino())
                .urlEncuesta(entity.getUrlEncuesta())
                .fechaEnvio(entity.getFechaEnvio())
                .estado(entity.getEstado().name())
                .metodoComunicacion(entity.getMetodoComunicacion().name())
                .mensajeError(entity.getMensajeError())
                // Campos adicionales se pueden poblar con joins si es necesario
                .build();
    }
}
