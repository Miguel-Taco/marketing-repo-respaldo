package pe.unmsm.crm.marketing.shared.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Servicio para procesar plantillas HTML con datos dinámicos.
 * Permite cargar plantillas desde resources y reemplazar placeholders con
 * valores reales.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HtmlTemplateService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Carga una plantilla HTML desde resources y la procesa con datos dinámicos.
     * Los placeholders en la plantilla siguen el formato {{clave}}
     * 
     * @param templateName nombre del archivo de plantilla (ej:
     *                     "report-template.html")
     * @param data         mapa con datos para reemplazar en la plantilla
     * @return HTML procesado listo para generar PDF
     * @throws IOException si no se puede leer la plantilla
     */
    public String processTemplate(String templateName, Map<String, Object> data) throws IOException {
        // Cargar plantilla desde resources/templates/pdf/
        ClassPathResource resource = new ClassPathResource("templates/pdf/" + templateName);
        String template = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        // Parsear HTML con Jsoup para manipulación segura
        Document doc = Jsoup.parse(template);

        // Reemplazar placeholders
        replacePlaceholders(doc, data);

        // Retornar HTML válido
        return doc.html();
    }

    /**
     * Procesa una plantilla HTML directamente desde un String (sin cargar desde
     * resources).
     * 
     * @param templateContent contenido HTML de la plantilla
     * @param data            mapa con datos para reemplazar
     * @return HTML procesado
     */
    public String processTemplateFromString(String templateContent, Map<String, Object> data) {
        Document doc = Jsoup.parse(templateContent);
        replacePlaceholders(doc, data);
        return doc.html();
    }

    /**
     * Reemplaza placeholders en el HTML con valores del mapa de datos.
     * Los placeholders siguen el formato {{clave}}
     */
    private void replacePlaceholders(Document doc, Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = formatValue(entry.getValue());

            // Reemplazar en todo el documento
            String html = doc.html();
            html = html.replace(placeholder, value);
            doc.html(html);
        }
    }

    /**
     * Formatea un valor según su tipo para mostrarlo en el HTML.
     */
    private String formatValue(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof LocalDate date) {
            return DATE_FORMATTER.format(date);
        }
        if (value instanceof java.time.LocalDateTime dateTime) {
            return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }
        return value.toString();
    }
}
