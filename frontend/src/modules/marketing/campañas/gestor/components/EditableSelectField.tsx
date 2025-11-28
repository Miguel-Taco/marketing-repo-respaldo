import React, { ReactNode } from 'react';

interface EditableSelectFieldProps<T = string> {
    label: string;
    value: T;
    options: Array<{ value: T; label: string }>;
    isEditing: boolean;
    onChange: (value: T) => void;
    error?: string;
    renderDisplay?: (value: T) => ReactNode;
}

export const EditableSelectField = <T extends string>({
    label,
    value,
    options,
    isEditing,
    onChange,
    error,
    renderDisplay,
}: EditableSelectFieldProps<T>) => {
    return (
        <div>
            <label className="text-sm text-gray-600">{label}</label>
            {isEditing ? (
                <div className="mt-1">
                    <select
                        value={value as string}
                        onChange={(e) => onChange(e.target.value as T)}
                        className={`w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-primary focus:outline-none ${error ? 'border-red-500' : 'border-separator'
                            }`}
                    >
                        {options.map((option) => (
                            <option key={option.value as string} value={option.value as string}>
                                {option.label}
                            </option>
                        ))}
                    </select>
                    {error && (
                        <p className="mt-1 text-sm text-red-500">{error}</p>
                    )}
                </div>
            ) : (
                <div className="mt-1">
                    {renderDisplay ? renderDisplay(value) : <span className="text-dark">{value}</span>}
                </div>
            )}
        </div>
    );
};
