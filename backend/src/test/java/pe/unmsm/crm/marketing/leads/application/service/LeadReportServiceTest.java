package pe.unmsm.crm.marketing.leads.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.unmsm.crm.marketing.leads.api.dto.LeadReportFilterDTO;
import pe.unmsm.crm.marketing.leads.domain.enums.EstadoLead;
import pe.unmsm.crm.marketing.leads.domain.enums.TipoFuente;
import pe.unmsm.crm.marketing.leads.domain.model.Lead;
import pe.unmsm.crm.marketing.leads.domain.repository.LeadRepository;
import pe.unmsm.crm.marketing.shared.services.HtmlTemplateService;
import pe.unmsm.crm.marketing.shared.services.PdfReportService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeadReportServiceTest {

    @Mock
    private LeadRepository leadRepository;

    @Mock
    private PdfReportService pdfReportService;

    @Mock
    private HtmlTemplateService htmlTemplateService;

    @InjectMocks
    private LeadReportService leadReportService;

    private LeadReportFilterDTO filterDTO;

    @BeforeEach
    void setUp() {
        filterDTO = LeadReportFilterDTO.builder()
                .fechaInicio(LocalDate.now().minusDays(7))
                .fechaFin(LocalDate.now())
                .build();
    }

    @Test
    void generateGeneralReport_ShouldReturnPdfBytes() throws IOException {
        // Arrange
        when(leadRepository.countByEstadoBetween(any(), any())).thenReturn(new ArrayList<>());
        when(leadRepository.countByFechaCreacionBetween(any(), any())).thenReturn(10L);
        when(leadRepository.findByFechaCreacionBetweenOrderByFechaCreacionAsc(any(), any()))
                .thenReturn(Collections.emptyList());
        when(htmlTemplateService.processTemplate(anyString(), anyMap())).thenReturn("<html></html>");
        when(pdfReportService.generatePdfFromHtml(anyString())).thenReturn(new byte[] { 1, 2, 3 });

        // Act
        byte[] result = leadReportService.generateGeneralReport(filterDTO);

        // Assert
        assertNotNull(result);
        verify(leadRepository).countByFechaCreacionBetween(any(), any());
        verify(htmlTemplateService).processTemplate(eq("lead-general-report.html"), anyMap());
        verify(pdfReportService).generatePdfFromHtml(anyString());
    }

    @Test
    void generateSourceReport_ShouldReturnPdfBytes() throws IOException {
        // Arrange
        List<Object[]> sourceCounts = new ArrayList<>();
        sourceCounts.add(new Object[] { TipoFuente.WEB, 5L });
        sourceCounts.add(new Object[] { TipoFuente.IMPORTACION, 3L });

        when(leadRepository.countByFuenteTipoBetween(any(), any())).thenReturn(sourceCounts);
        when(htmlTemplateService.processTemplate(anyString(), anyMap())).thenReturn("<html></html>");
        when(pdfReportService.generatePdfFromHtml(anyString())).thenReturn(new byte[] { 1, 2, 3 });

        // Act
        byte[] result = leadReportService.generateSourceReport(filterDTO);

        // Assert
        assertNotNull(result);
        verify(leadRepository).countByFuenteTipoBetween(any(), any());
        verify(htmlTemplateService).processTemplate(eq("lead-source-report.html"), anyMap());
    }
}
