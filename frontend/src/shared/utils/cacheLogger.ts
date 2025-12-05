/**
 * Shared Cache Logger Utility
 * 
 * Provides consistent, styled logging for cache operations across the application.
 * Supports different log types with color-coded console output for easy debugging.
 */

export type CacheLogType = 'HIT' | 'MISS' | 'WAITING' | 'STORED' | 'INVALIDATE' | 'CLEAR';

export interface CacheLoggerOptions {
    moduleName: string;
    enabled?: boolean;
}

/**
 * CacheLogger class for standardized cache operation logging
 * 
 * @example
 * const logger = new CacheLogger({ moduleName: 'UserCache', enabled: true });
 * logger.hit('user-123');
 * logger.miss('user-456');
 * logger.invalidate(['user-123', 'user-456']);
 */
export class CacheLogger {
    private enabled: boolean;
    private moduleName: string;

    // Color styles for different log types
    private static readonly styles = {
        HIT: 'color: #10b981; font-weight: bold',        // Green
        MISS: 'color: #f59e0b; font-weight: bold',       // Orange
        WAITING: 'color: #3b82f6; font-weight: bold',    // Blue
        STORED: 'color: #8b5cf6; font-weight: bold',     // Purple
        INVALIDATE: 'color: #ef4444; font-weight: bold', // Red
        CLEAR: 'color: #dc2626; font-weight: bold',      // Dark Red
    };

    private static readonly labels = {
        HIT: 'ACIERTO',
        MISS: 'FALLO',
        WAITING: 'ESPERANDO',
        STORED: 'GUARDADO',
        INVALIDATE: 'INVALIDADO',
        CLEAR: 'LIMPIADO',
    };

    constructor(options: CacheLoggerOptions) {
        this.moduleName = options.moduleName;
        this.enabled = options.enabled !== false; // Default to true
    }

    /**
     * Generic log method
     */
    log(type: CacheLogType, message: string, data?: any) {
        if (!this.enabled) return;

        const style = CacheLogger.styles[type];
        const label = CacheLogger.labels[type];
        const prefix = `[${this.moduleName}]`;
        const typeLabel = `[${label}]`;

        if (data !== undefined) {
            console.log(
                `%c${prefix} %c${typeLabel}%c ${message}`,
                'color: #6b7280; font-weight: bold',
                style,
                'color: inherit',
                data
            );
        } else {
            console.log(
                `%c${prefix} %c${typeLabel}%c ${message}`,
                'color: #6b7280; font-weight: bold',
                style,
                'color: inherit'
            );
        }
    }

    /**
     * Log a cache hit (data found in cache)
     */
    hit(key: string, additionalInfo?: any) {
        this.log('HIT', key, additionalInfo);
    }

    /**
     * Log a cache miss (data not found, fetching from source)
     */
    miss(key: string, additionalInfo?: any) {
        this.log('MISS', `Loading ${key}`, additionalInfo);
    }

    /**
     * Log waiting for an ongoing fetch
     */
    waiting(key: string, additionalInfo?: any) {
        this.log('WAITING', key, additionalInfo);
    }

    /**
     * Log successful cache storage
     */
    stored(key: string, additionalInfo?: any) {
        this.log('STORED', key, additionalInfo);
    }

    /**
     * Log cache invalidation
     */
    invalidate(keys: string | string[], additionalInfo?: any) {
        const keyList = Array.isArray(keys) ? keys.join(', ') : keys;
        this.log('INVALIDATE', keyList, additionalInfo);
    }

    /**
     * Log cache clear
     */
    clear(scope: string, additionalInfo?: any) {
        this.log('CLEAR', `All data for ${scope}`, additionalInfo);
    }

    /**
     * Enable logging
     */
    enable() {
        this.enabled = true;
    }

    /**
     * Disable logging
     */
    disable() {
        this.enabled = false;
    }

    /**
     * Check if logging is enabled
     */
    isEnabled(): boolean {
        return this.enabled;
    }
}

/**
 * Factory function to create a cache logger
 */
export function createCacheLogger(moduleName: string, enabled = true): CacheLogger {
    return new CacheLogger({ moduleName, enabled });
}
