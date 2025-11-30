import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { Plus, Search, Filter } from 'lucide-react';
import { guionesApi } from '../services/guiones.api';
import { GuionDTO } from '../types/guiones.types';
import { CreateScriptModal } from '../components/CreateScriptModal';
import { ScriptCard } from '../components/ScriptCard';
import { LinkScriptToCampaignModal } from '../components/LinkScriptToCampaignModal';
import { ViewScriptModal } from '../components/ViewScriptModal';
import { FileUploadDropzone } from '../components/FileUploadDropzone';
import { DeleteConfirmModal } from '../components/DeleteConfirmModal';
import { Button } from '../../../../../shared/components/ui/Button';

export const ScriptManagementPage: React.FC = () => {
    const { id: campaignId } = useParams<{ id: string }>();
    const [guiones, setGuiones] = useState<GuionDTO[]>([]);
    const [loading, setLoading] = useState(false);

    // Modal states
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [isLinkModalOpen, setIsLinkModalOpen] = useState(false);
    const [isViewModalOpen, setIsViewModalOpen] = useState(false);
    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);

    // Selection states
    const [selectedGuionForLink, setSelectedGuionForLink] = useState<GuionDTO | null>(null);
    const [selectedGuionForEdit, setSelectedGuionForEdit] = useState<GuionDTO | null>(null);
    const [selectedGuionForView, setSelectedGuionForView] = useState<GuionDTO | null>(null);
    const [selectedGuionForDelete, setSelectedGuionForDelete] = useState<GuionDTO | null>(null);

    // Delete state
    const [isDeleting, setIsDeleting] = useState(false);

    // Filter states
    const [searchTerm, setSearchTerm] = useState('');
    const [filterType, setFilterType] = useState<string>('ALL');

    const loadGuiones = async () => {
        try {
            setLoading(true);
            const data = await guionesApi.listGuiones();
            setGuiones(data || []);
        } catch (error) {
            console.error('Error cargando guiones:', error);
            setGuiones([]);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadGuiones();
    }, []);

    const handleCreateSuccess = () => {
        loadGuiones();
        setIsCreateModalOpen(false);
        setSelectedGuionForEdit(null);
    };

    const handleEdit = (guion: GuionDTO) => {
        setSelectedGuionForEdit(guion);
        setIsCreateModalOpen(true);
    };

    const handleLink = (guion: GuionDTO) => {
        setSelectedGuionForLink(guion);
        setIsLinkModalOpen(true);
    };

    const handleLinkSuccess = () => {
        // Opcional: mostrar notificación de éxito
        // alert('Guión vinculado exitosamente');
        setIsLinkModalOpen(false);
    };

    const handleDelete = (guion: GuionDTO) => {
        setSelectedGuionForDelete(guion);
        setIsDeleteModalOpen(true);
    };

    const confirmDelete = async () => {
        if (!selectedGuionForDelete?.id) return;

        setIsDeleting(true);
        try {
            await guionesApi.deleteGuion(selectedGuionForDelete.id);
            // Reload the scripts list after successful deletion
            await loadGuiones();
            // Close modal and reset state
            setIsDeleteModalOpen(false);
            setSelectedGuionForDelete(null);
        } catch (error: any) {
            console.error('Error deleting script:', error);
            alert(error.response?.data?.message || 'Error al eliminar el guión');
        } finally {
            setIsDeleting(false);
        }
    };

    const handleView = (guion: GuionDTO) => {
        setSelectedGuionForView(guion);
        setIsViewModalOpen(true);
    };

    const handleFileUpload = async (file: File) => {
        if (!campaignId) {
            throw new Error('No se puede subir archivo sin un ID de campaña');
        }

        try {
            await guionesApi.uploadGuionFile(Number(campaignId), file);
            // Reload the scripts list after successful upload
            await loadGuiones();
        } catch (error: any) {
            console.error('Error uploading file:', error);
            throw new Error(error.response?.data?.message || 'Error al subir el archivo');
        }
    };

    const filteredGuiones = (guiones || []).filter(guion => {
        const matchesSearch = guion.nombre.toLowerCase().includes(searchTerm.toLowerCase()) ||
            guion.objetivo.toLowerCase().includes(searchTerm.toLowerCase());
        const matchesType = filterType === 'ALL' || guion.tipo === filterType;
        return matchesSearch && matchesType;
    });

    return (
        <div className="flex flex-col h-full p-6 bg-gray-50">
            {/* Header */}
            <div className="flex justify-between items-start mb-8">
                <div>
                    <h1 className="text-3xl font-bold text-gray-900">Gestión de Guiones</h1>
                    <p className="text-gray-500 mt-2">Crea y administra los guiones estructurados para tus campañas</p>
                </div>
                <Button
                    variant="primary"
                    onClick={() => {
                        setSelectedGuionForEdit(null);
                        setIsCreateModalOpen(true);
                    }}
                    className="flex items-center gap-2"
                >
                    <Plus className="w-5 h-5" />
                    Crear Nuevo Guión
                </Button>
            </div>

            {/* File Upload Dropzone - Only show when in campaign context */}
            {campaignId && (
                <div className="mb-6">
                    <FileUploadDropzone onFileUpload={handleFileUpload} />
                </div>
            )}

            {/* Filters */}
            <div className="bg-white p-4 rounded-lg border border-gray-200 shadow-sm mb-6 flex flex-wrap gap-4 items-center justify-between">
                <div className="relative flex-1 max-w-md">
                    <Search className="w-5 h-5 text-gray-400 absolute left-3 top-1/2 transform -translate-y-1/2" />
                    <input
                        type="text"
                        placeholder="Buscar guiones..."
                        className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>
                <div className="flex items-center gap-2">
                    <Filter className="w-5 h-5 text-gray-400" />
                    <select
                        className="border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        value={filterType}
                        onChange={(e) => setFilterType(e.target.value)}
                    >
                        <option value="ALL">Todos los tipos</option>
                        <option value="RENOVACION">Renovación</option>
                        <option value="VENTA_NUEVA">Venta Nueva</option>
                        <option value="RECUPERO">Recupero</option>
                        <option value="RETENCION">Retención</option>
                        <option value="ENCUESTA">Encuesta</option>
                    </select>
                </div>
            </div>

            {/* Content */}
            {loading ? (
                <div className="flex-1 flex items-center justify-center">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
                </div>
            ) : filteredGuiones.length === 0 ? (
                <div className="flex-1 flex flex-col items-center justify-center text-gray-500 bg-white rounded-lg border border-gray-200 border-dashed m-1">
                    <div className="p-4 bg-gray-50 rounded-full mb-4">
                        <Plus className="w-8 h-8 text-gray-400" />
                    </div>
                    <h3 className="text-lg font-medium text-gray-900 mb-1">No hay guiones creados</h3>
                    <p className="mb-4">Comienza creando tu primer guión estructurado</p>
                    <Button variant="secondary" onClick={() => {
                        setSelectedGuionForEdit(null);
                        setIsCreateModalOpen(true);
                    }}>
                        Crear Guión
                    </Button>
                </div>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {filteredGuiones.map(guion => (
                        <ScriptCard
                            key={guion.id}
                            guion={guion}
                            onEdit={handleEdit}
                            onLink={handleLink}
                            onDelete={handleDelete}
                            onView={handleView}
                            showLinkButton={!campaignId} // Only show link button when NOT in campaign context
                        />
                    ))}
                </div>
            )}

            {/* Modals */}
            <CreateScriptModal
                isOpen={isCreateModalOpen}
                onClose={() => {
                    setIsCreateModalOpen(false);
                    setSelectedGuionForEdit(null);
                }}
                onSuccess={handleCreateSuccess}
                initialData={selectedGuionForEdit}
                campaignId={campaignId}
            />

            <LinkScriptToCampaignModal
                isOpen={isLinkModalOpen}
                onClose={() => setIsLinkModalOpen(false)}
                guion={selectedGuionForLink}
                onSuccess={handleLinkSuccess}
            />

            <ViewScriptModal
                isOpen={isViewModalOpen}
                onClose={() => {
                    setIsViewModalOpen(false);
                    setSelectedGuionForView(null);
                }}
                guion={selectedGuionForView}
            />

            <DeleteConfirmModal
                isOpen={isDeleteModalOpen}
                onClose={() => {
                    setIsDeleteModalOpen(false);
                    setSelectedGuionForDelete(null);
                }}
                onConfirm={confirmDelete}
                guion={selectedGuionForDelete}
                isDeleting={isDeleting}
            />
        </div>
    );
};
