import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

type ModuleFlag = 'MAILING' | 'TELEFONIA';

interface ProtectedRouteProps {
    children: React.ReactNode;
    requiredRole?: string;
    requiredModule?: ModuleFlag;
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children, requiredRole, requiredModule }) => {
    const { isAuthenticated, isLoading, hasRole, canAccessMailing, canAccessTelefonia } = useAuth();
    const location = useLocation();

    if (isLoading) {
        return <div className="flex items-center justify-center h-screen">Cargando...</div>;
    }

    if (!isAuthenticated) {
        return <Navigate to="/login" state={{ from: location }} replace />;
    }

    if (requiredRole && !hasRole(requiredRole)) {
        return <Navigate to="/leads" replace />;
    }

    if (requiredModule) {
        const hasModuleAccess = requiredModule === 'MAILING'
            ? canAccessMailing()
            : canAccessTelefonia();

        if (!hasModuleAccess) {
            return <Navigate to="/leads" replace />;
        }
    }

    return <>{children}</>;
};
