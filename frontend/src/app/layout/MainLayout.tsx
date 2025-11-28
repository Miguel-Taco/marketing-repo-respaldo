import React from 'react';
import { Outlet } from 'react-router-dom';
import { Sidebar } from '../../shared/components/layout/Sidebar';

export const MainLayout: React.FC = () => {
    return (
        <div className="flex min-h-screen bg-gray-50">
            {/* Sidebar Navigation */}
            <Sidebar />

            {/* Main Content Area */}
            <main className="flex-1 ml-20 p-8">
                <Outlet />
            </main>
        </div>
    );
};
