# Utilidad de Generación de Reportes PDF

Esta utilidad permite generar reportes en formato PDF a partir de plantillas HTML con datos dinámicos.

## 📋 Componentes

### Servicios

#### `PdfReportService`
- **Ubicación**: `pe.unmsm.crm.marketing.shared.services.PdfReportService`
- **Función**: Genera archivos PDF desde contenido HTML usando Flying Saucer
- **Métodos principales**:
  - `generatePdfFromHtml(String htmlContent)`: Convierte HTML a PDF
  - `generatePdfFromHtml(String htmlContent, String fileName)`: Convierte HTML a PDF con nombre sugerido

#### `HtmlTemplateService`
- **Ubicación**: `pe.unmsm.crm.marketing.shared.services.HtmlTemplateService`
- **Función**: Procesa plantillas HTML reemplazando placeholders con datos dinámicos
- **Métodos principales**:
  - `processTemplate(String templateName, Map<String, Object> data)`: Carga plantilla desde resources y procesa datos
  - `processTemplateFromString(String templateContent, Map<String, Object> data)`: Procesa plantilla desde String

### DTOs

#### `ReportDataDTO`
- **Ubicación**: `pe.unmsm.crm.marketing.shared.api.dto.ReportDataDTO`
- **Función**: DTO para encapsular datos de configuración del reporte
- **Campos**:
  - `reportTitle`: Título del reporte
  - `reportDescription`: Descripción o subtítulo
  - `generatedBy`: Usuario que generó el reporte
  - `generatedDate`: Fecha de generación
  - `startDate`: Fecha de inicio del rango
  - `endDate`: Fecha de fin del rango
  - `data`: Map con datos dinámicos

### Plantillas

#### `report-template.html`
- **Ubicación**: `src/main/resources/templates/pdf/report-template.html`
- **Función**: Plantilla HTML base flexible con CSS embebido
- **Placeholders soportados**:
  - `{{reportTitle}}`: Título del reporte
  - `{{reportDescription}}`: Descripción
  - `{{generatedBy}}`: Generado por
  - `{{generatedDate}}`: Fecha de generación
  - `{{startDate}}` / `{{endDate}}`: Rango de fechas
  - `{{criteriaItems}}`: HTML de criterios de búsqueda
  - `{{metricsCards}}`: HTML de tarjetas de métricas
  - `{{tableHeaders}}`: HTML de encabezados de tabla
  - `{{tableRows}}`: HTML de filas de tabla
  - `{{customContent}}`: Contenido personalizado adicional
  - `{{platformName}}`: Nombre de la plataforma
  - `{{pageNumber}}` / `{{totalPages}}`: Paginación

## 🚀 Uso Básico

### 1. Inyectar servicios en tu controlador/servicio

```java
@RestController
@RequiredArgsConstructor
public class MiControlador {
    
    private final PdfReportService pdfReportService;
    private final HtmlTemplateService htmlTemplateService;
    
    // ... tus endpoints
}
```

### 2. Preparar datos del reporte

```java
Map<String, Object> data = new HashMap<>();
data.put("reportTitle", "Mi Reporte");
data.put("reportDescription", "Descripción del reporte");
data.put("generatedBy", "Usuario XYZ");
data.put("generatedDate", LocalDate.now());
data.put("startDate", LocalDate.now().minusMonths(1));
data.put("endDate", LocalDate.now());

// Agregar datos específicos
data.put("criteriaItems", "<div>...</div>");
data.put("metricsCards", "<div>...</div>");
data.put("tableHeaders", "<th>...</th>");
data.put("tableRows", "<tr>...</tr>");
```

### 3. Generar PDF

```java
// Procesar plantilla
String html = htmlTemplateService.processTemplate("report-template.html", data);

// Generar PDF
byte[] pdfBytes = pdfReportService.generatePdfFromHtml(html);

// Configurar respuesta HTTP
HttpHeaders headers = new HttpHeaders();
headers.setContentType(MediaType.APPLICATION_PDF);
headers.setContentDispositionFormData("attachment", "mi-reporte.pdf");

return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
```

