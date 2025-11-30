// Formatear duración en segundos a "Xm Ys"
export const formatDuration = (seconds: number): string => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}m ${secs}s`;
};

// Obtener color según tipo de resultado
export const getColorForResult = (resultado: string): string => {
    const colorMap: Record<string, string> = {
        'CONTACTADO': 'text-green-500',
        'INTERESADO': 'text-green-500',
        'BUZON': 'text-blue-500',
        'NO_CONTESTA': 'text-yellow-500',
        'NO_INTERESADO': 'text-gray-400',
    };
    return colorMap[resultado] || 'text-gray-400';
};

// Obtener color de fondo según tipo de resultado
export const getBgColorForResult = (resultado: string): string => {
    const colorMap: Record<string, string> = {
        'CONTACTADO': 'bg-green-500',
        'INTERESADO': 'bg-green-500',
        'BUZON': 'bg-blue-500',
        'NO_CONTESTA': 'bg-yellow-500',
        'NO_INTERESADO': 'bg-gray-300',
    };
    return colorMap[resultado] || 'bg-gray-400';
};

// Obtener nombre del día de la semana
export const getDayName = (date: string): string => {
    const days = ['Dom', 'Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb'];
    const d = new Date(date);
    return days[d.getDay()];
};

// Formatear número con separadores de miles
export const formatNumber = (num: number): string => {
    return num.toLocaleString('es-PE');
};

// Calcular porcentaje de cambio
export const calculatePercentageChange = (current: number, previous: number): number => {
    if (previous === 0) return current > 0 ? 100 : 0;
    return ((current - previous) / previous) * 100;
};
