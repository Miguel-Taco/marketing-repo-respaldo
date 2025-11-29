package pe.unmsm.crm.marketing.campanas.mailing.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.unmsm.crm.marketing.campanas.mailing.domain.model.CampanaMailing;
import pe.unmsm.crm.marketing.shared.infra.exception.ValidationException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ValidacionMailingService {

    /**
     * Valida que todos los campos de contenido estén completos
     */
    public void validarContenidoCompleto(CampanaMailing campaña) {
        StringBuilder errores = new StringBuilder();
        
        if (campaña.getAsunto() == null || campaña.getAsunto().trim().isEmpty()) {
            errores.append("- Asunto es obligatorio\n");
        }
        
        if (campaña.getCuerpo() == null || campaña.getCuerpo().trim().isEmpty()) {
            errores.append("- Cuerpo del correo es obligatorio\n");
        }
        
        if (campaña.getCtaTexto() == null || campaña.getCtaTexto().trim().isEmpty()) {
            errores.append("- Texto del botón CTA es obligatorio\n");
        }
        
        if (errores.length() > 0) {
            throw new ValidationException(errores.toString());
        }
    }

    /**
     * Valida que la campaña pueda ser marcada como LISTO
     */
    public void validarParaMarcarListo(CampanaMailing campaña) {
        // Debe estar en estado PENDIENTE (1)
        if (!campaña.getIdEstado().equals(1)) {
            throw new ValidationException("Solo campañas en estado PENDIENTE pueden marcarse como LISTO");
        }
        
        // Validar contenido completo
        validarContenidoCompleto(campaña);
    }

    /**
     * Valida que la campaña pueda ser enviada
     */
    public void validarParaEnviar(CampanaMailing campaña) {
        // Debe estar en estado LISTO (2)
        if (!campaña.getIdEstado().equals(2)) {
            throw new ValidationException("Solo campañas en estado LISTO pueden enviarse");
        }
        
        // Fecha inicio debe haber llegado
        if (LocalDateTime.now().isBefore(campaña.getFechaInicio())) {
            throw new ValidationException("La fecha de inicio aún no ha llegado");
        }
        
        // Fecha inicio debe ser antes de fecha fin
        if (campaña.getFechaInicio().isAfter(campaña.getFechaFin())) {
            throw new ValidationException("La fecha de inicio debe ser anterior a la fecha de fin");
        }
        
        // Contenido debe estar completo
        validarContenidoCompleto(campaña);
    }

    /**
     * Valida longitud y formato del asunto
     */
    public void validarAsunto(String asunto) {
        if (asunto == null || asunto.trim().isEmpty()) {
            throw new ValidationException("Asunto no puede estar vacío");
        }
        
        if (asunto.length() > 255) {
            throw new ValidationException("Asunto no puede exceder 255 caracteres");
        }
    }

    /**
     * Valida longitud del texto CTA
     */
    public void validarCtaTexto(String ctaTexto) {
        if (ctaTexto == null || ctaTexto.trim().isEmpty()) {
            throw new ValidationException("Texto CTA no puede estar vacío");
        }
        
        if (ctaTexto.length() > 100) {
            throw new ValidationException("Texto CTA no puede exceder 100 caracteres");
        }
    }

    /**
     * Valida que el cuerpo del email sea válido
     */
    public void validarCuerpo(String cuerpo) {
        if (cuerpo == null || cuerpo.trim().isEmpty()) {
            throw new ValidationException("Cuerpo del correo no puede estar vacío");
        }
        
        if (cuerpo.length() > 65535) {
            throw new ValidationException("Cuerpo del correo es demasiado largo");
        }
    }
}