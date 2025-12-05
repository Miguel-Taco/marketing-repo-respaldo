import React, { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { telemarketingApi } from '../services/telemarketingApi';
import type { Contacto } from '../types';

export const ScheduledCallsNotification: React.FC = () => {
    const [scheduledCalls, setScheduledCalls] = useState<Contacto[]>([]);
    const [nextCall, setNextCall] = useState<Contacto | null>(null);
    const [showNotification, setShowNotification] = useState(false);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    // Load scheduled calls on mount
    useEffect(() => {
        const loadScheduledCalls = async () => {
            try {
                const calls = await telemarketingApi.getLlamadasProgramadas();
                setScheduledCalls(calls);
            } catch (error) {
                console.error('Error loading scheduled calls:', error);
            } finally {
                setLoading(false);
            }
        };

        loadScheduledCalls();
    }, []);

    // Find the next upcoming call
    useEffect(() => {
        if (!scheduledCalls || scheduledCalls.length === 0) {
            setNextCall(null);
            return;
        }

        const now = new Date();
        const upcoming = scheduledCalls
            .filter(call => {
                if (!call.fechaUltimaLlamada) return false;
                const scheduledTime = new Date(call.fechaUltimaLlamada);
                return scheduledTime > now;
            })
            .sort((a, b) => {
                const timeA = new Date(a.fechaUltimaLlamada!).getTime();
                const timeB = new Date(b.fechaUltimaLlamada!).getTime();
                return timeA - timeB;
            });

        setNextCall(upcoming[0] || null);
    }, [scheduledCalls]);

    // Set timer for next call
    useEffect(() => {
        if (!nextCall || !nextCall.fechaUltimaLlamada) return;

        const scheduledTime = new Date(nextCall.fechaUltimaLlamada);
        const now = new Date();
        const msUntilCall = scheduledTime.getTime() - now.getTime();

        // If the call is due now or in the past, show notification immediately
        if (msUntilCall <= 0) {
            setShowNotification(true);
            return;
        }

        // Set a timer to show notification when the time comes
        const timer = setTimeout(() => {
            setShowNotification(true);
        }, msUntilCall);

        return () => clearTimeout(timer);
    }, [nextCall]);

    const handleCall = useCallback(() => {
        if (!nextCall) return;

        // Navigate to call screen
        navigate(`/marketing/campanas/telefonicas/campanias/${nextCall.id}/llamar/${nextCall.idLead}`);

        // Close notification
        setShowNotification(false);
    }, [nextCall, navigate]);

    const handleDismiss = useCallback(() => {
        setShowNotification(false);
    }, []);

    if (loading || !showNotification || !nextCall) {
        return null;
    }

    return (
        <div className="fixed bottom-4 right-4 z-50 max-w-md bg-white rounded-lg shadow-2xl border-2 border-blue-500 p-4 animate-bounce">
            <div className="flex items-start gap-3">
                <div className="flex-shrink-0 w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center">
                    <span className="material-symbols-outlined text-blue-600 text-2xl">
                        phone_in_talk
                    </span>
                </div>
                <div className="flex-1">
                    <h3 className="font-semibold text-gray-900 mb-1">
                        ¡Llamada Programada!
                    </h3>
                    <p className="text-sm text-gray-600 mb-3">
                        Es hora de llamar a <strong>{nextCall.nombreCompleto}</strong>
                    </p>
                    <div className="flex gap-2">
                        <button
                            onClick={handleCall}
                            className="flex-1 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg font-medium transition-colors flex items-center justify-center gap-2"
                        >
                            <span className="material-symbols-outlined text-sm">call</span>
                            Llamar Ahora
                        </button>
                        <button
                            onClick={handleDismiss}
                            className="px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors"
                        >
                            Más tarde
                        </button>
                    </div>
                </div>
                <button
                    onClick={handleDismiss}
                    className="flex-shrink-0 text-gray-400 hover:text-gray-600"
                >
                    <span className="material-symbols-outlined">close</span>
                </button>
            </div>
        </div>
    );
};
