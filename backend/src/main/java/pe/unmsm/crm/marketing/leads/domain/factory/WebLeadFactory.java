package pe.unmsm.crm.marketing.leads.domain.factory;

import org.springframework.stereotype.Component;
import java.util.Map;

import pe.unmsm.crm.marketing.leads.domain.model.Lead;
import pe.unmsm.crm.marketing.leads.domain.model.LeadWeb;
import pe.unmsm.crm.marketing.leads.domain.model.staging.EnvioFormulario;
import pe.unmsm.crm.marketing.leads.domain.enums.TipoFuente;
import pe.unmsm.crm.marketing.leads.domain.enums.EstadoLead;
import pe.unmsm.crm.marketing.leads.domain.vo.DatosContacto;
import pe.unmsm.crm.marketing.leads.domain.vo.DatosDemograficos;
import pe.unmsm.crm.marketing.leads.domain.vo.TrackingUTM;
import pe.unmsm.crm.marketing.shared.domain.model.Distrito;
import pe.unmsm.crm.marketing.shared.domain.repository.DistritoRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WebLeadFactory implements LeadFactory {

    private final DistritoRepository distritoRepository;

    @Override
    public Lead convertirALead(Object origenStaging) {
        if (!(origenStaging instanceof EnvioFormulario)) {
            throw new IllegalArgumentException("El objeto origen no es un EnvioFormulario");
        }
        EnvioFormulario envio = (EnvioFormulario) origenStaging;
        Map<String, String> respuestas = envio.getRespuestas();

        LeadWeb lead = new LeadWeb();
        lead.setNombre(respuestas.get("nombre_completo"));
        lead.setIdReferenciaOrigen(envio.getId());

        // --- VINCULACIÓN CON STAGING PARA AUDITORÍA ---
        // Guardamos el ID del formulario original para trazabilidad
        lead.setEnvioFormularioId(envio.getId());
        // -----------------------------------------------

        lead.setEstado(EstadoLead.NUEVO);

        // Distrito: buscar entidad desde el ID
        String distritoId = respuestas.get("distrito_id");
        Distrito distrito = null;
        if (distritoId != null && !distritoId.isBlank()) {
            distrito = distritoRepository.findById(distritoId).orElse(null);
        }

        DatosContacto contacto = new DatosContacto(
                respuestas.get("email"),
                respuestas.get("telefono"));
        lead.setContacto(contacto);

        if (respuestas.containsKey("edad") || respuestas.containsKey("dni") || distrito != null) {
            Integer edad = parseIntSafe(respuestas.get("edad"));
            lead.setDemograficos(new DatosDemograficos(edad, null, distrito));
        }

        TrackingUTM tracking = new TrackingUTM(
                "INTERNO",
                "FORMULARIO_INTRANET",
                "GESTION_OPERATIVA");

        if (respuestas.containsKey("campana_id")) {
            tracking = new TrackingUTM("INTERNO", "FORMULARIO_INTRANET", respuestas.get("campana_id"));
        }

        lead.setTracking(tracking);

        return lead;
    }

    @Override
    public boolean soporta(TipoFuente tipo) {
        return TipoFuente.WEB.equals(tipo);
    }

    private Integer parseIntSafe(String value) {
        try {
            return value != null ? Integer.parseInt(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}