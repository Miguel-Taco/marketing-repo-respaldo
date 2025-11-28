export const formatDate = (date: string | Date | undefined | null): string => {
    if (!date) return '-';
    const d = new Date(date);
    if (isNaN(d.getTime())) return '-';

    return new Intl.DateTimeFormat('es-PE', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    }).format(d);
};

export const formatCurrency = (amount: number | undefined | null): string => {
    if (amount === undefined || amount === null) return '-';
    return new Intl.NumberFormat('es-PE', {
        style: 'currency',
        currency: 'PEN'
    }).format(amount);
};

export const formatPhone = (phone: string | undefined | null): string => {
    if (!phone) return '-';
    // Limpiar caracteres no numéricos
    const cleaned = phone.replace(/\D/g, '');

    // Formato simple para celulares de Perú (9 dígitos)
    if (cleaned.length === 9) {
        return `(+51) ${cleaned.slice(0, 3)} ${cleaned.slice(3, 6)} ${cleaned.slice(6)}`;
    }

    return phone;
};

export const truncateText = (text: string, maxLength: number): string => {
    if (!text) return '';
    if (text.length <= maxLength) return text;
    return text.slice(0, maxLength) + '...';
};
