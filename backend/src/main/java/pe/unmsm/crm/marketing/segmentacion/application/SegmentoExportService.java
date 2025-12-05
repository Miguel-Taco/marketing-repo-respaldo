package pe.unmsm.crm.marketing.segmentacion.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import pe.unmsm.crm.marketing.leads.domain.model.Lead;
import pe.unmsm.crm.marketing.leads.domain.repository.LeadRepository;
import pe.unmsm.crm.marketing.segmentacion.domain.model.Segmento;
import pe.unmsm.crm.marketing.segmentacion.domain.model.ReglaSegmento;
import pe.unmsm.crm.marketing.segmentacion.domain.model.ReglaSimple;
import pe.unmsm.crm.marketing.segmentacion.domain.model.GrupoReglasAnd;
import pe.unmsm.crm.marketing.segmentacion.domain.model.GrupoReglasOr;
import pe.unmsm.crm.marketing.segmentacion.infra.persistence.JpaSegmentoMiembroRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SegmentoExportService {

    private final LeadRepository leadRepository;
    private final JpaSegmentoMiembroRepository miembroRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] exportSegmentoToExcel(Segmento segmento) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Reporte Segmento");

            // Styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle infoLabelStyle = createInfoLabelStyle(workbook);
            CellStyle infoValueStyle = createInfoValueStyle(workbook);

            int currentRow = 0;

            // === SECCIÓN DE INFORMACIÓN DEL SEGMENTO ===
            currentRow = writeSegmentInfo(sheet, segmento, infoLabelStyle, infoValueStyle, currentRow);

            // Fila en blanco
            currentRow++;

            // === SECCIÓN DE TABLA DE MIEMBROS ===
            currentRow = writeMembersTable(sheet, segmento, headerStyle, currentRow);

            // Auto-size columns
            for (int i = 0; i < 10; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private int writeSegmentInfo(Sheet sheet, Segmento segmento, CellStyle labelStyle, CellStyle valueStyle,
            int startRow) {
        int row = startRow;

        // Título
        createInfoRow(sheet, row++, "Nombre del Segmento:", segmento.getNombre(), labelStyle, valueStyle);

        // Descripción
        String descripcion = segmento.getDescripcion() != null ? segmento.getDescripcion() : "Sin descripción";
        createInfoRow(sheet, row++, "Descripción:", descripcion, labelStyle, valueStyle);

        // Tipo de Audiencia
        createInfoRow(sheet, row++, "Tipo de Audiencia:", segmento.getTipoAudiencia(), labelStyle, valueStyle);

        // Filtros detallados
        row = writeFiltersInfo(sheet, segmento, row, labelStyle, valueStyle);

        return row;
    }

    private int writeFiltersInfo(Sheet sheet, Segmento segmento, int startRow, CellStyle labelStyle,
            CellStyle valueStyle) {
        int row = startRow;

        if (segmento.getReglaPrincipal() == null) {
            createInfoRow(sheet, row++, "Filtros:", "Sin filtros", labelStyle, valueStyle);
            return row;
        }

        // Create filters header row
        Row headerRow = sheet.createRow(row++);
        Cell filterLabelCell = headerRow.createCell(0);
        filterLabelCell.setCellValue("Filtros:");
        filterLabelCell.setCellStyle(labelStyle);

        // Get filters and display them horizontally
        List<String> filterTexts = new ArrayList<>();

        if (segmento.getReglaPrincipal() instanceof ReglaSimple) {
            // Single filter
            ReglaSimple regla = (ReglaSimple) segmento.getReglaPrincipal();
            filterTexts.add(formatFilter(regla));
        } else if (segmento.getReglaPrincipal() instanceof GrupoReglasAnd) {
            // Multiple filters with AND
            GrupoReglasAnd grupo = (GrupoReglasAnd) segmento.getReglaPrincipal();
            for (ReglaSegmento regla : grupo.getReglas()) {
                if (regla instanceof ReglaSimple) {
                    filterTexts.add(formatFilter((ReglaSimple) regla));
                }
            }
        } else if (segmento.getReglaPrincipal() instanceof GrupoReglasOr) {
            // Multiple filters with OR
            GrupoReglasOr grupo = (GrupoReglasOr) segmento.getReglaPrincipal();
            for (ReglaSegmento regla : grupo.getReglas()) {
                if (regla instanceof ReglaSimple) {
                    filterTexts.add(formatFilter((ReglaSimple) regla));
                }
            }
        }

        // Display each filter in a separate cell horizontally
        int col = 1;
        for (String filterText : filterTexts) {
            Cell filterCell = headerRow.createCell(col++);
            filterCell.setCellValue(filterText);
            filterCell.setCellStyle(valueStyle);
        }

        return row;
    }

    private String formatFilter(ReglaSimple regla) {
        StringBuilder sb = new StringBuilder();

        // Campo
        sb.append(formatCampo(regla.getCampo()));
        sb.append(" ");

        // Operador
        sb.append(formatOperador(regla.getOperador()));
        sb.append(" ");

        // Valor
        if (regla.getValorTexto() != null) {
            sb.append(regla.getValorTexto());
        } else if (regla.getValorNumeroDesde() != null) {
            if (regla.getValorNumeroHasta() != null) {
                sb.append(regla.getValorNumeroDesde()).append(" - ").append(regla.getValorNumeroHasta());
            } else {
                sb.append(regla.getValorNumeroDesde());
            }
        } else if (regla.getValorFechaDesde() != null) {
            if (regla.getValorFechaHasta() != null) {
                sb.append(regla.getValorFechaDesde()).append(" - ").append(regla.getValorFechaHasta());
            } else {
                sb.append(regla.getValorFechaDesde());
            }
        }

        return sb.toString();
    }

    private String formatCampo(String campo) {
        // Convertir nombres técnicos a nombres legibles
        switch (campo) {
            case "edad":
                return "Edad";
            case "genero":
                return "Género";
            case "distrito":
                return "Distrito";
            case "provincia":
                return "Provincia";
            case "departamento":
                return "Departamento";
            case "email":
                return "Email";
            case "telefono":
                return "Teléfono";
            case "nombre":
                return "Nombre";
            case "fechaCreacion":
                return "Fecha de Creación";
            default:
                return campo;
        }
    }

    private String formatOperador(String operador) {
        // Convertir operadores técnicos a símbolos legibles
        switch (operador) {
            case "IGUAL":
                return "=";
            case "DISTINTO":
                return "≠";
            case "MAYOR_QUE":
                return ">";
            case "MENOR_QUE":
                return "<";
            case "MAYOR_IGUAL":
                return ">=";
            case "MENOR_IGUAL":
                return "<=";
            case "CONTIENE":
                return "contiene";
            case "NO_CONTIENE":
                return "no contiene";
            case "EMPIEZA_CON":
                return "empieza con";
            case "TERMINA_CON":
                return "termina con";
            case "ENTRE":
                return "entre";
            default:
                return operador;
        }
    }

    private void createInfoRow(Sheet sheet, int rowNum, String label, String value, CellStyle labelStyle,
            CellStyle valueStyle) {
        Row row = sheet.createRow(rowNum);

        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(labelStyle);

        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);
        valueCell.setCellStyle(valueStyle);
    }

    private int writeMembersTable(Sheet sheet, Segmento segmento, CellStyle headerStyle, int startRow) {
        int row = startRow;

        // Header row
        Row headerRow = sheet.createRow(row++);
        String[] headers = { "ID", "Nombre Completo", "Email", "Teléfono", "Edad", "Género",
                "Distrito", "Provincia", "Departamento", "Fecha Creación" };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Get members
        List<Long> memberIds = miembroRepository.findByIdSegmento(segmento.getId()).stream()
                .map(entity -> entity.getIdMiembro())
                .collect(java.util.stream.Collectors.toList());
        List<Lead> leads = memberIds.isEmpty() ? List.of() : leadRepository.findAllByIdWithLocation(memberIds);

        // Data rows
        for (Lead lead : leads) {
            Row dataRow = sheet.createRow(row++);

            dataRow.createCell(0).setCellValue(lead.getId());
            dataRow.createCell(1).setCellValue(lead.getNombre() != null ? lead.getNombre() : "");
            dataRow.createCell(2).setCellValue(
                    lead.getContacto() != null && lead.getContacto().getEmail() != null
                            ? lead.getContacto().getEmail()
                            : "");
            dataRow.createCell(3).setCellValue(
                    lead.getContacto() != null && lead.getContacto().getTelefono() != null
                            ? lead.getContacto().getTelefono()
                            : "");
            dataRow.createCell(4).setCellValue(
                    lead.getDemograficos() != null && lead.getDemograficos().getEdad() != null
                            ? lead.getDemograficos().getEdad()
                            : 0);
            dataRow.createCell(5).setCellValue(
                    lead.getDemograficos() != null && lead.getDemograficos().getGenero() != null
                            ? lead.getDemograficos().getGenero().toString()
                            : "");

            // Ubigeo data using object graph
            if (lead.getDemograficos() != null && lead.getDemograficos().getDistrito() != null) {
                var distrito = lead.getDemograficos().getDistrito();
                dataRow.createCell(6).setCellValue(distrito.getNombre());
                dataRow.createCell(7).setCellValue(
                        distrito.getProvincia() != null ? distrito.getProvincia().getNombre() : "");
                dataRow.createCell(8).setCellValue(
                        distrito.getProvincia() != null && distrito.getProvincia().getDepartamento() != null
                                ? distrito.getProvincia().getDepartamento().getNombre()
                                : "");
            } else {
                dataRow.createCell(6).setCellValue("");
                dataRow.createCell(7).setCellValue("");
                dataRow.createCell(8).setCellValue("");
            }

            dataRow.createCell(9).setCellValue(
                    lead.getFechaCreacion() != null ? lead.getFechaCreacion().format(DATE_FORMATTER) : "");
        }

        return row;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);

        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);

        return style;
    }

    private CellStyle createInfoLabelStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);

        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        return style;
    }

    private CellStyle createInfoValueStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);

        return style;
    }
}
