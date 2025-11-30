import React from 'react';

interface StatsCardProps {
    title: string;
    value: string | number;
    trend?: {
        value: number;
        isPositive: boolean;
    };
    icon?: string;
}

export const StatsCard: React.FC<StatsCardProps> = ({ title, value, trend, icon }) => {
    return (
        <div className="flex min-w-[158px] flex-1 flex-col gap-2 rounded-lg bg-white p-6 border border-gray-200/50 shadow-sm">
            <p className="text-base font-medium text-gray-600">{title}</p>
            <p className="tracking-light text-3xl font-bold text-gray-900">{value}</p>
            {trend && (
                <p className={`text-base font-medium leading-normal ${trend.isPositive ? 'text-green-600' : 'text-red-600'
                    }`}>
                    {trend.isPositive ? '+' : ''}{trend.value.toFixed(1)}%
                </p>
            )}
        </div>
    );
};
