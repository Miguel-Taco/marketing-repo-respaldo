import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

type ModuleFlag = 'MAILING' | 'TELEFONIA';

interface NavItem {
    icon: string;
    label: string;
    path: string;
    excludePath?: string;
    requiredRole?: string;
    requiredModule?: ModuleFlag;
}

const NAV_ITEMS: NavItem[] = [
    { icon: 'person_search', label: 'Leads', path: '/leads' },
    { icon: 'filter_alt', label: 'Segmentaci??n', path: '/marketing/segmentacion' },
    { icon: 'campaign', label: 'Campa??as', path: '/marketing/campanas', excludePath: '/marketing/campanas/mailing', requiredRole: 'ADMIN' },
    { icon: 'mail', label: 'Emailing', path: '/emailing', requiredModule: 'MAILING' },
    { icon: 'phone_in_talk', label: 'Tel?fono', path: '/marketing/campanas/telefonicas', requiredModule: 'TELEFONIA' },
    { icon: 'ballot', label: 'Encuestas', path: '/encuestas' },
];


export const Sidebar: React.FC = () => {
    const location = useLocation();
    const { logout, hasRole, canAccessMailing, canAccessTelefonia } = useAuth();

    const filteredItems = React.useMemo(() => {
        return NAV_ITEMS.filter(item => {
            if (item.requiredRole && !hasRole(item.requiredRole)) {
                return false;
            }
            if (item.requiredModule === 'MAILING' && !canAccessMailing()) {
                return false;
            }
            if (item.requiredModule === 'TELEFONIA' && !canAccessTelefonia()) {
                return false;
            }
            return true;
        });
    }, [hasRole, canAccessMailing, canAccessTelefonia]);

    const activeItem = React.useMemo(() => {
        const sortedItems = [...filteredItems].sort((a, b) => b.path.length - a.path.length);
        return sortedItems.find(item => {
            const isExcluded = item.excludePath && (location.pathname.startsWith(item.excludePath) || location.pathname === item.excludePath);
            if (isExcluded) return false;
            return location.pathname === item.path || location.pathname.startsWith(`${item.path}/`);
        });
    }, [location.pathname, filteredItems]);

    return (
        <nav className="fixed left-0 top-0 h-full w-20 bg-primary flex flex-col items-center py-6 space-y-4 z-50 shadow-xl">
            {/* Logo/Dashboard */}
            <Link
                to="/"
                className="text-white/80 hover:text-white hover:bg-white/10 p-3 rounded-lg transition-colors"
                title="Dashboard"
            >
                <span className="material-symbols-outlined text-3xl">apps</span>
            </Link>

            <div className="h-px w-12 bg-white/20 my-2" />

            {/* Navegación Principal */}
            <div className="flex flex-col space-y-3 w-full items-center">
                {filteredItems.map((item) => {
                    const isActive = activeItem?.path === item.path;
                    return (
                        <Link
                            key={item.path}
                            to={item.path}
                            title={item.label}
                            aria-label={item.label}
                            aria-current={isActive ? 'page' : undefined}
                            className={`
                w-12 h-12 flex items-center justify-center rounded-lg transition-all duration-200
                ${isActive
                                    ? 'bg-white text-primary shadow-lg scale-105'
                                    : 'text-white/80 hover:text-white hover:bg-white/10'}
              `}
                        >
                            <span className="material-symbols-outlined text-2xl">{item.icon}</span>
                        </Link>
                    );
                })}
            </div>

            {/* Separador inferior */}
            <div className="!mt-auto" />
            <div className="h-px w-12 bg-white/20 my-2" />

            {/* Acciones secundarias */}
            <button
                className="text-white/80 hover:text-white p-3 rounded-lg hover:bg-white/10 transition-colors"
                title="Ayuda"
                aria-label="Ayuda"
            >
                <span className="material-symbols-outlined text-2xl">help_outline</span>
            </button>
            <button
                onClick={logout}
                className="text-white/80 hover:text-white p-3 rounded-lg hover:bg-white/10 transition-colors"
                title="Cerrar sesión"
                aria-label="Cerrar sesión"
            >
                <span className="material-symbols-outlined text-2xl">logout</span>
            </button>
        </nav>
    );
};
