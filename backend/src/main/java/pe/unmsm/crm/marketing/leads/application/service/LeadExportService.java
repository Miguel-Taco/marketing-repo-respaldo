package pe.unmsm.crm.marketing.leads.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.unmsm.crm.marketing.leads.domain.model.Lead;
import pe.unmsm.crm.marketing.shared.application.service.ExcelExportService;
import pe.unmsm.crm.marketing.shared.application.service.ExcelExportService.ExcelConfig;
import pe.unmsm.crm.marketing.shared.application.service.ExcelExportService.ColumnConfig;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for exporting Leads to Excel format
 * Uses the generic ExcelExportService for actual Excel generation
 */
@Service
@RequiredArgsConstructor
public class LeadExportService {

        private final ExcelExportService excelExportService;

        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        /**
         * Exports a list of leads to Excel format
         * 
         * @param leads List of leads to export
         * @return Byte array containing the Excel file
         * @throws IOException if Excel generation fails
         */
        public byte[] exportLeadsToExcel(List<Lead> leads) throws IOException {
                ExcelConfig<Lead> config = buildLeadExcelConfig();
                return excelExportService.exportToExcel(leads, config);
        }

        /**
         * Builds the Excel configuration for Lead export
         * Defines which columns to include and how to extract values
         */
        private ExcelConfig<Lead> buildLeadExcelConfig() {
                List<ColumnConfig<Lead>> columns = new ArrayList<>();

                // ID
                columns.add(new ColumnConfig<>("ID", Lead::getId));

                // Nombre Completo
                columns.add(new ColumnConfig<>("Nombre Completo",
                                lead -> lead.getNombre() != null ? lead.getNombre() : ""));

                // Email
                columns.add(new ColumnConfig<>("Email",
                                lead -> lead.getContacto() != null && lead.getContacto().getEmail() != null
                                                ? lead.getContacto().getEmail()
                                                : ""));

                // Teléfono
                columns.add(new ColumnConfig<>("Teléfono",
                                lead -> lead.getContacto() != null && lead.getContacto().getTelefono() != null
                                                ? lead.getContacto().getTelefono()
                                                : ""));

                // Edad
                columns.add(new ColumnConfig<>("Edad",
                                lead -> lead.getDemograficos() != null && lead.getDemograficos().getEdad() != null
                                                ? lead.getDemograficos().getEdad()
                                                : 0));

                // Género
                columns.add(new ColumnConfig<>("Género",
                                lead -> lead.getDemograficos() != null && lead.getDemograficos().getGenero() != null
                                                ? lead.getDemograficos().getGenero().toString()
                                                : ""));

                // Estado
                columns.add(new ColumnConfig<>("Estado",
                                lead -> lead.getEstado() != null ? lead.getEstado().toString() : ""));

                // Fuente
                columns.add(new ColumnConfig<>("Fuente",
                                lead -> lead.getFuenteTipo() != null ? lead.getFuenteTipo().toString() : ""));

                // Distrito (using object graph)
                columns.add(new ColumnConfig<>("Distrito", this::getDistritoName));

                // Provincia (using object graph)
                columns.add(new ColumnConfig<>("Provincia", this::getProvinciaName));

                // Departamento (using object graph)
                columns.add(new ColumnConfig<>("Departamento", this::getDepartamentoName));

                // Fecha Creación
                columns.add(new ColumnConfig<>("Fecha Creación",
                                lead -> lead.getFechaCreacion() != null
                                                ? lead.getFechaCreacion().format(DATE_FORMATTER)
                                                : ""));

                return new ExcelConfig<>("Leads", columns);
        }

        /**
         * Helper methods using object graph for location names
         */
        private String getDistritoName(Lead lead) {
                if (lead.getDemograficos() == null || lead.getDemograficos().getDistrito() == null) {
                        return "";
                }
                return lead.getDemograficos().getDistrito().getNombre();
        }

        private String getProvinciaName(Lead lead) {
                if (lead.getDemograficos() == null || lead.getDemograficos().getDistrito() == null) {
                        return "";
                }
                var distrito = lead.getDemograficos().getDistrito();
                return distrito.getProvincia() != null ? distrito.getProvincia().getNombre() : "";
        }

        private String getDepartamentoName(Lead lead) {
                if (lead.getDemograficos() == null || lead.getDemograficos().getDistrito() == null) {
                        return "";
                }
                var distrito = lead.getDemograficos().getDistrito();
                if (distrito.getProvincia() != null && distrito.getProvincia().getDepartamento() != null) {
                        return distrito.getProvincia().getDepartamento().getNombre();
                }
                return "";
        }
}
