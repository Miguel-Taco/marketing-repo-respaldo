import React, { useState } from 'react';
import { Button } from '../../../../shared/components/ui/Button';
import { CreateLeadDTO } from '../types/lead.types';
import { useUbigeo } from '../../../../shared/hooks/useUbigeo';

interface Props {
    isLoading: boolean;
    onSubmit: (data: CreateLeadDTO) => void;
    onCancel: () => void;
}

export const LeadForm: React.FC<Props> = ({ isLoading, onSubmit, onCancel }) => {
    // --- HOOK DE UBIGEO (Conectado al Backend) ---
    const { departamentos, provincias, distritos, loadProvincias, loadDistritos, loadingUbi } = useUbigeo();

    // Estados para controlar los selects padres (no se envían al backend)
    const [selectedDep, setSelectedDep] = useState('');
    const [selectedProv, setSelectedProv] = useState('');

    // Estado del formulario
    const [formData, setFormData] = useState({
        nombreCompleto: '',
        email: '',
        telefono: '',
        edad: '',
        genero: '',
        distritoId: '', // Este es el único dato de ubicación que le importa al Backend
        origen: 'FORMULARIO_INTERNO'
    });

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    // --- LÓGICA DE CASCADA ---
    const handleDepChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        const depId = e.target.value;
        setSelectedDep(depId);
        setSelectedProv(''); // Reset provincia
        setFormData(prev => ({ ...prev, distritoId: '' })); // Reset distrito

        loadProvincias(depId); // Llamada a API
    };

    const handleProvChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        const provId = e.target.value;
        setSelectedProv(provId);
        setFormData(prev => ({ ...prev, distritoId: '' })); // Reset distrito

        loadDistritos(provId); // Llamada a API
    };

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();

        // Validación simple
        if (!formData.distritoId) {
            alert("Por favor selecciona un distrito");
            return;
        }

        const payload: CreateLeadDTO = {
            nombreCompleto: formData.nombreCompleto,
            origen: formData.origen,
            contacto: {
                email: formData.email,
                telefono: formData.telefono,
                distritoId: formData.distritoId
            },
            demograficos: {
                edad: formData.edad ? parseInt(formData.edad) : undefined,
                genero: formData.genero || undefined
            },
            tracking: {
                source: "CRM_INTERNO",
                medium: "AGENTE"
            }
        };

        onSubmit(payload);
    };

    const inputClasses = "w-full border border-separator rounded-lg px-3 py-2 focus:ring-2 focus:ring-primary focus:outline-none transition-all bg-white disabled:bg-gray-100 disabled:text-gray-400";
    const labelClasses = "block text-sm font-medium mb-2 text-gray-700";

    return (
        <form onSubmit={handleSubmit} className="bg-white rounded-lg shadow-card border border-separator divide-y divide-separator">

            {/* SECCIÓN 1: Información Principal */}
            <div className="p-6">
                <h3 className="text-lg font-bold text-dark mb-4">Información Principal</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div className="md:col-span-2">
                        <label className={labelClasses}>Nombre Completo <span className="text-red-500">*</span></label>
                        <input name="nombreCompleto" required type="text" className={inputClasses} value={formData.nombreCompleto} onChange={handleChange} placeholder="Ej: Ana Torres" />
                    </div>
                    <div>
                        <label className={labelClasses}>Email <span className="text-red-500">*</span></label>
                        <input name="email" required type="email" className={inputClasses} value={formData.email} onChange={handleChange} />
                    </div>
                    <div>
                        <label className={labelClasses}>Teléfono</label>
                        <input name="telefono" type="tel" className={inputClasses} value={formData.telefono} onChange={handleChange} />
                    </div>
                </div>
            </div>

            {/* SECCIÓN 2: Datos Demográficos y Ubicación */}
            <div className="p-6 bg-gray-50/50">
                <h3 className="text-lg font-bold text-dark mb-4">Datos Demográficos</h3>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                    <div>
                        <label className={labelClasses}>Edad</label>
                        <input
                            name="edad"
                            type="number"
                            min="0"
                            className={inputClasses}
                            value={formData.edad}
                            onChange={(e) => {
                                const val = parseInt(e.target.value);
                                if (val < 0) return; // Prevent negative
                                handleChange(e);
                            }}
                        />
                    </div>
                    <div>
                        <label className={labelClasses}>Género</label>
                        <select name="genero" className={inputClasses} value={formData.genero} onChange={handleChange}>
                            <option value="">No especificar</option>
                            <option value="M">Masculino</option>
                            <option value="F">Femenino</option>
                            <option value="PREFIERO_NO_DECIRLO">Prefiero no decirlo</option>
                            <option value="OTROS">Otros</option>
                        </select>
                    </div>

                    {/* --- CAMPO PAÍS ELIMINADO --- */}
                    {/* Se eliminó el input de País según tu solicitud */}

                    {/* --- SELECTORES DINÁMICOS --- */}
                    <div>
                        <label className={labelClasses}>Departamento</label>
                        <select className={inputClasses} value={selectedDep} onChange={handleDepChange}>
                            <option value="">Seleccionar...</option>
                            {departamentos.map(d => <option key={d.id} value={d.id}>{d.nombre}</option>)}
                        </select>
                    </div>

                    <div>
                        <label className={labelClasses}>Provincia {loadingUbi && '...'}</label>
                        <select className={inputClasses} value={selectedProv} onChange={handleProvChange} disabled={!selectedDep}>
                            <option value="">Seleccionar...</option>
                            {provincias.map(p => <option key={p.id} value={p.id}>{p.nombre}</option>)}
                        </select>
                    </div>

                    <div>
                        <label className={labelClasses}>Distrito <span className="text-red-500">*</span></label>
                        <select
                            name="distritoId"
                            className={inputClasses}
                            value={formData.distritoId}
                            onChange={handleChange}
                            disabled={!selectedProv}
                            required
                        >
                            <option value="">Seleccionar...</option>
                            {distritos.map(d => <option key={d.id} value={d.id}>{d.nombre}</option>)}
                        </select>
                    </div>
                </div>
            </div>

            <div className="p-6 flex justify-end gap-4 bg-gray-50 rounded-b-lg">
                <Button type="button" variant="secondary" onClick={onCancel} disabled={isLoading}>Cancelar</Button>
                <Button type="submit" variant="primary" isLoading={isLoading}>Guardar Lead</Button>
            </div>
        </form>
    );
};
