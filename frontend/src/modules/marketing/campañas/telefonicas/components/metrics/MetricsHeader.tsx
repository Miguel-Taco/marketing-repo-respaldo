import React from 'react';

interface MetricsHeaderProps {
    diasFiltro: number;
    onDiasChange: (dias: number) => void;
    onRefresh: () => void;
}

export const MetricsHeader: React.FC<MetricsHeaderProps> = ({
    diasFiltro,
    onDiasChange,
    onRefresh
}) => {
    const diasOptions = [7, 15, 30, 90];

    return (
        <div className="flex flex-col gap-4 p-4">
            <h1 className="text-3xl md:text-4xl font-black tracking-tight text-gray-900">
                Métricas de campaña
            </h1>

            <div className="flex flex-wrap items-center justify-center gap-3 pt-6 w-full sm:max-w-xl mx-auto">
                <div className="relative">
                    <select
                        value={diasFiltro}
                        onChange={(e) => onDiasChange(Number(e.target.value))}
                        className="flex h-10 shrink-0 items-center justify-center gap-x-2 rounded-full bg-white border border-gray-300 pl-4 pr-10 shadow-sm text-sm font-medium leading-normal appearance-none cursor-pointer"
                    >
                        {diasOptions.map(dias => (
                            <option key={dias} value={dias}>
                                Últimos {dias} días
                            </option>
                        ))}
                    </select>
                    <span className="material-symbols-outlined absolute right-3 top-1/2 -translate-y-1/2 text-base pointer-events-none">
                        expand_more
                    </span>
                </div>

                <button
                    onClick={onRefresh}
                    className="flex min-w-[84px] cursor-pointer items-center justify-center gap-2 overflow-hidden rounded-full h-10 px-4 bg-blue-600 text-white text-sm font-bold leading-normal tracking-wide shadow-sm hover:bg-blue-700 transition-colors"
                >
                    <span className="material-symbols-outlined text-lg">refresh</span>
                    <span className="truncate">Actualizar</span>
                </button>
            </div>
        </div>
    );
};
