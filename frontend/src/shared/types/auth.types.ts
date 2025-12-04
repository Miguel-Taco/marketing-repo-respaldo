export interface LoginRequest {
    username: string;
    password: string;
}

export interface UserInfo {
    userId: number;
    username: string;
    activo: boolean;
    roles: string[];
    agentId?: number;
    canalPrincipal?: string;
    puedeAccederMailing?: boolean;
    puedeAccederTelefonia?: boolean;
    campaniasMailing?: number[];
    campaniasTelefonicas?: number[];
}

export interface LoginResponse {
    token: string;
    type: string;
    username: string;
    userId: number;
    roles: string[];
    agentId?: number;
    canalPrincipal?: string;
    puedeAccederMailing?: boolean;
    puedeAccederTelefonia?: boolean;
    campaniasMailing?: number[];
    campaniasTelefonicas?: number[];
    profile?: UserInfo;
}
