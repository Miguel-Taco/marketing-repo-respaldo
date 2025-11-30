import { useState, useEffect } from 'react';
import { useLocation, useParams } from 'react-router-dom';
import { telemarketingApi } from '../services/telemarketingApi';
import { BreadcrumbItem } from '../types';

interface UseBreadcrumbsReturn {
    breadcrumbs: BreadcrumbItem[] | null;
    loading: boolean;
    campaignName: string | null;
}

// Cache simple para evitar llamadas repetidas en la misma sesión
const campaignNameCache: Record<number, string> = {};

export const useBreadcrumbs = (): UseBreadcrumbsReturn => {
    const location = useLocation();
    const params = useParams<{ id: string }>();
    const [breadcrumbs, setBreadcrumbs] = useState<BreadcrumbItem[] | null>(null);
    const [loading, setLoading] = useState(false);
    const [campaignName, setCampaignName] = useState<string | null>(null);

    useEffect(() => {
        const generateBreadcrumbs = async () => {
            const path = location.pathname;

            // Si no estamos en una ruta de campaña específica, no mostramos breadcrumbs
            // o mostramos solo la raíz si es necesario (pero el requerimiento es para campañas específicas)
            if (!path.includes('/campanias/')) {
                setBreadcrumbs(null);
                return;
            }

            const campaignId = params.id ? Number(params.id) : null;

            if (!campaignId) {
                setBreadcrumbs(null);
                return;
            }

            setLoading(true);

            try {
                // Obtener nombre de campaña (del cache o API)
                let name = campaignNameCache[campaignId];

                if (!name) {
                    try {
                        const campaign = await telemarketingApi.getCampaniaById(campaignId);
                        name = campaign.nombre;
                        campaignNameCache[campaignId] = name;
                    } catch (error) {
                        console.error('Error fetching campaign name:', error);
                        name = `Campaña #${campaignId}`;
                    }
                }

                setCampaignName(name);

                // Base breadcrumbs
                const items: BreadcrumbItem[] = [
                    {
                        label: 'Campañas Telefónicas',
                        path: '/marketing/campanas/telefonicas'
                    }
                ];

                // Determinar la sección actual
                if (path.includes('/cola')) {
                    items.push({
                        label: name,
                        path: `/marketing/campanas/telefonicas/campanias/${campaignId}/cola`
                    });
                    items.push({ label: 'Cola de Llamadas' });
                } else if (path.includes('/leads')) {
                    items.push({
                        label: name,
                        path: `/marketing/campanas/telefonicas/campanias/${campaignId}/cola`
                    });
                    items.push({ label: 'Leads' });
                } else if (path.includes('/historial')) {
                    items.push({
                        label: name,
                        path: `/marketing/campanas/telefonicas/campanias/${campaignId}/cola`
                    });
                    items.push({ label: 'Historial' });
                } else if (path.includes('/metricas')) {
                    items.push({
                        label: name,
                        path: `/marketing/campanas/telefonicas/campanias/${campaignId}/cola`
                    });
                    items.push({ label: 'Métricas' });
                } else if (path.includes('/llamar/')) {
                    items.push({
                        label: name,
                        path: `/marketing/campanas/telefonicas/campanias/${campaignId}/cola`
                    });
                    items.push({ label: 'Llamada en curso' });
                } else {
                    // Default fallback
                    items.push({ label: name });
                }

                setBreadcrumbs(items);
            } catch (error) {
                console.error('Error generating breadcrumbs:', error);
                setBreadcrumbs(null);
            } finally {
                setLoading(false);
            }
        };

        generateBreadcrumbs();
    }, [location.pathname, params.id]);

    return { breadcrumbs, loading, campaignName };
};
