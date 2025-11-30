import React from 'react';
import ReactMarkdown from 'react-markdown';
import rehypeRaw from 'rehype-raw';

interface MarkdownViewerProps {
    content: string;
    className?: string;
}

/**
 * Componente para visualizar contenido markdown con estilos modernos.
 * Usa react-markdown y @tailwindcss/typography para renderizado y estilos.
 * Soporta HTML embebido mediante rehype-raw.
 */
export const MarkdownViewer: React.FC<MarkdownViewerProps> = ({ content, className = '' }) => {
    return (
        <div className={`prose prose-lg max-w-none ${className}`}>
            <ReactMarkdown rehypePlugins={[rehypeRaw]}>
                {content}
            </ReactMarkdown>
        </div>
    );
};
