import React from 'react';
import { Routes, Route } from 'react-router-dom';
import { CampaignsPage } from './pages/CampaignsPage';
import { CampaignLeadsPage } from './pages/CampaignLeadsPage';
import { CallQueuePage } from './pages/CallQueuePage';
import { CallScreenPage } from './pages/CallScreenPage';
import { CallHistoryPage } from './pages/CallHistoryPage';
import { AgentMetricsPage } from './pages/AgentMetricsPage';
import { ScriptManagementPage } from './pages/ScriptManagementPage';
import { TelemarketingLayout } from './components/TelemarketingLayout';

export const TelemarketingRoutes: React.FC = () => {
    return (
        <Routes>
            <Route element={<TelemarketingLayout />}>
                <Route path="/" element={<CampaignsPage />} />
                <Route path="/campanias" element={<CampaignsPage />} />
                <Route path="/leads" element={<CampaignLeadsPage />} />
                <Route path="/campanias/:id/leads" element={<CampaignLeadsPage />} />
                <Route path="/cola" element={<CallQueuePage />} />
                <Route path="/campanias/:id/cola" element={<CallQueuePage />} />
                <Route path="/campanias/:id/llamar/:idContacto" element={<CallScreenPage />} />
                <Route path="/historial" element={<CallHistoryPage />} />
                <Route path="/campanias/:id/historial" element={<CallHistoryPage />} />
                <Route path="/metricas" element={<AgentMetricsPage />} />
                <Route path="/guiones" element={<ScriptManagementPage />} />
            </Route>
        </Routes>
    );
};
