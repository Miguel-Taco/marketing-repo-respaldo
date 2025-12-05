import { http } from "../../../../shared/services/api.client";
import { LeadReportFilterDTO } from "../types/lead.types";

const ENDPOINT = '/leads/reportes';

export const leadReportsApi = {
    // 1. Reporte General
    getGeneralReport: (filters: LeadReportFilterDTO) => {
        return downloadPdf(`${ENDPOINT}/general/pdf`, filters, 'reporte-general-leads.pdf');
    },

    // 2. Reporte de Fuentes
    getSourceReport: (filters: LeadReportFilterDTO) => {
        return downloadPdf(`${ENDPOINT}/fuentes/pdf`, filters, 'reporte-fuentes-leads.pdf');
    },

    // 3. Reporte de Conversión
    getConversionReport: (filters: LeadReportFilterDTO) => {
        return downloadPdf(`${ENDPOINT}/conversion/pdf`, filters, 'reporte-conversion-leads.pdf');
    },

    // 4. Reporte de Tendencias
    getTrendsReport: (filters: LeadReportFilterDTO, granularidad: string = 'DIARIO') => {
        return downloadPdf(`${ENDPOINT}/tendencias/pdf`, { ...filters, granularidad }, 'reporte-tendencias-leads.pdf');
    },

    // 5. Reporte Demográfico
    getDemographicReport: (filters: LeadReportFilterDTO) => {
        return downloadPdf(`${ENDPOINT}/demografico/pdf`, filters, 'reporte-demografico-leads.pdf');
    }
};

// Helper para descargar PDF
const downloadPdf = async (url: string, params: any, filename: string) => {
    try {
        const data = await http.get<Blob>(url, {
            params,
            responseType: 'blob' // Importante para archivos binarios
        });

        // Crear URL del blob y forzar descarga
        const blob = new Blob([data], { type: 'application/pdf' });
        const downloadUrl = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = downloadUrl;
        link.setAttribute('download', filename);
        document.body.appendChild(link);
        link.click();
        link.remove();
        window.URL.revokeObjectURL(downloadUrl);

        return true;
    } catch (error) {
        console.error("Error descargando reporte:", error);
        throw error;
    }
};
