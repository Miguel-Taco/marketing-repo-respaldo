import React, { useState } from 'react';
import { Button } from '../../../../shared/components/ui/Button';
import { FileDropzone } from '../../../../shared/components/ui/FileDropzone';

interface Props {
    onUpload: (file: File) => Promise<void>;
    isUploading: boolean;
}

export const ImportUploader: React.FC<Props> = ({ onUpload, isUploading }) => {
    const [selectedFile, setSelectedFile] = useState<File | null>(null);

    const handleUploadClick = () => {
        if (selectedFile) {
            onUpload(selectedFile);
            setSelectedFile(null); // Reset después de subir
        }
    };

    return (
        <div className="bg-white rounded-lg shadow-card border border-separator">
            <div className="p-5 border-b border-separator">
                <h3 className="text-lg font-semibold text-dark">Cargar Nuevo Lote</h3>
            </div>
            <div className="p-6 space-y-4">
                {/* FileDropzone Component */}
                <FileDropzone
                    onFileSelect={setSelectedFile}
                    accept=".xlsx"
                    maxSize={5 * 1024 * 1024} // 5MB
                    selectedFile={selectedFile}
                    disabled={isUploading}
                />

                <Button
                    className="w-full"
                    onClick={handleUploadClick}
                    disabled={!selectedFile || isUploading}
                    isLoading={isUploading}
                >
                    Iniciar Carga
                </Button>
            </div>
        </div>
    );
};
