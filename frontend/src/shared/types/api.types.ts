export interface ApiResponse<T> {
    status: string;    // "OK" | "ACCEPTED"
    message: string;
    timestamp: string; // ISO Date
    data: T;           // El objeto real
}

export interface PaginatedResponse<T> {
    content: T[];
    totalPages: number;
    totalElements: number;
    size: number;
    number: number;
}
