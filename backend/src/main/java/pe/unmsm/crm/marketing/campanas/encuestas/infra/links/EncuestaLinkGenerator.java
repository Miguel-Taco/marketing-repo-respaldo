package pe.unmsm.crm.marketing.campanas.encuestas.infra.links;

import org.springframework.stereotype.Component;

@Component
public class EncuestaLinkGenerator {
    /**
     * Genera un enlace público para una encuesta.
     * La URL se genera on-demand y no se almacena en la base de datos.
     * 
     * @param idEncuesta ID de la encuesta (INT)
     * @return URL pública para acceder a la encuesta
     */
    public String generarEnlacePublico(int idEncuesta) {
        return "http://localhost:5600/q/" + idEncuesta;
    }
}
