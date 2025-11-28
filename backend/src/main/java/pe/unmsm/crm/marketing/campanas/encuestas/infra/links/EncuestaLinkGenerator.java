package pe.unmsm.crm.marketing.campanas.encuestas.infra.links;

import org.springframework.stereotype.Component;

@Component
public class EncuestaLinkGenerator {
    public String generateLink(String encuestaId) {
        return "https://crm.unmsm.pe/encuestas/" + encuestaId;
    }
}
