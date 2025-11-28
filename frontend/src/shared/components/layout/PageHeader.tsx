import React, { ReactNode } from 'react';

interface PageHeaderProps {
    title: string;
    subtitle?: string;
    action?: ReactNode;
}

export const PageHeader: React.FC<PageHeaderProps> = ({ title, subtitle, action }) => {
    return (
        <div className="mb-8 flex flex-col sm:flex-row sm:items-center sm:justify-between">
            <div>
                <h1 className="text-2xl font-bold text-gray-900">{title}</h1>
                {subtitle && (
                    <p className="mt-1 text-sm text-gray-500">{subtitle}</p>
                )}
            </div>
            {action && (
                <div className="mt-4 sm:mt-0">
                    {action}
                </div>
            )}
        </div>
    );
};
