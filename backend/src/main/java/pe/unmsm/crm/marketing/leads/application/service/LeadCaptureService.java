package pe.unmsm.crm.marketing.leads.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.unmsm.crm.marketing.leads.domain.model.staging.EnvioFormulario;
import pe.unmsm.crm.marketing.leads.domain.repository.StagingRepository;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class LeadCaptureService {

    private final StagingRepository stagingRepository;

    @Transactional
    public EnvioFormulario guardarEnvioWeb(Map<String, String> payload) {
        // 1. Crear la entidad de Staging
        EnvioFormulario envio = new EnvioFormulario();
        envio.setRespuestas(payload);

        // Opcional: Podrías capturar IP o UserAgent si vinieran en el controller
        // envio.setIp(ip);

        // 2. Guardar en BD (Tabla envios_formulario)
        return stagingRepository.save(envio);
    }
}
