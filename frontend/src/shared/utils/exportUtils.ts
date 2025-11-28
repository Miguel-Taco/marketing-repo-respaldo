import { apiClient } from '../services/api.client';
import type { AxiosRequestConfig } from 'axios';

/**
 * Downloads a blob as a file with the specified filename
 * @param blob - The Blob object to download
 * @param filename - The name for the downloaded file
 */
export function downloadBlob(blob: Blob, filename: string): void {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', filename);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
}

/**
 * Exports data to Excel via API endpoint
 * @param endpoint - The API endpoint to call
 * @param params - Optional query parameters for GET requests
 * @param method - HTTP method (GET or POST)
 * @param body - Request body for POST requests
 * @param filename - Optional custom filename (auto-generated if not provided)
 */
export async function exportToExcel(
    endpoint: string,
    params?: Record<string, any>,
    method: 'GET' | 'POST' = 'GET',
    body?: any,
    filename?: string
): Promise<void> {
    const config: AxiosRequestConfig = {
        responseType: 'blob',
        ...(params && { params })
    };

    const response = method === 'GET'
        ? await apiClient.get(endpoint, config)
        : await apiClient.post(endpoint, body, config);

    const blob = new Blob([response.data], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    });

    const finalFilename = filename || generateExportFilename('export');
    downloadBlob(blob, finalFilename);
}

/**
 * Generates a timestamped filename for exports
 * @param prefix - Prefix for the filename (e.g., 'leads', 'campaigns')
 * @param extension - File extension (default: 'xlsx')
 * @returns Generated filename with timestamp
 */
export function generateExportFilename(
    prefix: string,
    extension: string = 'xlsx'
): string {
    const timestamp = new Date().toISOString().split('T')[0];
    return `${prefix}_${timestamp}.${extension}`;
}

/**
 * Downloads any binary data (PDF, images, etc.) from an API endpoint
 * @param endpoint - The API endpoint to call
 * @param filename - The filename for the download
 * @param mimeType - MIME type of the file
 * @param params - Optional query parameters
 */
export async function downloadFile(
    endpoint: string,
    filename: string,
    mimeType: string,
    params?: Record<string, any>
): Promise<void> {
    const config: AxiosRequestConfig = {
        responseType: 'blob',
        ...(params && { params })
    };

    const response = await apiClient.get(endpoint, config);
    const blob = new Blob([response.data], { type: mimeType });
    downloadBlob(blob, filename);
}

/**
 * Downloads data as a CSV file
 * @param data - Array of objects to export
 * @param filename - Name for the CSV file (without extension)
 * @param columns - Array of column definitions with key and label
 */
export function downloadCSV<T extends Record<string, any>>(
    data: T[],
    filename: string,
    columns: Array<{ key: keyof T; label: string }>
): void {
    if (data.length === 0) {
        console.warn('No data to export');
        return;
    }

    // Create CSV header
    const header = columns.map(col => col.label).join(',');

    // Create CSV rows
    const rows = data.map(item =>
        columns.map(col => {
            const value = item[col.key];
            // Escape values containing commas or quotes
            if (value === null || value === undefined) return '';
            const stringValue = String(value);
            if (stringValue.includes(',') || stringValue.includes('"') || stringValue.includes('\n')) {
                return `"${stringValue.replace(/"/g, '""')}"`;
            }
            return stringValue;
        }).join(',')
    );

    // Combine header and rows
    const csv = [header, ...rows].join('\n');

    // Create and download blob
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const finalFilename = `${filename}_${new Date().toISOString().split('T')[0]}.csv`;
    downloadBlob(blob, finalFilename);
}
