import React from 'react';
import { Outlet, useNavigate, useLocation, useParams } from 'react-router-dom';
import { Breadcrumbs } from '../../../../../shared/components/ui/Breadcrumbs';
import { useBreadcrumbs } from '../hooks/useBreadcrumbs';

export const TelemarketingLayout: React.FC = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const { breadcrumbs } = useBreadcrumbs();

    const { id } = useParams<{ id: string }>();

    const isActive = (path: string) => {
        if (path === `/marketing/campanas/telefonicas/campanias/${id}` && location.pathname === `/marketing/campanas/telefonicas/campanias/${id}`) return true;
        if (path !== `/marketing/campanas/telefonicas/campanias/${id}` && location.pathname.startsWith(path)) return true;
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
            {/* Breadcrumbs */}
            {breadcrumbs && breadcrumbs.length > 0 && (
                <div className="px-6 pt-6 pb-0">
                    <Breadcrumbs items={breadcrumbs} />
                </div>
            )}

            {/* Tabs de navegación - Solo visibles si hay un ID de campaña */}
            {id && (
                <nav className="mb-6 flex flex-wrap items-center gap-2 p-6 pb-0">
                    <button
                        onClick={() => navigate(`/marketing/campanas/telefonicas/campanias/${id}/cola`)}
                        className={getButtonClass(`/marketing/campanas/telefonicas/campanias/${id}/cola`)}
                    >
                        Cola actual
                    </button>
                    <button
                        onClick={() => navigate(`/marketing/campanas/telefonicas/campanias/${id}/leads`)}
                        className={getButtonClass(`/marketing/campanas/telefonicas/campanias/${id}/leads`)}
                    >
                        Leads
                    </button>
                    <button
                        onClick={() => navigate(`/marketing/campanas/telefonicas/campanias/${id}/historial`)}
                        className={getButtonClass(`/marketing/campanas/telefonicas/campanias/${id}/historial`)}
                    >
                        Historial
                    </button>
                    <button
                        onClick={() => navigate(`/marketing/campanas/telefonicas/campanias/${id}/metricas`)}
                        className={getButtonClass(`/marketing/campanas/telefonicas/campanias/${id}/metricas`)}
                    >
                        Métricas
                    </button>
                    <button
                        onClick={() => navigate(`/marketing/campanas/telefonicas/campanias/${id}/guiones`)}
                        className={getButtonClass(`/marketing/campanas/telefonicas/campanias/${id}/guiones`)}
                    >
                        Guiones
                    </button>
                </nav>
            )}

            {/* Contenido de la página */}
            <div className="flex-1 overflow-hidden">
                <Outlet />
            </div>
        </div>
    );
};
