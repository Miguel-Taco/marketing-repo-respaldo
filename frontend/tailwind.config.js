/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./index.html",
        "./src/**/*.{js,ts,jsx,tsx}",
    ],
    theme: {
        extend: {
            fontFamily: {
                sans: ['Inter', 'sans-serif'],
            },
            colors: {
                primary: {
                    DEFAULT: '#3C83F6', // Azul corporativo
                    hover: '#2563EB',
                },
                background: '#F3F4F6',
                dark: '#1F2937',       // Texto principal
                separator: '#D9D9D9',  // Bordes

                // Estados (Chips)
                status: {
                    success: { bg: '#EBFBDC', text: '#6EA113' }, // CALIFICADO
                    warning: { bg: '#FFFBEB', text: '#D97706' }, // RECHAZADO
                    default: { bg: '#EAEAEA', text: '#555555' }, // NUEVO
                }
            },
            boxShadow: {
                'soft': '0 1px 3px 0 rgba(0, 0, 0, 0.05), 0 1px 2px 0 rgba(0, 0, 0, 0.03)',
                'glow': '0 4px 14px 0 rgba(60, 131, 246, 0.3)',
            }
        },
    },
    plugins: [
        require('@tailwindcss/typography'),
    ],
}
