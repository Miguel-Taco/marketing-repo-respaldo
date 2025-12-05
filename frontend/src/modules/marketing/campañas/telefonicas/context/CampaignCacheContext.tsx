import React, { createContext, useContext, useState, useCallback, ReactNode, useEffect, useRef } from 'react';
import { telemarketingApi } from '../services/telemarketingApi';
import { createCacheLogger } from '../../../../../shared/utils/cacheLogger';
import type {
    CampaniaTelefonica,
    Contacto,
    Llamada,
    MetricasDiarias,
    MetricasCampania,
    GuionArchivo,
    Guion
} from '../types';

// Initialize cache logger for this module
const logger = createCacheLogger('CampaignCache', true);

// Tipos de datos que se pueden cachear
type CacheDataType = 'campaign' | 'queue' | 'leads' | 'history' | 'dailyMetrics' | 'campaignMetrics' | 'scripts' | 'guion' | 'scheduledCalls';

// Estructura del caché por campaña
interface CampaignCacheData {
    campaign?: CampaniaTelefonica | null;
    queue?: Contacto[] | null;
    leads?: Contacto[] | null;
    history?: Llamada[] | null;
    dailyMetrics?: MetricasDiarias | null;
    campaignMetrics?: MetricasCampania | null;
    scripts?: GuionArchivo[] | null;
    guion?: Guion | null;
    scheduledCalls?: Contacto[] | null;
    lastUpdated: {
        campaign?: Date;
        queue?: Date;
        leads?: Date;
        history?: Date;
        dailyMetrics?: Date;
        campaignMetrics?: Date;
        scripts?: Date;
        guion?: Date;
        scheduledCalls?: Date;
    };
}

// Caché global: campaignId -> datos
interface CacheState {
    [campaignId: number]: CampaignCacheData;
}

interface CampaignCacheContextProps {
    // Obtener datos del caché (carga si no existe)
    getCachedData: <T>(campaignId: number, dataType: CacheDataType) => Promise<T | undefined>;

    // Invalidar tipos específicos de datos
    invalidateCache: (campaignId: number, dataTypes: CacheDataType[]) => void;

    // Invalidar toda la campaña
    invalidateAll: (campaignId: number) => void;

    // Precargar todos los datos de una campaña
    preloadCampaignData: (campaignId: number) => Promise<void>;

    // Verificar si hay datos en caché
    isCached: (campaignId: number, dataType: CacheDataType) => boolean;

    // Estado de carga
    isLoading: (campaignId: number, dataType: CacheDataType) => boolean;
}

const CampaignCacheContext = createContext<CampaignCacheContextProps | undefined>(undefined);

