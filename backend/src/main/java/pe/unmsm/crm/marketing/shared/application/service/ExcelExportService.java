package pe.unmsm.crm.marketing.shared.application.service;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

/**
 * Generic Excel export service for creating Excel files from any type of data
 * Provides reusable functionality for Excel generation across the application
 */
@Service
@RequiredArgsConstructor
public class ExcelExportService {

    /**
     * Exports a list of data to Excel format
     * 
     * @param data   List of objects to export
     * @param config Configuration for Excel generation
     * @param <T>    Type of data being exported
     * @return Byte array containing the Excel file
     * @throws IOException if Excel generation fails
     */
    public <T> byte[] exportToExcel(List<T> data, ExcelConfig<T> config) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(config.getSheetName());

            // Create header style
            CellStyle headerStyle = createHeaderStyle(workbook);

            // Create header row
            Row headerRow = sheet.createRow(0);
            List<ColumnConfig<T>> columns = config.getColumns();

            for (int i = 0; i < columns.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns.get(i).getHeader());
                cell.setCellStyle(headerStyle);
            }

            // Fill data rows
            int rowNum = 1;
            for (T item : data) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < columns.size(); i++) {
                    Cell cell = row.createCell(i);
                    Object value = columns.get(i).getValueExtractor().apply(item);
                    setCellValue(cell, value);
                }
            }

            // Auto-size columns
            for (int i = 0; i < columns.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * Sets cell value based on object type
     */
    private void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else {
            cell.setCellValue(value.toString());
        }
    }

    /**
     * Creates the default header cell style
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);

        // Set background color
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Set borders
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);

        return style;
    }

    /**
     * Configuration class for Excel export
     */
    public static class ExcelConfig<T> {
        private final String sheetName;
        private final List<ColumnConfig<T>> columns;

        public ExcelConfig(String sheetName, List<ColumnConfig<T>> columns) {
            this.sheetName = sheetName;
            this.columns = columns;
        }

        public String getSheetName() {
            return sheetName;
        }

        public List<ColumnConfig<T>> getColumns() {
            return columns;
        }
    }

    /**
     * Configuration for a single column
     */
    public static class ColumnConfig<T> {
        private final String header;
        private final Function<T, Object> valueExtractor;

        public ColumnConfig(String header, Function<T, Object> valueExtractor) {
            this.header = header;
            this.valueExtractor = valueExtractor;
        }

        public String getHeader() {
            return header;
        }

        public Function<T, Object> getValueExtractor() {
            return valueExtractor;
        }
    }
}
