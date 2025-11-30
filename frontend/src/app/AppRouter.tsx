import React from 'react';
import { BrowserRouter, Routes, Route, Navigate, Outlet } from 'react-router-dom';
import { MainLayout } from './layout/MainLayout';
import { LeadsListPage } from '../modules/marketing/leads/pages/LeadsListPage';
import { LeadCapturePage } from '../modules/marketing/leads/pages/LeadCapturePage';
import { LeadDetailPage } from '../modules/marketing/leads/pages/LeadDetailPage';
import { LeadImportPage } from '../modules/marketing/leads/pages/LeadImportPage';
import { SegmentationPage } from '../modules/marketing/segmentacion/pages/SegmentationPage';
import { CreateSegmentPage } from '../modules/marketing/segmentacion/pages/CreateSegmentPage';
import { EditSegmentPage } from '../modules/marketing/segmentacion/pages/EditSegmentPage';
import { SegmentacionLayout } from '../modules/marketing/segmentacion/components/SegmentacionLayout';
import { CampanasListPage } from '../modules/marketing/campañas/gestor/pages/CampanasListPage';
import { TelemarketingRoutes } from '../modules/marketing/campañas/telefonicas';
import { MailingListPage } from '../modules/marketing/campañas/mailing/pages/MailingListPage';
import { MailingEditorPage } from '../modules/marketing/campañas/mailing/pages/MailingEditorPage';
import { LeadsProvider } from '../modules/marketing/leads/context/LeadsContext';
import { ImportHistoryProvider } from '../modules/marketing/leads/context/ImportHistoryContext';
import { SegmentosProvider } from '../modules/marketing/segmentacion/context/SegmentosContext';
import { CampaignsProvider } from '../modules/marketing/campañas/telefonicas/context/CampaignsContext';
import { ToastProvider } from '../shared/components/ui/Toast';
import { EncuestaPage } from '../modules/marketing/campañas/encuestas/EncuestaPage';
import { CreateEncuestaPage } from '../modules/marketing/campañas/encuestas/pages/CreateEncuestaPage';

export const AppRouter: React.FC = () => {
  return (
    <BrowserRouter>
      <ToastProvider>
        <SegmentosProvider>
          <LeadsProvider>
            <CampaignsProvider>
              <ImportHistoryProvider>
                <Routes>
                  <Route path="/" element={<MainLayout />}>
                    <Route index element={<Navigate to="/leads" replace />} />

                    {/* Rutas de Leads */}
                    <Route path="leads" element={<LeadsListPage />} />
                    <Route path="leads/new" element={<LeadCapturePage />} />
                    <Route path="leads/import" element={<LeadImportPage />} />
                    <Route path="leads/:id" element={<LeadDetailPage />} />

                    {/* Rutas de Segmentación */}
                    <Route path="marketing/segmentacion" element={<Outlet />}>
                      <Route index element={<SegmentationPage />} />
                      <Route path="new" element={<CreateSegmentPage />} />
                      <Route path="edit/:id" element={<EditSegmentPage />} />
                    </Route>

                    {/* Rutas de Encuestas */}
                    <Route path="encuestas" element={<EncuestaPage />} />
                    <Route path="encuestas/new" element={<CreateEncuestaPage />} />
                    <Route path="encuestas/edit/:id" element={<CreateEncuestaPage />} />

                    {/* Rutas de Campañas */}
                    <Route path="marketing/campanas" element={<CampanasListPage />} />

                    {/* Rutas de Campañas de Mailing */}
                    <Route path="marketing/campanas/mailing" element={<MailingListPage />} />
                    <Route path="marketing/campanas/mailing/:id/edit" element={<MailingEditorPage />} />
                    {/* Alias para acceder por /emailing (compatibilidad con Sidebar) */}
                    <Route path="emailing" element={<MailingListPage />} />
                    <Route path="emailing/:id/edit" element={<MailingEditorPage />} />

                    {/* Rutas de Campañas Telefónicas */}
                    <Route path="marketing/campanas/telefonicas/*" element={<TelemarketingRoutes />} />
                  </Route>
                </Routes>
              </ImportHistoryProvider>
            </CampaignsProvider>
          </LeadsProvider>
        </SegmentosProvider>
      </ToastProvider>
    </BrowserRouter>
  );
};
