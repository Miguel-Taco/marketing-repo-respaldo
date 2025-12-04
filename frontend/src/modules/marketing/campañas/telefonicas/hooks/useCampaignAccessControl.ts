import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { telemarketingApi } from '../services/telemarketingApi';

/**
 * Hook to check if a campaign can be accessed based on its status.
 * Redirects to the main campaigns page if the campaign is not active.
 */
export const useCampaignAccessControl = (campaignId: string | undefined) => {
    const navigate = useNavigate();
    const [isChecking, setIsChecking] = useState(true);
    const [canAccess, setCanAccess] = useState(false);

    useEffect(() => {
        if (!campaignId) {
            setIsChecking(false);
            setCanAccess(false);
            return;
        }

        const checkAccess = async () => {
            try {
                const campaign = await telemarketingApi.getCampaniaById(Number(campaignId));

                // Only allow access to active campaigns
                const isActive = campaign.estado === 'Vigente' || campaign.estado === 'ACTIVA';

                if (!isActive) {
                    navigate('/marketing/campanas/telefonicas', { replace: true });
                    setCanAccess(false);
                } else {
                    setCanAccess(true);
                }
            } catch (error) {
                console.error('Error checking campaign access:', error);
                navigate('/marketing/campanas/telefonicas', { replace: true });
                setCanAccess(false);
            } finally {
                setIsChecking(false);
            }
        };

        checkAccess();
    }, [campaignId, navigate]);

    return { isChecking, canAccess };
};
