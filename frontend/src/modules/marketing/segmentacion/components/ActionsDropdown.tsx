import React, { useState, useRef, useEffect } from 'react';

interface ActionsDropdownProps {
    onDelete: () => void;
    onExport: () => void;
}

export const ActionsDropdown: React.FC<ActionsDropdownProps> = ({ onDelete, onExport }) => {
    const [isOpen, setIsOpen] = useState(false);
    const dropdownRef = useRef<HTMLDivElement>(null);

    // Close dropdown when clicking outside
    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
                setIsOpen(false);
            }
        };

        if (isOpen) {
            document.addEventListener('mousedown', handleClickOutside);
        }

        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, [isOpen]);

    const handleAction = (action: () => void) => {
        action();
        setIsOpen(false);
    };

    return (
        <div className="relative" ref={dropdownRef}>
            <button
                onClick={() => setIsOpen(!isOpen)}
                className="px-4 py-2 border border-separator rounded-lg hover:bg-gray-50 flex items-center gap-2 transition-colors"
            >
                <span className="material-symbols-outlined text-lg">more_horiz</span>
                <span className="font-medium">Acciones</span>
                <span className={`material-symbols-outlined text-lg transition-transform ${isOpen ? 'rotate-180' : ''}`}>
                    expand_more
                </span>
            </button>

            {isOpen && (
                <div className="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg border border-separator z-50">
                    <div className="py-1">
                        <button
                            onClick={() => handleAction(onExport)}
                            className="w-full px-4 py-2 text-left hover:bg-gray-50 flex items-center gap-3 transition-colors"
                        >
                            <span className="material-symbols-outlined text-lg text-primary">download</span>
                            <span className="text-gray-700">Exportar</span>
                        </button>
                        <div className="border-t border-separator my-1"></div>
                        <button
                            onClick={() => handleAction(onDelete)}
                            className="w-full px-4 py-2 text-left hover:bg-red-50 flex items-center gap-3 transition-colors"
                        >
                            <span className="material-symbols-outlined text-lg text-red-600">delete</span>
                            <span className="text-red-600">Eliminar</span>
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
};
