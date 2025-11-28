export const isValidEmail = (email: string): boolean => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
};

export const isValidDNI = (dni: string): boolean => {
    const dniRegex = /^\d{8}$/;
    return dniRegex.test(dni);
};

export const isValidRUC = (ruc: string): boolean => {
    const rucRegex = /^(10|20)\d{9}$/;
    return rucRegex.test(ruc);
};

export const isValidPhone = (phone: string): boolean => {
    // Acepta 9 dígitos (celular) o 7 dígitos (fijo)
    const phoneRegex = /^\d{7,9}$/;
    return phoneRegex.test(phone.replace(/\D/g, ''));
};

export const isRequired = (value: any): boolean => {
    if (value === null || value === undefined) return false;
    if (typeof value === 'string') return value.trim().length > 0;
    if (Array.isArray(value)) return value.length > 0;
    return true;
};
