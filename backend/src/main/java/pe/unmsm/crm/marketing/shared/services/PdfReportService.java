package pe.unmsm.crm.marketing.shared.services;

import com.lowagie.text.DocumentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Servicio para generar archivos PDF a partir de contenido HTML.
 * Utiliza Flying Saucer (xhtmlrenderer) con iText como backend.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PdfReportService {

    /**
     * Genera un PDF a partir de contenido HTML.
     * El HTML debe ser XHTML válido para que Flying Saucer pueda procesarlo
     * correctamente.
     * 
     * @param htmlContent HTML bien formado (XHTML válido)
     * @return byte array del PDF generado
     * @throws IOException si hay error al generar el PDF
     */
    public byte[] generatePdfFromHtml(String htmlContent) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();

            // Configurar el HTML
            renderer.setDocumentFromString(htmlContent);
            renderer.layout();

            // Generar PDF
            renderer.createPDF(outputStream);

            byte[] pdfBytes = outputStream.toByteArray();
            log.info("PDF generado exitosamente, tamaño: {} bytes", pdfBytes.length);
            return pdfBytes;

        } catch (DocumentException e) {
            log.error("Error al generar PDF: {}", e.getMessage());
            throw new IOException("Error al generar PDF", e);
        }
    }

    /**
     * Genera un PDF y opcionalmente lo guarda con un nombre específico.
     * 
     * @param htmlContent HTML bien formado
     * @param fileName    nombre sugerido para el archivo (sin extensión)
     * @return byte array del PDF generado
     * @throws IOException si hay error al generar el PDF
     */
    public byte[] generatePdfFromHtml(String htmlContent, String fileName) throws IOException {
        byte[] pdfBytes = generatePdfFromHtml(htmlContent);
        log.info("PDF '{}' generado exitosamente", fileName);
        return pdfBytes;
    }
}
