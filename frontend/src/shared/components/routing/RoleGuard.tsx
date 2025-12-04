import React from 'react';
import { useAuth } from '../../../context/AuthContext';

interface RoleGuardProps {
    children: React.ReactNode;
    requiredRole: string;
    fallback?: React.ReactNode;
}

export const RoleGuard: React.FC<RoleGuardProps> = ({ children, requiredRole, fallback = null }) => {
    const { hasRole } = useAuth();

    if (!hasRole(requiredRole)) {
        return <>{fallback}</>;
    }

    return <>{children}</>;
};