## 📝 Ejemplo Completo

Ver `ReportController.java` para ejemplos completos de uso:

### Endpoint de ejemplo genérico
```
GET /api/v1/reportes/ejemplo/pdf
```

### Endpoint de reporte de campaña
```
GET /api/v1/reportes/campanas/{idCampana}/pdf
```

## 🎨 Crear una Plantilla Personalizada

### 1. Crear archivo HTML en `src/main/resources/templates/pdf/`

```html
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8"/>
    <title>{{reportTitle}}</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            padding: 20px;
        }
        /* ... tus estilos CSS ... */
    </style>
</head>
<body>
    <h1>{{reportTitle}}</h1>
    <p>{{customField1}}</p>
    <!-- ... tu contenido ... -->
</body>
</html>
```

### 2. Usar la plantilla

```java
Map<String, Object> data = new HashMap<>();
data.put("reportTitle", "Mi Título");
data.put("customField1", "Mi valor personalizado");

String html = htmlTemplateService.processTemplate("mi-plantilla.html", data);
byte[] pdf = pdfReportService.generatePdfFromHtml(html);
```

## ⚙️ Consideraciones Técnicas

### Limitaciones de CSS

Flying Saucer soporta CSS 2.1 y algunas características de CSS 3. **NO soporta**:
- JavaScript
- Tailwind CDN o cualquier CSS externo que requiera procesamiento
- Flexbox avanzado (usa `display: table` para layouts complejos)
- Grid CSS moderno

### Estilos Recomendados

✅ **Usar**:
- CSS inline o embebido en `<style>`
- `display: table` / `table-cell` para layouts
- Colores hexadecimales (#RGB)
- Fuentes estándar o embebidas
- `page-break-inside: avoid` para control de paginación

❌ **Evitar**:
- Enlaces externos a CSS
- Tailwind CDN
- JavaScript
- Fuentes de Google Fonts (usar fuentes del sistema)

### Control de Paginación

```css
@page {
    size: A4;
    margin: 2cm;
}

.no-break {
    page-break-inside: avoid;
}

.page-break {
    page-break-after: always;
}
```

## 🔧 Integración con Supabase (Opcional)

Para guardar PDFs en Supabase Storage:

```java
@RequiredArgsConstructor
public class MiServicio {
    
    private final PdfReportService pdfReportService;
    private final HtmlTemplateService htmlTemplateService;
    private final SupabaseStorageService supabaseService;
    
    public String generarYGuardarReporte(Map<String, Object> data) throws IOException {
        // Generar PDF
        String html = htmlTemplateService.processTemplate("report-template.html", data);
        byte[] pdfBytes = pdfReportService.generatePdfFromHtml(html);
        
        // Guardar en Supabase
        String rutaArchivo = "reportes/reporte-" + System.currentTimeMillis() + ".pdf";
        supabaseService.uploadFile("mi-bucket", rutaArchivo, pdfBytes, "application/pdf");
        
        // Obtener URL pública
        return supabaseService.getPublicUrl("mi-bucket", rutaArchivo);
    }
}
```

## 🧪 Testing

### Test de generación de PDF

```java
@SpringBootTest
class PdfReportServiceTest {
    
    @Autowired
    private PdfReportService pdfReportService;
    
    @Test
    void testGeneratePdf() throws IOException {
        String html = "<html><body><h1>Test</h1></body></html>";
        byte[] pdf = pdfReportService.generatePdfFromHtml(html);
        
        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }
}
```

## 📚 Recursos Adicionales

- [Flying Saucer Documentation](https://github.com/flyingsaucerproject/flyingsaucer)
- [OpenPDF Documentation](https://github.com/LibrePDF/OpenPDF)
- [Jsoup Documentation](https://jsoup.org/)

## 🤝 Contribuir

Para agregar nuevas plantillas o mejorar la utilidad:

1. Crear plantilla HTML en `resources/templates/pdf/`
2. Documentar placeholders soportados
3. Crear endpoint de ejemplo
4. Actualizar este README