export const CampaignCacheProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
    const [cache, setCache] = useState<CacheState>({});
    const [loadingStates, setLoadingStates] = useState<{ [key: string]: boolean }>({});
    const loadingStatesRef = useRef(loadingStates);

    // Usar ref para acceder al estado actual del caché sin causar re-renders
    const cacheRef = useRef(cache);
    useEffect(() => {
        cacheRef.current = cache;
    }, [cache]);

    // Mantener loadingStatesRef sincronizado
    useEffect(() => {
        loadingStatesRef.current = loadingStates;
    }, [loadingStates]);

    // Inicializar caché para una campaña si no existe
    const initCampaignCache = useCallback((campaignId: number) => {
        setCache(prev => {
            if (prev[campaignId]) return prev;
            return {
                ...prev,
                [campaignId]: {
                    lastUpdated: {}
                }
            };
        });
    }, []);

    // Marcar como cargando
    const setLoading = useCallback((campaignId: number, dataType: CacheDataType, loading: boolean) => {
        const key = `${campaignId}-${dataType}`;
        setLoadingStates(prev => ({ ...prev, [key]: loading }));
    }, []);

    // Verificar si está cargando
    const isLoading = useCallback((campaignId: number, dataType: CacheDataType): boolean => {
        const key = `${campaignId}-${dataType}`;
        return loadingStatesRef.current[key] || false;
    }, []);

    // Verificar si hay datos en caché
    const isCached = useCallback((campaignId: number, dataType: CacheDataType): boolean => {
        return cacheRef.current[campaignId]?.[dataType] !== undefined;
    }, []);

    // Obtener datos del caché o cargarlos
    const getCachedData = useCallback(async <T,>(
        campaignId: number,
        dataType: CacheDataType
    ): Promise<T | undefined> => {
        initCampaignCache(campaignId);

        // Si ya está en caché (incluso si es null), retornar
        // Usamos !== undefined para distinguir entre "no en caché" y "en caché pero vacío (null)"
        if (cacheRef.current[campaignId]?.[dataType] !== undefined) {
            const cachedValue = cacheRef.current[campaignId][dataType];
            if (cachedValue) {
                logger.hit(`${dataType} for campaign ${campaignId}`);
            } else {
                // Si es null, es un "hit" negativo (sabemos que no existe)
                logger.hit(`${dataType} (empty) for campaign ${campaignId}`);
            }
            return cachedValue as T;
        }

        // Si ya se está cargando, esperar
        const key = `${campaignId}-${dataType}`;
        if (loadingStatesRef.current[key]) {
            logger.waiting(`${dataType} for campaign ${campaignId}`);
            return new Promise((resolve) => {
                const checkInterval = setInterval(() => {
                    if (!loadingStatesRef.current[key]) {
                        clearInterval(checkInterval);
                        resolve(cacheRef.current[campaignId]?.[dataType] as T);
                    }
                }, 100);
            });
        }

        // Cargar datos
        logger.miss(`${dataType} for campaign ${campaignId}`);
        setLoading(campaignId, dataType, true);
        try {
            let data: any;

            switch (dataType) {
                case 'campaign':
                    data = await telemarketingApi.getCampaniaById(campaignId);
                    break;
                case 'queue':
                    data = await telemarketingApi.getCola(campaignId);
                    break;
                case 'leads':
                    data = await telemarketingApi.getContactosCampania(campaignId);
                    break;
                case 'history':
                    data = await telemarketingApi.getHistorialLlamadas(campaignId);
                    break;
                case 'dailyMetrics':
                    data = await telemarketingApi.getMetricasDiarias(campaignId);
                    break;
                case 'campaignMetrics':
                    data = await telemarketingApi.getMetricasCampaniaCompletas(campaignId);
                    break;
                case 'scripts':
                    data = await telemarketingApi.getScriptsGenerales(campaignId);
                    break;
                case 'guion':
                    data = await telemarketingApi.getGuion(campaignId);
                    break;
                case 'scheduledCalls':
                    data = await telemarketingApi.getLlamadasProgramadas();
                    break;
            }

            // Guardar en caché
            setCache(prev => ({
                ...prev,
                [campaignId]: {
                    ...prev[campaignId],
                    [dataType]: data,
                    lastUpdated: {
                        ...prev[campaignId]?.lastUpdated,
                        [dataType]: new Date()
                    }
                }
            }));

            logger.stored(`${dataType} for campaign ${campaignId}`);
            return data as T;
        } catch (error: any) {
            console.error(`Error loading ${dataType} for campaign ${campaignId}:`, error);

            // Si es un error 404 (Not Found), guardamos null en caché para evitar reintentos infinitos
            if (error.response?.status === 404 || error.status === 404) {
                setCache(prev => ({
                    ...prev,
                    [campaignId]: {
                        ...prev[campaignId],
                        [dataType]: null, // Guardamos null explícitamente
                        lastUpdated: {
                            ...prev[campaignId]?.lastUpdated,
                            [dataType]: new Date()
                        }
                    }
                }));
                logger.stored(`${dataType} (null/404) for campaign ${campaignId}`);
            }

            return undefined;
        } finally {
            setLoading(campaignId, dataType, false);
        }
    }, [initCampaignCache, setLoading]);

    // Invalidar tipos específicos de datos
    const invalidateCache = useCallback((campaignId: number, dataTypes: CacheDataType[]) => {
        logger.invalidate(dataTypes, `for campaign ${campaignId}`);
        setCache(prev => {
            if (!prev[campaignId]) return prev;

            const updated = { ...prev[campaignId] };
            const updatedLastUpdated = { ...updated.lastUpdated };

            dataTypes.forEach(dataType => {
                delete updated[dataType];
                delete updatedLastUpdated[dataType];
            });

            return {
                ...prev,
                [campaignId]: {
                    ...updated,
                    lastUpdated: updatedLastUpdated
                }
            };
        });
    }, []);

    // Invalidar toda la campaña
    const invalidateAll = useCallback((campaignId: number) => {
        logger.clear(`campaign ${campaignId}`);
        setCache(prev => {
            const newCache = { ...prev };
            delete newCache[campaignId];
            return newCache;
        });
    }, []);

    // Precargar todos los datos de una campaña
    const preloadCampaignData = useCallback(async (campaignId: number) => {
        // Precargar datos de todas las pestañas en segundo plano
        const dataTypesToPreload: CacheDataType[] = [
            'campaign',        // Datos generales de la campaña
            'queue',           // Cola de llamadas
            'leads',           // Leads de la campaña
            'history',         // Historial de llamadas
            'dailyMetrics',    // Métricas diarias
            'campaignMetrics', // Métricas de campaña
            'scripts',         // Scripts disponibles
            'guion'            // Guion vinculado
        ];

        // Cargar todos en paralelo sin bloquear
        Promise.all(
            dataTypesToPreload.map(dataType =>
                getCachedData(campaignId, dataType).catch(err => {
                    // Log error pero no bloquear otras cargas
                    console.error(`Error preloading ${dataType}:`, err);
                    return undefined;
                })
            )
        ).then(() => {
            logger.log('STORED', `Precarga completada para campaña ${campaignId}`);
        });
    }, [getCachedData]);

    return (
        <CampaignCacheContext.Provider
            value={{
                getCachedData,
                invalidateCache,
                invalidateAll,
                preloadCampaignData,
                isCached,
                isLoading
            }}
        >
            {children}
        </CampaignCacheContext.Provider>
    );
};

