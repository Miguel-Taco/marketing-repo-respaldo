import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { UserInfo, LoginRequest } from '../types/auth.types';
import { authApi } from '../services/auth.api';

interface AuthContextType {
    user: UserInfo | null;
    isAuthenticated: boolean;
    isLoading: boolean;
    login: (credentials: LoginRequest) => Promise<void>;
    logout: () => void;
    hasRole: (role: string) => boolean;
    canAccessMailing: () => boolean;
    canAccessTelefonia: () => boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
    const [user, setUser] = useState<UserInfo | null>(null);
    const [isLoading, setIsLoading] = useState<boolean>(true);

    useEffect(() => {
        const initAuth = async () => {
            const token = localStorage.getItem('jwt_token');
            if (token) {
                try {
                    const userInfo = await authApi.getMe();
                    setUser(userInfo);
                } catch (error) {
                    console.error('Error al validar sesión:', error);
                    localStorage.removeItem('jwt_token');
                }
            }
            setIsLoading(false);
        };

        initAuth();
    }, []);

    const login = async (credentials: LoginRequest) => {
        try {
            const response = await authApi.login(credentials);
            localStorage.setItem('jwt_token', response.token);

            const profile: UserInfo = response.profile || {
                userId: response.userId,
                username: response.username,
                activo: true,
                roles: response.roles,
                agentId: response.agentId,
                canalPrincipal: response.canalPrincipal,
                puedeAccederMailing: response.puedeAccederMailing,
                puedeAccederTelefonia: response.puedeAccederTelefonia,
                campaniasMailing: response.campaniasMailing,
                campaniasTelefonicas: response.campaniasTelefonicas
            };

            // Actualizar estado del usuario
            setUser(profile);
        } catch (error) {
            throw error;
        }
    };

    const logout = () => {
        authApi.logout().catch(console.error); // Intentar logout en backend pero no bloquear
        localStorage.removeItem('jwt_token');
        setUser(null);
        window.location.href = '/login';
    };

    const hasRole = (role: string): boolean => {
        if (!user || !user.roles) return false;
        return user.roles.includes(role);
    };

    const canAccessMailing = (): boolean => {
        if (hasRole('ADMIN')) return true;
        return !!user?.puedeAccederMailing;
    };

    const canAccessTelefonia = (): boolean => {
        if (hasRole('ADMIN')) return true;
        return !!user?.puedeAccederTelefonia;
    };

    return (
        <AuthContext.Provider value={{
            user,
            isAuthenticated: !!user,
            isLoading,
            login,
            logout,
            hasRole,
            canAccessMailing,
            canAccessTelefonia
        }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (context === undefined) {
        throw new Error('useAuth debe ser usado dentro de un AuthProvider');
    }
    return context;
};
