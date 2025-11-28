package pe.unmsm.crm.marketing.leads.application.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import pe.unmsm.crm.marketing.leads.domain.model.staging.LoteImportacion;
import pe.unmsm.crm.marketing.leads.domain.repository.LoteRepository;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ImportService {

    private final LoteRepository loteRepository;
    private final AsyncImportProcessor asyncProcessor; // ← NUEVO: Inyectar procesador async

    // 1. Método Síncrono: Inicia el proceso y retorna el ID
    // NO tiene @Transactional para evitar que errores en async marquen rollback
    public LoteImportacion iniciarImportacion(MultipartFile file) {
        try {
            // Crear Lote Inicial
            LoteImportacion lote = crearLoteInicial(file.getOriginalFilename());

            // Leer Excel en memoria (rápido para 30-100 registros)
            List<Map<String, String>> filas = leerExcel(file);
            lote.setTotalRegistros(filas.size());
            lote = loteRepository.save(lote);

            // Disparar proceso asíncrono EN OTRA CLASE (esto sí funciona con @Async)
            asyncProcessor.procesarFilasAsync(lote.getId(), filas);

            return lote; // Retornar inmediatamente "EN_PROCESO"
        } catch (IOException e) {
            throw new RuntimeException("Error al leer archivo Excel", e);
        }
    }

    @Transactional
    protected LoteImportacion crearLoteInicial(String filename) {
        LoteImportacion lote = new LoteImportacion();
        lote.setNombreArchivo(filename);
        lote.setTotalRegistros(0);
        return loteRepository.save(lote);
    }

    public Page<LoteImportacion> obtenerHistorial(Pageable pageable) {
        return loteRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public LoteImportacion obtenerLotePorId(@NonNull Long id) {
        return loteRepository.findById(id)
                .orElseThrow(() -> new pe.unmsm.crm.marketing.shared.infra.exception.NotFoundException(
                        "LoteImportacion", id));
    }

    // Utilidad simple para leer Excel a Map
    private List<Map<String, String>> leerExcel(MultipartFile file) throws IOException {
        List<Map<String, String>> lista = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            // Mapeo de columnas
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow)
                headers.add(cell.getStringCellValue());

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null)
                    continue;

                Map<String, String> datos = new HashMap<>();
                boolean hasData = false; // Flag para verificar si la fila tiene datos

                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j);
                    String valor = "";

                    if (cell != null) {
                        // Manejo mejorado de tipos de celda para evitar .toString() genérico
                        switch (cell.getCellType()) {
                            case STRING:
                                valor = cell.getStringCellValue().trim();
                                break;
                            case NUMERIC:
                                if (DateUtil.isCellDateFormatted(cell)) {
                                    valor = cell.getDateCellValue().toString();
                                } else {
                                    // Evitar notación científica en números (ej: teléfonos)
                                    double num = cell.getNumericCellValue();
                                    if (num == (long) num) {
                                        valor = String.format("%d", (long) num);
                                    } else {
                                        valor = String.valueOf(num);
                                    }
                                }
                                break;
                            case BOOLEAN:
                                valor = String.valueOf(cell.getBooleanCellValue());
                                break;
                            case FORMULA:
                                try {
                                    valor = cell.getStringCellValue();
                                } catch (Exception e) {
                                    valor = String.valueOf(cell.getNumericCellValue());
                                }
                                break;
                            default:
                                valor = "";
                        }
                    }

                    if (!valor.isEmpty()) {
                        hasData = true;
                    }
                    datos.put(headers.get(j), valor);
                }

                // Solo agregar si la fila tiene al menos un dato no vacío
                if (hasData) {
                    lista.add(datos);
                }
            }
        }
        return lista;
    }
}