export const useCampaignCache = () => {
    const context = useContext(CampaignCacheContext);
    if (!context) {
        throw new Error('useCampaignCache must be used within a CampaignCacheProvider');
    }
    return context;
};

// Hook personalizado para obtener datos de un tipo específico
export const useCachedCampaignData = <T,>(
    campaignId: number | undefined,
    dataType: CacheDataType
): { data: T | undefined; loading: boolean; refresh: () => Promise<void> } => {
    const { getCachedData, invalidateCache, isLoading } = useCampaignCache();
    const [data, setData] = useState<T | undefined>();
    const [loading, setLoading] = useState(true);

    // Usar ref para evitar recrear loadData en cada render
    const campaignIdRef = useRef(campaignId);
    const dataTypeRef = useRef(dataType);

    useEffect(() => {
        campaignIdRef.current = campaignId;
        dataTypeRef.current = dataType;
    }, [campaignId, dataType]);

    const loadData = useCallback(async () => {
        const currentCampaignId = campaignIdRef.current;
        const currentDataType = dataTypeRef.current;

        if (!currentCampaignId) {
            setLoading(false);
            return;
        }

        setLoading(true);
        const result = await getCachedData<T>(currentCampaignId, currentDataType);
        // Normalizar null a undefined para evitar problemas en componentes que esperan undefined como valor por defecto
        setData(result === null ? undefined : result);
        setLoading(false);
    }, [getCachedData]);

    const refresh = useCallback(async () => {
        if (!campaignId) return;
        invalidateCache(campaignId, [dataType]);
        await loadData();
    }, [campaignId, dataType, invalidateCache, loadData]);

    useEffect(() => {
        loadData();
    }, [campaignId, dataType]); // Solo recargar cuando cambien campaignId o dataType

    return {
        data,
        loading: loading || (campaignId ? isLoading(campaignId, dataType) : false),
        refresh
    };
};
