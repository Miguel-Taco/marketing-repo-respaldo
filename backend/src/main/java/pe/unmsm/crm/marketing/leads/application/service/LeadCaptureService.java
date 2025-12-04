package pe.unmsm.crm.marketing.leads.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.unmsm.crm.marketing.leads.domain.model.staging.EnvioFormulario;
import pe.unmsm.crm.marketing.leads.domain.repository.StagingRepository;
import pe.unmsm.crm.marketing.shared.logging.AuditoriaService;
import pe.unmsm.crm.marketing.shared.logging.ModuloLog;
import pe.unmsm.crm.marketing.shared.logging.AccionLog;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class LeadCaptureService {

    private final StagingRepository stagingRepository;
    private final AuditoriaService auditoriaService;

    @Transactional
    public EnvioFormulario guardarEnvioWeb(Map<String, String> payload) {
        // 1. Crear la entidad de Staging
        EnvioFormulario envio = new EnvioFormulario();
        envio.setRespuestas(payload);

        // Opcional: Podrías capturar IP o UserAgent si vinieran en el controller
        // envio.setIp(ip);

        // 2. Guardar en BD (Tabla envios_formulario)
        EnvioFormulario saved = stagingRepository.save(envio);

        // AUDITORÍA: Registrar captura de lead desde web
        auditoriaService.registrarEvento(
                ModuloLog.LEADS,
                AccionLog.CREAR,
                saved.getId(),
                null, // No hay usuario autenticado en captura web
                String.format("Lead capturado desde formulario web. Email: %s",
                        payload.getOrDefault("email", "No especificado")));

        return saved;
    }
}
