export const APP_CONSTANTS = {
    PAGINATION: {
        DEFAULT_PAGE_SIZE: 10,
        PAGE_SIZE_OPTIONS: [10, 20, 50, 100]
    },
    DATE_FORMAT: 'dd/MM/yyyy',
    API: {
        TIMEOUT: 10000
    }
};

export const STATUS_COLORS: Record<string, string> = {
    'NUEVO': 'bg-blue-100 text-blue-800',
    'CALIFICADO': 'bg-green-100 text-green-800',
    'DESCARTADO': 'bg-red-100 text-red-800',
    'PENDIENTE': 'bg-yellow-100 text-yellow-800',
    'EN_PROCESO': 'bg-purple-100 text-purple-800',
    'COMPLETADO': 'bg-green-100 text-green-800',
    'CON_ERRORES': 'bg-red-100 text-red-800',
    'VACIO': 'bg-gray-100 text-gray-800'
};

export const STATUS_LABELS: Record<string, string> = {
    'NUEVO': 'Nuevo',
    'CALIFICADO': 'Calificado',
    'DESCARTADO': 'Descartado',
    'PENDIENTE': 'Pendiente',
    'EN_PROCESO': 'En Proceso',
    'COMPLETADO': 'Completado',
    'CON_ERRORES': 'Con Errores',
    'VACIO': 'Vacío'
};
