import { useState, useEffect, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export interface ImportProgressUpdate {
    loteId: number;
    nombreArchivo: string;
    totalRegistros: number;
    procesados: number;
    exitosos: number;
    duplicados: number;
    conErrores: number;
    completado: boolean;
}

interface UseImportProgressProps {
    loteId: number | null;
    enabled: boolean;
}

export const useImportProgress = ({ loteId, enabled }: UseImportProgressProps) => {
    const [progress, setProgress] = useState<ImportProgressUpdate | null>(null);
    const [connected, setConnected] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const clientRef = useRef<Client | null>(null);

    useEffect(() => {
        if (!enabled || !loteId) {
            return;
        }

        // Crear cliente STOMP con SockJS
        // WebSocket está en /ws (raíz del servidor), no en /api/v1/ws
        const wsUrl = import.meta.env.VITE_API_URL?.replace('/api/v1', '') || 'http://localhost:8080';
        const socket = new SockJS(wsUrl + '/ws');
        const stompClient = new Client({
            webSocketFactory: () => socket as any,
            debug: (str: string) => console.log('[STOMP]', str),
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
        });

        stompClient.onConnect = () => {
            console.log('✅ WebSocket conectado');
            setConnected(true);
            setError(null);

            // Suscribirse al topic de progreso específico del lote
            stompClient.subscribe(`/topic/import-progress/${loteId}`, (message: any) => {
                try {
                    const update: ImportProgressUpdate = JSON.parse(message.body);
                    console.log('📨 Progreso recibido:', update);
                    setProgress(update);
                } catch (err) {
                    console.error('Error parseando mensaje:', err);
                    setError('Error al procesar actualización');
                }
            });
        };

        stompClient.onStompError = (frame: any) => {
            console.error('❌ Error STOMP:', frame);
            setError('Error de conexión WebSocket');
            setConnected(false);
        };

        stompClient.onWebSocketClose = () => {
            console.log('🔌 WebSocket desconectado');
            setConnected(false);
        };

        stompClient.activate();
        clientRef.current = stompClient;

        // Cleanup al desmontar
        return () => {
            if (clientRef.current) {
                console.log('🧹 Limpiando conexión WebSocket');
                clientRef.current.deactivate();
            }
        };
    }, [loteId, enabled]);

    return { progress, connected, error };
};
