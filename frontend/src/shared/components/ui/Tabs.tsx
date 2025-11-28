import React from 'react';

interface TabItem {
    label: string;
    value: string;
}

interface TabsProps {
    items: TabItem[];
    activeValue: string;
    onChange: (value: string) => void;
}

export const Tabs: React.FC<TabsProps> = ({ items, activeValue, onChange }) => {
    return (
        <div className="flex space-x-2 border-b border-separator pb-2 mb-6">
            {items.map((tab) => {
                const isActive = tab.value === activeValue;
                return (
                    <button
                        key={tab.value}
                        onClick={() => onChange(tab.value)}
                        className={`
              px-5 py-2 rounded-full font-semibold text-sm transition-all duration-200
              ${isActive
                                ? 'bg-primary text-white shadow-md'
                                : 'bg-transparent text-dark border border-separator hover:bg-gray-50'}
            `}
                    >
                        {tab.label}
                    </button>
                );
            })}
        </div>
    );
};
