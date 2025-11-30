import React from 'react';
import ReactMarkdown from 'react-markdown';
import rehypeRaw from 'rehype-raw';
import { CreateGuionRequest } from '../types/guiones.types';
import { generateMarkdownFromScript } from '../utils/markdownGenerator';

interface MarkdownPreviewProps {
    data: CreateGuionRequest;
}

export const MarkdownPreview: React.FC<MarkdownPreviewProps> = ({ data }) => {
    const markdown = generateMarkdownFromScript(data);

    return (
        <div className="prose prose-blue max-w-none bg-white p-8 rounded-lg border border-gray-200">
            <ReactMarkdown
                rehypePlugins={[rehypeRaw]}
                children={markdown}
            />
        </div>
    );
};
