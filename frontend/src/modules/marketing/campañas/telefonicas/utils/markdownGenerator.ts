import { CreateGuionRequest, TIPOS_SECCION } from '../types/guiones.types';

export const generateMarkdownFromScript = (data: CreateGuionRequest): string => {
    let markdown = `# ${data.nombre || 'Sin título'}\n\n`;

    // Metadata
    markdown += `## Información del Guión\n\n`;
    markdown += `**Objetivo:** ${data.objetivo || 'No definido'}\n\n`;
    markdown += `**Tipo:** ${data.tipo || 'No definido'}\n\n`;

    if (data.notasInternas) {
        markdown += `**Notas Internas:** ${data.notasInternas}\n\n`;
    }

    markdown += `---\n\n`;

    // Sections
    if (data.secciones) {
        // Sort sections by order if needed, but they should be in order from the form
        const sortedSections = [...data.secciones].sort((a, b) => a.orden - b.orden);

        sortedSections.forEach(seccion => {
            // Find label for the section type
            const tipoInfo = Object.values(TIPOS_SECCION).find(t => t.value === seccion.tipoSeccion);
            const titulo = tipoInfo ? tipoInfo.label : seccion.tipoSeccion;

            // Only show sections with content or if it's a preview we might want to show empty ones too?
            // Let's show them if they have content to match backend logic usually, 
            // but for preview it might be nice to see structure. 
            // The backend logic shown earlier appends them.

            markdown += `## ${titulo}\n\n`;

            // The content from TipTap is HTML. We need to convert it to Markdown or just render it as HTML if we were using a different viewer.
            // But wait, the backend stores "contenido" which is generated from these sections.
            // The backend `generarMarkdown` just appends `seccion.getContenido()`.
            // If `seccion.getContenido()` is HTML (from TipTap), then the "Markdown" file will actually contain HTML.
            // Markdown supports HTML, so this is valid.
            // So we can just append the content.

            markdown += `${seccion.contenido || '(Sin contenido)'}\n\n`;
        });
    }

    return markdown;
};
