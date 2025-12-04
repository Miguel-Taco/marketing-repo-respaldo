import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../shared/context/AuthContext';

export const LoginPage: React.FC = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(false);
    const [showPassword, setShowPassword] = useState(false);

    const { login } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);
        setIsLoading(true);

        try {
            await login({ username, password });
            // Redirigir a la página intentada o a leads por defecto
            const from = (location.state as any)?.from?.pathname || '/leads';
            navigate(from, { replace: true });
        } catch (err: any) {
            console.error('Login error:', err);
            setError('Credenciales inválidas. Por favor intenta nuevamente.');
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="relative flex h-auto min-h-screen w-full flex-col bg-[#f5f7f8] dark:bg-[#101722] overflow-x-hidden font-display">
            <div className="flex flex-1 w-full">
                {/* Left Side - Branding */}
                <div className="hidden lg:flex flex-col justify-between w-2/5 min-h-screen bg-[#3c83f6] p-12 text-white">
                    <div className="flex flex-col gap-4">
                        <div className="flex items-center gap-3">
                            <svg className="text-white" fill="none" height="32" stroke="currentColor" strokeLinecap="round"
                                strokeLinejoin="round" strokeWidth="2" viewBox="0 0 24 24" width="32"
                                xmlns="http://www.w3.org/2000/svg">
                                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
                            </svg>
                            <span className="text-xl font-bold">Marketing CRM</span>
                        </div>
                        <div className="mt-8">
                            <h1 className="text-white tracking-light text-[32px] font-bold leading-tight">Módulo de Marketing</h1>
                            <p className="text-white/80 text-base font-normal leading-normal pt-2">
                                Gestiona Leads, segmentalos, usalos en campañas y recibe feedback de tus clientes.
                            </p>
                        </div>
                    </div>
                    <div className="relative w-full aspect-square max-w-sm mx-auto flex items-center justify-center">
                        <div className="absolute inset-0 bg-white/10 rounded-full"></div>
                        <div className="absolute inset-8 bg-white/10 rounded-full"></div>
                        <span className="material-symbols-outlined !text-9xl text-white/50" style={{ fontSize: '120px' }}>
                            ads_click
                        </span>
                    </div>
                </div>

                {/* Right Side - Login Form */}
                <div className="flex flex-1 items-center justify-center p-6 lg:p-12 bg-[#f5f7f8] dark:bg-[#101722]">
                    <div className="w-full max-w-md mx-auto">
                        <div className="bg-white dark:bg-[#101722] dark:border dark:border-white/10 shadow-lg rounded-lg p-8 md:p-10">
                            <div className="flex flex-col text-left mb-8">
                                <h1 className="text-[#1F2937] dark:text-white text-[22px] font-bold leading-tight tracking-[-0.015em]">
                                    Iniciar sesión
                                </h1>
                                <p className="text-[#6B7280] dark:text-white/60 text-sm font-normal leading-normal pt-1">
                                    Ingresa a tu cuenta de Marketing
                                </p>
                            </div>

                            {error && (
                                <div className="mb-6 p-4 bg-red-50 border border-red-200 text-red-600 rounded-lg text-sm">
                                    {error}
                                </div>
                            )}

                            <form onSubmit={handleSubmit} className="flex flex-col gap-8" noValidate>
                                <div className="flex flex-col gap-1.5">
                                    <label className="text-[#1F2937] dark:text-white text-base font-medium leading-normal" htmlFor="username">
                                        Usuario
                                    </label>
                                    <input
                                        className="flex w-full min-w-0 flex-1 resize-none overflow-hidden rounded-xl text-[#1F2937] dark:text-white focus:outline-0 focus:ring-2 focus:ring-[#3c83f6]/50 border border-[#D9D9D9] dark:border-white/20 bg-white dark:bg-[#101722] h-14 placeholder:text-[#6B7280] p-[15px] text-base font-normal leading-normal"
                                        id="username"
                                        name="username"
                                        placeholder="Ingresa tu usuario"
                                        type="text"
                                        autoComplete="username"
                                        value={username}
                                        onChange={(e) => setUsername(e.target.value)}
                                        required
                                    />
                                </div>
                                <div className="flex flex-col gap-1.5">
                                    <label className="text-[#1F2937] dark:text-white text-base font-medium leading-normal" htmlFor="password">
                                        Contraseña
                                    </label>
                                    <div className="relative">
                                        <input
                                            className="flex w-full min-w-0 flex-1 resize-none overflow-hidden rounded-xl text-[#1F2937] dark:text-white focus:outline-0 focus:ring-2 focus:ring-[#3c83f6]/50 border border-[#D9D9D9] dark:border-white/20 bg-white dark:bg-[#101722] h-14 placeholder:text-[#6B7280] p-[15px] pr-12 text-base font-normal leading-normal"
                                            id="password"
                                            placeholder="Ingresa tu contraseña"
                                            type={showPassword ? "text" : "password"}
                                            value={password}
                                            onChange={(e) => setPassword(e.target.value)}
                                            required
                                        />
                                        <button
                                            className="absolute inset-y-0 right-0 flex items-center pr-4 text-[#6B7280] dark:text-white/60 hover:text-[#3c83f6]"
                                            type="button"
                                            onClick={() => setShowPassword(!showPassword)}
                                        >
                                            <span className="material-symbols-outlined">
                                                {showPassword ? 'visibility_off' : 'visibility'}
                                            </span>
                                        </button>
                                    </div>
                                </div>

                                <button
                                    className="flex items-center justify-center gap-2 w-full bg-[#3c83f6] text-white font-semibold h-14 px-6 rounded-full hover:bg-[#3c83f6]/90 focus:outline-none focus:ring-2 focus:ring-[#3c83f6] focus:ring-offset-2 dark:focus:ring-offset-[#101722] transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                                    type="submit"
                                    disabled={isLoading}
                                >
                                    {isLoading ? (
                                        <span>Iniciando sesión...</span>
                                    ) : (
                                        <>
                                            <span>Ingresar</span>
                                            <span className="material-symbols-outlined">arrow_forward</span>
                                        </>
                                    )}
                                </button>
                            </form>

                            <div className="text-center mt-8">
                                <p className="text-[#6B7280] dark:text-white/60 text-xs">
                                    Parte del sistema CRM. Si no tienes acceso, contacta a tu administrador.
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};
