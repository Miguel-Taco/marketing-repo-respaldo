import React, { createContext, useContext, useState, useCallback, ReactNode, useEffect } from 'react';
import { CampanaMailing, MetricasMailing } from '../types/mailing.types';
import { mailingApi } from '../services/mailing.api';

interface CampanasCacheState {
    [key: string]: CampanaMailing[];
}

interface MailingContextType {
    // Cache de campañas
    campanasCache: CampanasCacheState;
    metricsCache: Map<number, MetricasMailing>;
    
    // Estado de carga
    initialLoadingComplete: boolean;
    loading: boolean;
    loadingMetrics: boolean;
    error: string | null;
    
    // Métodos
    listarCampanas: (estado: string) => Promise<CampanaMailing[]>;
    obtenerDetalle: (id: number) => Promise<CampanaMailing>;
    obtenerMetricas: (id: number) => Promise<MetricasMailing>;
    invalidarCampanasCache: () => void;
    invalidarMetricasCache: (id?: number) => void;
}

const MailingContext = createContext<MailingContextType | undefined>(undefined);

export const MailingProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
    const [campanasCache, setCompanasCache] = useState<CampanasCacheState>({});
    const [metricsCache, setMetricsCache] = useState<Map<number, MetricasMailing>>(new Map());
    const [loading, setLoading] = useState(false);
    const [loadingMetrics, setLoadingMetrics] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [initialLoadingComplete, setInitialLoadingComplete] = useState(false);

    /**
     * Listar campañas - DEVUELVE CACHÉ INMEDIATAMENTE + ACTUALIZA EN BACKGROUND
     */
    const listarCampanas = useCallback(async (estado: string): Promise<CampanaMailing[]> => {
        // 1. SI ESTÁ EN CACHÉ Y TIENE DATOS → DEVOLVER INMEDIATAMENTE
        if (campanasCache[estado] && campanasCache[estado].length > 0) {
            console.log(`📦 [MAILING] Devolviendo "${estado}" del caché (${campanasCache[estado].length} items)`);
            
            // 2. ACTUALIZAR EN BACKGROUND (sin esperar, sin bloquear)
            actualizarCampanasEnBackground(estado);
            
            // 3. DEVOLVER DATOS DEL CACHÉ INMEDIATAMENTE
            return campanasCache[estado];
        }

        // SI NO HAY CACHÉ → HACER PETICIÓN SÍNCRONA
        console.log(`🔄 [MAILING] Cargando "${estado}" desde servidor...`);
        setLoading(true);
        try {
            const data = await mailingApi.listarCampanas(estado);
            
            // Guardar en caché
            setCompanasCache(prev => ({
                ...prev,
                [estado]: data
            }));

            setError(null);
            console.log(`✅ [MAILING] "${estado}" cargado y cacheado (${data.length} items)`);
            return data;
        } catch (err: any) {
            const errorMsg = err.message || 'Error al cargar campañas';
            setError(errorMsg);
            console.error(`❌ [MAILING] Error:`, errorMsg);
            return [];
        } finally {
            setLoading(false);
        }
    }, [campanasCache]);

    /**
     * Actualizar en background (sin bloquear UI)
     */
    const actualizarCampanasEnBackground = useCallback(async (estado: string) => {
        try {
            const data = await mailingApi.listarCampanas(estado);
            
            // Actualizar caché silenciosamente
            setCompanasCache(prev => {
                const prevData = prev[estado] || [];
                // Solo actualizar si cambió
                if (JSON.stringify(prevData) !== JSON.stringify(data)) {
                    console.log(`🔄 [MAILING] "${estado}" actualizado en background (${data.length} items)`);
                    return { ...prev, [estado]: data };
                }
                return prev;
            });
        } catch (err) {
            console.error(`❌ [MAILING] Error actualizando "${estado}":`, err);
        }
    }, []);

    /**
     * Obtener detalle de campaña
     */
    const obtenerDetalle = useCallback(async (id: number): Promise<CampanaMailing> => {
        try {
            const data = await mailingApi.obtenerDetalle(id);
            return data;
        } catch (err: any) {
            setError(err.message);
            throw err;
        }
    }, []);

    /**
     * Obtener métricas - DEVUELVE CACHÉ INMEDIATAMENTE + ACTUALIZA EN BACKGROUND
     */
    const obtenerMetricas = useCallback(async (id: number): Promise<MetricasMailing> => {
        // 1. SI ESTÁ EN CACHÉ → DEVOLVER INMEDIATAMENTE
        const cached = metricsCache.get(id);
        if (cached) {
            console.log(`📦 [MÉTRICAS] Devolviendo campaña ${id} del caché`);
            
            // 2. ACTUALIZAR EN BACKGROUND
            actualizarMetricasEnBackground(id);
            
            return cached;
        }

        // SI NO HAY CACHÉ → HACER PETICIÓN SÍNCRONA
        console.log(`🔄 [MÉTRICAS] Cargando campaña ${id}...`);
        setLoadingMetrics(true);
        try {
            const data = await mailingApi.obtenerMetricas(id);
            
            // Guardar en caché
            setMetricsCache(prev => new Map(prev).set(id, data));
            
            console.log(`✅ [MÉTRICAS] Campaña ${id} cacheada`);
            return data;
        } catch (err: any) {
            setError(err.message);
            console.error(`❌ [MÉTRICAS] Error:`, err.message);
            throw err;
        } finally {
            setLoadingMetrics(false);
        }
    }, [metricsCache]);

    /**
     * Actualizar métricas en background
     */
    const actualizarMetricasEnBackground = useCallback(async (id: number) => {
        try {
            const data = await mailingApi.obtenerMetricas(id);
            
            // Actualizar caché
            setMetricsCache(prev => new Map(prev).set(id, data));
            
            console.log(`✅ [MÉTRICAS] Campaña ${id} actualizada en background`);
        } catch (err) {
            console.error(`❌ [MÉTRICAS] Error actualizando ${id}:`, err);
        }
    }, []);

    /**
     * Invalidar caché de campañas (al crear/editar)
     */
    const invalidarCampanasCache = useCallback(() => {
        console.log('🗑️ [MAILING] Limpiando caché de campañas');
        setCompanasCache({});
    }, []);

    /**
     * Invalidar caché de métricas
     */
    const invalidarMetricasCache = useCallback((id?: number) => {
        if (id) {
            console.log(`🗑️ [MÉTRICAS] Limpiando caché de campaña ${id}`);
            setMetricsCache(prev => {
                const newMap = new Map(prev);
                newMap.delete(id);
                return newMap;
            });
        } else {
            console.log('🗑️ [MÉTRICAS] Limpiando caché de todas las métricas');
            setMetricsCache(new Map());
        }
    }, []);

    /**
     * ✅ CARGAR DATOS AUTOMÁTICAMENTE AL INICIALIZAR EL PROVIDER
     * Se ejecuta UNA SOLA VEZ al montar el provider
     */
    useEffect(() => {
        const initializeCache = async () => {
            console.log('🚀 [MAILING] Inicializando caché de campañas...');
            try {
                // Cargar todos los estados en paralelo
                const [pendientes, listos, enviados, finalizados] = await Promise.all([
                    mailingApi.listarCampanas('pendiente'),
                    mailingApi.listarCampanas('listo'),
                    mailingApi.listarCampanas('enviado'),
                    mailingApi.listarCampanas('finalizado')
                ]);

                // Guardar todo en caché
                setCompanasCache({
                    pendiente: pendientes,
                    listo: listos,
                    enviado: enviados,
                    finalizado: finalizados
                });

                console.log(`✅ [MAILING] Caché inicializado: ${pendientes.length} pendientes, ${listos.length} listos, ${enviados.length} enviados, ${finalizados.length} finalizados`);

                // Cargar métricas para campañas enviadas y finalizadas
                const allSentAndFinalized = [...enviados, ...finalizados];
                for (const campana of allSentAndFinalized) {
                    try {
                        const metricas = await mailingApi.obtenerMetricas(campana.id);
                        setMetricsCache(prev => new Map(prev).set(campana.id, metricas));
                    } catch (err) {
                        console.error(`Error cargando métricas ${campana.id}:`, err);
                    }
                }

                console.log(`✅ [MÉTRICAS] Caché de métricas inicializado`);

            } catch (err) {
                console.error('❌ [MAILING] Error inicializando caché:', err);
                setError('Error al cargar datos de campañas');
            } finally {
                setInitialLoadingComplete(true);
            }
        };

        initializeCache();
    }, []); // ✅ Solo se ejecuta una vez al montar

    return (
        <MailingContext.Provider
            value={{
                campanasCache,
                metricsCache,
                initialLoadingComplete,
                loading,
                loadingMetrics,
                error,
                listarCampanas,
                obtenerDetalle,
                obtenerMetricas,
                invalidarCampanasCache,
                invalidarMetricasCache
            }}
        >
            {children}
        </MailingContext.Provider>
    );
};

export const useMailing= () => {
    const context = useContext(MailingContext);
    if (!context) {
        throw new Error('useMailingContext debe usarse dentro de MailingProvider');
    }
    return context;
};