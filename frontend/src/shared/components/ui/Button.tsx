import React from 'react';

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
    variant?: 'primary' | 'secondary' | 'danger';
    isLoading?: boolean;
    icon?: string; // Nombre del icono de Google Fonts
}

export const Button: React.FC<ButtonProps> = ({
    variant = 'primary',
    isLoading,
    icon,
    children,
    className = '',
    ...props
}) => {
    const baseStyles = "rounded-full px-5 py-2.5 font-semibold text-sm transition-all duration-200 flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed";
    const variants = {
        primary: "bg-primary text-white hover:bg-primary-hover shadow-glow",
        secondary: "bg-white text-dark border border-separator hover:bg-gray-50",
        danger: "bg-red-500 text-white hover:bg-red-600 shadow-glow",
    };

    return (
        <button className={`${baseStyles} ${variants[variant]} ${className}`} disabled={isLoading || props.disabled} {...props}>
            {isLoading ? (
                <span className="animate-spin h-4 w-4 border-2 border-current border-t-transparent rounded-full" />
            ) : icon ? (
                <span className="material-symbols-outlined text-lg">{icon}</span>
            ) : null}
            {children}
        </button>
    );
};
