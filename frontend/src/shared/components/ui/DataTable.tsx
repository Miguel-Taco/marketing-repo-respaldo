import React, { ReactNode } from 'react';
import { LoadingSpinner } from './LoadingSpinner';

interface Column<T> {
    header: string;
    accessor: keyof T | ((item: T) => ReactNode);
    className?: string;
}

interface PaginationProps {
    currentPage: number;
    totalPages: number;
    totalElements: number;
    onPageChange: (page: number) => void;
}

interface DataTableProps<T> {
    columns: Column<T>[];
    data: T[];
    loading?: boolean;
    pagination?: PaginationProps;
    emptyMessage?: string;
}

export function DataTable<T extends { id?: number | string }>({
    columns,
    data,
    loading = false,
    pagination,
    emptyMessage = 'No hay datos disponibles'
}: DataTableProps<T>) {
    if (loading) {
        return (
            <div className="flex justify-center items-center h-64 bg-white rounded-lg shadow">
                <LoadingSpinner size="lg" />
            </div>
        );
    }

    if (!data || data.length === 0) {
        return (
            <div className="flex justify-center items-center h-64 bg-white rounded-lg shadow text-gray-500">
                {emptyMessage}
            </div>
        );
    }

    return (
        <div className="bg-white shadow rounded-lg overflow-hidden">
            <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                        <tr>
                            {columns.map((col, index) => (
                                <th
                                    key={index}
                                    scope="col"
                                    className={`px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider ${col.className || ''}`}
                                >
                                    {col.header}
                                </th>
                            ))}
                        </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                        {data.map((item, rowIndex) => (
                            <tr key={item.id || rowIndex} className="hover:bg-gray-50">
                                {columns.map((col, colIndex) => (
                                    <td
                                        key={colIndex}
                                        className={`px-6 py-4 whitespace-nowrap text-sm text-gray-900 ${col.className || ''}`}
                                    >
                                        {typeof col.accessor === 'function'
                                            ? col.accessor(item)
                                            : (item[col.accessor] as ReactNode)}
                                    </td>
                                ))}
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            {pagination && (
                <div className="bg-white px-4 py-3 flex items-center justify-between border-t border-gray-200 sm:px-6">
                    <div className="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
                        <div>
                            <p className="text-sm text-gray-700">
                                Mostrando página <span className="font-medium">{pagination.currentPage + 1}</span> de{' '}
                                <span className="font-medium">{pagination.totalPages}</span> ({pagination.totalElements} resultados)
                            </p>
                        </div>
                        <div>
                            <nav className="relative z-0 inline-flex rounded-md shadow-sm -space-x-px" aria-label="Pagination">
                                <button
                                    onClick={() => pagination.onPageChange(pagination.currentPage - 1)}
                                    disabled={pagination.currentPage === 0}
                                    className="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:bg-gray-100 disabled:cursor-not-allowed"
                                >
                                    Anterior
                                </button>
                                <button
                                    onClick={() => pagination.onPageChange(pagination.currentPage + 1)}
                                    disabled={pagination.currentPage >= pagination.totalPages - 1}
                                    className="relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:bg-gray-100 disabled:cursor-not-allowed"
                                >
                                    Siguiente
                                </button>
                            </nav>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
