import React, { useState, useRef, DragEvent, ChangeEvent } from 'react';

interface FileDropzoneProps {
    onFileSelect: (file: File) => void;
    accept?: string;
    maxSize?: number; // in bytes
    selectedFile?: File | null;
    disabled?: boolean;
}

export const FileDropzone: React.FC<FileDropzoneProps> = ({
    onFileSelect,
    accept = '*',
    maxSize = 10 * 1024 * 1024, // 10MB default
    selectedFile = null,
    disabled = false
}) => {
    const [isDragging, setIsDragging] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const fileInputRef = useRef<HTMLInputElement>(null);

    const validateFile = (file: File): string | null => {
        // Validate file type
        if (accept !== '*') {
            const acceptedTypes = accept.split(',').map(type => type.trim());
            const fileExtension = '.' + file.name.split('.').pop()?.toLowerCase();

            if (!acceptedTypes.some(type => fileExtension === type.toLowerCase())) {
                return `Solo se aceptan archivos ${accept}`;
            }
        }

        // Validate file size
        if (file.size > maxSize) {
            const maxSizeMB = (maxSize / (1024 * 1024)).toFixed(1);
            return `El archivo excede el tamaño máximo de ${maxSizeMB}MB`;
        }

        return null;
    };

    const handleFile = (file: File) => {
        const validationError = validateFile(file);

        if (validationError) {
            setError(validationError);
            return;
        }

        setError(null);
        onFileSelect(file);
    };

    const handleDragEnter = (e: DragEvent<HTMLDivElement>) => {
        e.preventDefault();
        e.stopPropagation();
        if (!disabled) {
            setIsDragging(true);
        }
    };

    const handleDragLeave = (e: DragEvent<HTMLDivElement>) => {
        e.preventDefault();
        e.stopPropagation();
        setIsDragging(false);
    };

    const handleDragOver = (e: DragEvent<HTMLDivElement>) => {
        e.preventDefault();
        e.stopPropagation();
    };

    const handleDrop = (e: DragEvent<HTMLDivElement>) => {
        e.preventDefault();
        e.stopPropagation();
        setIsDragging(false);

        if (disabled) return;

        const files = e.dataTransfer.files;
        if (files && files.length > 0) {
            handleFile(files[0]);
        }
    };

    const handleFileInputChange = (e: ChangeEvent<HTMLInputElement>) => {
        const files = e.target.files;
        if (files && files.length > 0) {
            handleFile(files[0]);
        }
    };

    const handleClick = () => {
        if (!disabled) {
            fileInputRef.current?.click();
        }
    };

    const formatFileSize = (bytes: number): string => {
        if (bytes < 1024) return bytes + ' B';
        if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
        return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
    };

    const getAcceptLabel = (): string => {
        if (accept === '*') return 'Cualquier archivo';
        return accept.replace(/\./g, '').toUpperCase();
    };

    const maxSizeLabel = (maxSize / (1024 * 1024)).toFixed(0) + 'MB';

    return (
        <div className="space-y-2">
            <div
                onClick={handleClick}
                onDragEnter={handleDragEnter}
                onDragLeave={handleDragLeave}
                onDragOver={handleDragOver}
                onDrop={handleDrop}
                className={`
                    flex flex-col items-center justify-center p-8 
                    border-2 border-dashed rounded-lg text-center 
                    transition-all duration-200 cursor-pointer
                    ${disabled ? 'opacity-50 cursor-not-allowed bg-gray-100' : ''}
                    ${isDragging && !disabled
                        ? 'border-primary bg-primary/5 border-solid'
                        : selectedFile
                            ? 'border-green-500 bg-green-50'
                            : 'border-separator bg-gray-50 hover:border-primary hover:bg-gray-100'
                    }
                `}
            >
                {/* Icon */}
                <span className={`material-symbols-outlined text-5xl mb-2 ${isDragging && !disabled
                    ? 'text-primary'
                    : selectedFile
                        ? 'text-green-600'
                        : 'text-gray-400'
                    }`}>
                    {isDragging && !disabled ? 'download' : selectedFile ? 'check_circle' : 'cloud_upload'}
                </span>

                {/* Text */}
                {selectedFile ? (
                    <div>
                        <p className="font-semibold text-dark text-lg flex items-center justify-center">
                            <span className="material-symbols-outlined text-green-500 mr-2">check_circle</span> {selectedFile.name}
                        </p>
                        <p className="text-sm text-gray-500 mt-1">
                            {formatFileSize(selectedFile.size)}
                        </p>
                        <p className="text-sm text-gray-400 mt-2">
                            Haz clic para cambiar archivo
                        </p>
                    </div>
                ) : isDragging && !disabled ? (
                    <div>
                        <p className="font-semibold text-primary text-lg">
                            ¡Suelta el archivo aquí!
                        </p>
                    </div>
                ) : (
                    <div>
                        <p className="font-semibold text-dark">
                            Arrastra un archivo aquí
                        </p>
                        <p className="text-sm text-gray-500 mt-1">
                            o haz clic para buscar
                        </p>
                        <p className="text-xs text-gray-400 mt-2">
                            {getAcceptLabel()} (Máx {maxSizeLabel})
                        </p>
                    </div>
                )}

                {/* Hidden file input */}
                <input
                    ref={fileInputRef}
                    type="file"
                    className="hidden"
                    accept={accept}
                    onChange={handleFileInputChange}
                    disabled={disabled}
                />
            </div>

            {/* Error message */}
            {error && (
                <div className="flex items-center gap-2 p-3 bg-red-50 border border-red-200 rounded-lg">
                    <span className="material-symbols-outlined text-red-600 text-lg">error</span>
                    <p className="text-sm text-red-600 font-medium">{error}</p>
                </div>
            )}
        </div>
    );
};
