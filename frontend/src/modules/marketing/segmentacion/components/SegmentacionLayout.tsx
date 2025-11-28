import React from 'react';
import { Outlet } from 'react-router-dom';
import { SegmentosProvider } from '../context/SegmentosContext';

/**
 * Layout wrapper for segmentation routes that provides the SegmentosContext
 */
export const SegmentacionLayout: React.FC = () => {
    return (
        <SegmentosProvider>
            <Outlet />
        </SegmentosProvider>
    );
};
