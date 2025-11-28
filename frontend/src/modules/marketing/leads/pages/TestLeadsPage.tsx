import React, { useState } from 'react';
import { useLeads } from '../hooks/useLeads';
import { useLeadMutations } from '../hooks/useLeadMutations';

export const TestLeadsPage = () => {
    const { leads, loading, error, refresh } = useLeads();
    const { createLead, cualificarLead } = useLeadMutations();
    const [log, setLog] = useState<string>('');

    const handleCrearPrueba = async () => {
        setLog('Enviando lead de prueba...');
        const resultado = await createLead({
            nombreCompleto: "Usuario Test Frontend",
            origen: "WEB_TEST_REACT",
            contacto: {
                email: `test.${Date.now()}@frontend.com`, // Email único
                telefono: "999000111",
                distritoId: "150101"
            },
            tracking: { source: "react_test" }
        });

        if (resultado.success) {
            setLog('✅ Lead creado con éxito. Refrescando lista...');
            refresh(); // Recargar la lista para ver el nuevo
        } else {
            setLog('❌ Error creando: ' + resultado.error);
        }
    };

    return (
        <div className="p-10 space-y-5">
            <h1 className="text-2xl font-bold">Prueba Técnica de Conexión</h1>

            {/* Zona de Control */}
            <div className="p-4 border rounded bg-gray-100 space-x-4">
                <button
                    onClick={refresh}
                    className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
                >
                    🔄 Refrescar Lista
                </button>

                <button
                    onClick={handleCrearPrueba}
                    className="px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600"
                >
                    ➕ Crear Lead Dummy
                </button>
            </div>

            {/* Zona de Logs */}
            {log && <div className="p-2 bg-yellow-100 text-yellow-800 rounded">{log}</div>}
            {error && <div className="p-2 bg-red-100 text-red-800 rounded">Error de Red: {error}</div>}

            {/* Zona de Datos (JSON Crudo) */}
            {loading ? (
                <p>Cargando datos del backend...</p>
            ) : (
                <div className="border rounded p-4">
                    <h3 className="font-bold mb-2">Leads recibidos ({leads.length}):</h3>
                    <pre className="bg-gray-800 text-green-400 p-4 rounded overflow-auto max-h-96 text-xs">
                        {JSON.stringify(leads, null, 2)}
                    </pre>
                </div>
            )}
        </div>
    );
};
