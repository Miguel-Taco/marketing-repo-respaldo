import React, { useState, useEffect } from 'react';

interface EditableAsyncSelectProps {
    label: string;
    value: number | null | undefined;
    isEditing: boolean;
    onChange: (value: number | undefined) => void;
    loadOptions: () => Promise<Array<{ value: number; label: string }>>;
    loadLabel?: (value: number) => Promise<string>; // Optional: load label for selected value in read mode
    error?: string;
    allowClear?: boolean;
    placeholder?: string;
}

export const EditableAsyncSelect: React.FC<EditableAsyncSelectProps> = ({
    label,
    value,
    isEditing,
    onChange,
    loadOptions,
    loadLabel,
    error,
    allowClear = true,
    placeholder = 'Sin asignar',
}) => {
    const [options, setOptions] = useState<Array<{ value: number; label: string }>>([]);
    const [loading, setLoading] = useState(false);
    const [selectedLabel, setSelectedLabel] = useState<string>(placeholder);
    const [loadingLabel, setLoadingLabel] = useState(false);

    // Load options when entering edit mode
    useEffect(() => {
        if (isEditing && options.length === 0) {
            setLoading(true);
            loadOptions()
                .then((loadedOptions) => {
                    setOptions(loadedOptions);
                    // Update selected label if value exists
                    if (value) {
                        const option = loadedOptions.find((opt) => opt.value === value);
                        if (option) {
                            setSelectedLabel(option.label);
                        }
                    }
                })
                .catch((error) => {
                    console.error('Error loading options:', error);
                })
                .finally(() => {
                    setLoading(false);
                });
        }
    }, [isEditing, options.length, loadOptions, value]);

    // Update selected label when value changes (for edit mode or if options are already loaded)
    useEffect(() => {
        if (isEditing && value && options.length > 0) {
            const option = options.find((opt) => opt.value === value);
            setSelectedLabel(option ? option.label : placeholder);
        } else if (isEditing && !value) {
            setSelectedLabel(placeholder);
        }
    }, [value, options, placeholder, isEditing]);

    // Load label for current value in read mode
    useEffect(() => {
        if (!isEditing && value && loadLabel) {
            // Check if we already have the label in options
            const existingOption = options.find((opt) => opt.value === value);
            if (existingOption) {
                setSelectedLabel(existingOption.label);
            } else {
                // Load label from API
                setLoadingLabel(true);
                loadLabel(value)
                    .then((label) => {
                        setSelectedLabel(label);
                    })
                    .catch((error) => {
                        console.error('Error loading label:', error);
                        setSelectedLabel(`ID: ${value}`);
                    })
                    .finally(() => {
                        setLoadingLabel(false);
                    });
            }
        } else if (!isEditing && !value) {
            setSelectedLabel(placeholder);
        }
    }, [value, isEditing, loadLabel, options, placeholder]);

    const handleChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        const newValue = e.target.value;
        if (newValue === '') {
            onChange(undefined);
        } else {
            onChange(parseInt(newValue, 10));
        }
    };

    return (
        <div>
            <label className="text-sm text-gray-600">{label}</label>
            {isEditing ? (
                <div className="mt-1">
                    {loading ? (
                        <div className="w-full px-3 py-2 border border-separator rounded-lg bg-gray-50 text-gray-500">
                            Cargando opciones...
                        </div>
                    ) : (
                        <select
                            value={value || ''}
                            onChange={handleChange}
                            className={`w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-primary focus:outline-none ${error ? 'border-red-500' : 'border-separator'
                                }`}
                        >
                            {allowClear && <option value="">-- {placeholder} --</option>}
                            {options.map((option) => (
                                <option key={option.value} value={option.value}>
                                    {option.label}
                                </option>
                            ))}
                        </select>
                    )}
                    {error && (
                        <p className="mt-1 text-sm text-red-500">{error}</p>
                    )}
                </div>
            ) : (
                <div className="mt-1 text-dark">
                    {loadingLabel ? (
                        <span className="text-gray-500">Cargando...</span>
                    ) : value ? (
                        selectedLabel
                    ) : (
                        <span className="text-gray-400">{placeholder}</span>
                    )}
                </div>
            )}
        </div>
    );
};
