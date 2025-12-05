import React, { useState } from 'react';

interface PreviewMember {
    id: number;
    nombre: string;
    edad: number;
    correo: string;
    telefono: string;
}

interface SegmentPreviewTableProps {
    members: PreviewMember[];
    totalCount: number;
    currentPage: number;
    totalPages: number;
    onPageChange: (page: number) => void;
    isLoading?: boolean;
}

export const SegmentPreviewTable: React.FC<SegmentPreviewTableProps> = ({
    members,
    totalCount,
    currentPage,
    totalPages,
    onPageChange,
    isLoading = false
}) => {
    const [searchTerm, setSearchTerm] = useState('');

    const filteredMembers = members.filter(member => {
        const search = searchTerm.toLowerCase();
        return (
            member.nombre.toLowerCase().includes(search) ||
            member.correo.toLowerCase().includes(search)
        );
    });

    if (isLoading) {
        return (
            <div className="bg-white p-6 rounded-xl shadow-lg border border-separator">
                <div className="flex items-center justify-center py-12">
                    <div className="text-center">
                        <div className="inline-block h-8 w-8 animate-spin rounded-full border-4 border-solid border-primary border-t-transparent"></div>
                        <p className="mt-4 text-gray-600">Cargando previsualización...</p>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="bg-white p-6 rounded-xl shadow-lg border border-separator sticky top-8">
            {/* Header with Counter */}
            <div className="flex justify-between items-center mb-4">
                <h2 className="text-xl font-semibold text-dark">Previsualización de Segmento</h2>
                <span className="text-sm font-bold text-primary bg-blue-50 px-3 py-1 rounded-full">
                    {totalCount} Miembros
                </span>
            </div>

            {/* Search Bar */}
            <div className="relative mb-6">
                <input
                    type="search"
                    placeholder="Buscar por Nombre o Correo..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className="w-full pl-10 pr-4 py-2 border border-separator rounded-lg focus:ring-2 focus:ring-primary focus:outline-none"
                />
                <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-gray-400">
                    search
                </span>
            </div>

            {/* Table Container */}
            <div className="overflow-x-auto max-h-[70vh] overflow-y-auto">
                <table className="min-w-full">
                    <thead className="bg-gray-50 sticky top-0 z-10">
                        <tr>
                            <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase border-b-2 border-gray-200">
                                Nombre
                            </th>
                            <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase border-b-2 border-gray-200 w-20">
                                Edad
                            </th>
                            <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase border-b-2 border-gray-200">
                                Correo
                            </th>
                            <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase border-b-2 border-gray-200 w-32">
                                Número
                            </th>
                        </tr>
                    </thead>
                    <tbody>
                        {filteredMembers.length === 0 ? (
                            <tr>
                                <td colSpan={4} className="px-4 py-8 text-center text-gray-500">
                                    {searchTerm ? 'No se encontraron miembros que coincidan con la búsqueda.' : 'No hay miembros para mostrar.'}
                                </td>
                            </tr>
                        ) : (
                            filteredMembers.map((member) => (
                                <tr key={member.id} className="hover:bg-gray-50 transition-colors border-b border-gray-100">
                                    <td className="px-4 py-3 text-sm text-gray-800">{member.nombre}</td>
                                    <td className="px-4 py-3 text-sm text-gray-800">{member.edad}</td>
                                    <td className="px-4 py-3 text-sm text-gray-800">{member.correo}</td>
                                    <td className="px-4 py-3 text-sm text-gray-800">{member.telefono}</td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>

                {/* Pagination Controls */}
                {totalPages > 1 && (
                    <div className="mt-4 flex items-center justify-between bg-gray-50 py-3 px-4 rounded">
                        <button
                            onClick={() => onPageChange(currentPage - 1)}
                            disabled={currentPage === 0}
                            className="px-3 py-1 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                            Anterior
                        </button>
                        <span className="text-sm text-gray-600">
                            Página {currentPage + 1} de {totalPages}
                        </span>
                        <button
                            onClick={() => onPageChange(currentPage + 1)}
                            disabled={currentPage >= totalPages - 1}
                            className="px-3 py-1 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                            Siguiente
                        </button>
                    </div>
                )}
            </div>
        </div>
    );
};
