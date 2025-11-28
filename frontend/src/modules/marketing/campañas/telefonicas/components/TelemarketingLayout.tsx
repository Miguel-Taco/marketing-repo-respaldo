import React from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';

export const TelemarketingLayout: React.FC = () => {
    const navigate = useNavigate();
    const location = useLocation();

    const isActive = (path: string) => {
        if (path === '/marketing/campanas/telefonicas' && location.pathname === '/marketing/campanas/telefonicas') return true;
        if (path !== '/marketing/campanas/telefonicas' && location.pathname.startsWith(path)) return true;
        return false;
    };

    const getButtonClass = (path: string) => {
        const baseClass = "rounded-full px-5 py-2 text-sm font-medium transition-colors";
        if (isActive(path)) {
            return `${baseClass} bg-primary text-white`;
        }
        return `${baseClass} border border-gray-300 bg-transparent text-gray-700 hover:bg-gray-50`;
    };

    return (
        <div className="flex flex-col h-full">
            {/* Tabs de navegación */}
            <nav className="mb-6 flex flex-wrap items-center gap-2 p-6 pb-0">
                <button
                    onClick={() => navigate('/marketing/campanas/telefonicas')}
                    className={getButtonClass('/marketing/campanas/telefonicas')}
                >
                    Campañas
                </button>
                <button
                    onClick={() => navigate('/marketing/campanas/telefonicas/leads')}
                    className={getButtonClass('/marketing/campanas/telefonicas/leads')}
                >
                    Leads
                </button>
                <button
                    onClick={() => navigate('/marketing/campanas/telefonicas/cola')}
                    className={getButtonClass('/marketing/campanas/telefonicas/cola')}
                >
                    Cola actual
                </button>
                <button
                    onClick={() => navigate('/marketing/campanas/telefonicas/historial')}
                    className={getButtonClass('/marketing/campanas/telefonicas/historial')}
                >
                    Historial
                </button>
                <button
                    onClick={() => navigate('/marketing/campanas/telefonicas/metricas')}
                    className={getButtonClass('/marketing/campanas/telefonicas/metricas')}
                >
                    Métricas
                </button>
                <button
                    onClick={() => navigate('/marketing/campanas/telefonicas/guiones')}
                    className={getButtonClass('/marketing/campanas/telefonicas/guiones')}
                >
                    Guiones
                </button>
            </nav>

            {/* Contenido de la página */}
            <div className="flex-1 overflow-hidden">
                <Outlet />
            </div>
        </div>
    );
};
