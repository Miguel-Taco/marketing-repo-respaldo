import React from 'react';

interface EditableTextFieldProps {
    label: string;
    value: string;
    isEditing: boolean;
    onChange: (value: string) => void;
    error?: string;
    required?: boolean;
    multiline?: boolean;
    rows?: number;
}

export const EditableTextField: React.FC<EditableTextFieldProps> = ({
    label,
    value,
    isEditing,
    onChange,
    error,
    required = false,
    multiline = false,
    rows = 3,
}) => {
    return (
        <div>
            <label className="text-sm text-gray-600">
                {label}
                {required && <span className="text-red-500 ml-1">*</span>}
            </label>
            {isEditing ? (
                <div className="mt-1">
                    {multiline ? (
                        <textarea
                            value={value || ''}
                            onChange={(e) => onChange(e.target.value)}
                            rows={rows}
                            className={`w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-primary focus:outline-none resize-none ${error ? 'border-red-500' : 'border-separator'
                                }`}
                        />
                    ) : (
                        <input
                            type="text"
                            value={value || ''}
                            onChange={(e) => onChange(e.target.value)}
                            className={`w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-primary focus:outline-none ${error ? 'border-red-500' : 'border-separator'
                                }`}
                        />
                    )}
                    {error && (
                        <p className="mt-1 text-sm text-red-500">{error}</p>
                    )}
                </div>
            ) : (
                <div className="mt-1 text-dark">
                    {value || <span className="text-gray-400">Sin {label.toLowerCase()}</span>}
                </div>
            )}
        </div>
    );
};
