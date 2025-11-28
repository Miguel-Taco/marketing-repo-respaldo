import React from 'react';

export type BadgeVariant = 'success' | 'warning' | 'default' | 'info' | 'danger';

interface BadgeProps {
    variant?: BadgeVariant;
    children: React.ReactNode;
}

const styles: Record<BadgeVariant, string> = {
    success: "bg-status-success-bg text-status-success-text",
    warning: "bg-status-warning-bg text-status-warning-text",
    default: "bg-status-default-bg text-status-default-text",
    info: "bg-blue-100 text-blue-700",
    danger: "bg-red-100 text-red-700",
};

export const Badge: React.FC<BadgeProps> = ({ children, variant = 'default' }) => {
    return (
        <span className={`px-3 py-1 rounded-full text-xs font-bold uppercase ${styles[variant]}`}>
            {children}
        </span>
    );
};
