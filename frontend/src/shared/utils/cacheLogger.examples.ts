/**
 * Cache Logger - Usage Examples
 * 
 * This file demonstrates how to use the shared CacheLogger utility
 * in different contexts across the application.
 */

import { createCacheLogger } from './cacheLogger';

// ============================================================================
// Example 1: Basic Usage in a Context
// ============================================================================

// Initialize logger for a specific module
const userCacheLogger = createCacheLogger('UserCache', true);

// Log cache operations
userCacheLogger.hit('user-123');                    // Cache hit
userCacheLogger.miss('user-456');                   // Cache miss, loading
userCacheLogger.stored('user-456');                 // Successfully stored
userCacheLogger.invalidate('user-123');             // Single key invalidation
userCacheLogger.invalidate(['user-123', 'user-456']); // Multiple keys
userCacheLogger.clear('all users');                 // Clear entire cache

// ============================================================================
// Example 2: Using with Additional Data
// ============================================================================

const productCacheLogger = createCacheLogger('ProductCache');

// Log with additional context
productCacheLogger.hit('product-789', {
    timestamp: new Date(),
    source: 'memory'
});

productCacheLogger.miss('product-101', {
    reason: 'expired',
    ttl: 3600
});

// ============================================================================
// Example 3: Conditional Logging (Development vs Production)
// ============================================================================

const isDevelopment = import.meta.env.DEV;
const apiCacheLogger = createCacheLogger('APICache', isDevelopment);

// Logs will only appear in development
apiCacheLogger.hit('api/users');
apiCacheLogger.miss('api/products');

// ============================================================================
// Example 4: Dynamic Enable/Disable
// ============================================================================

const dynamicLogger = createCacheLogger('DynamicCache');

// Enable/disable at runtime
dynamicLogger.disable();
dynamicLogger.hit('key-1'); // Won't log

dynamicLogger.enable();
dynamicLogger.hit('key-2'); // Will log

// Check if enabled
if (dynamicLogger.isEnabled()) {
    // Perform expensive logging operations
}

// ============================================================================
// Example 5: Integration in a React Context (Campaign Cache Example)
// ============================================================================

/*
import { createCacheLogger } from '@/shared/utils/cacheLogger';

const logger = createCacheLogger('CampaignCache', true);

export const CampaignCacheProvider = ({ children }) => {
    const getCachedData = async (campaignId, dataType) => {
        // Check cache
        if (cache[campaignId]?.[dataType]) {
            logger.hit(`${dataType} for campaign ${campaignId}`);
            return cache[campaignId][dataType];
        }

        // Load from API
        logger.miss(`${dataType} for campaign ${campaignId}`);
        const data = await fetchData(campaignId, dataType);
        
        // Store in cache
        cache[campaignId][dataType] = data;
        logger.stored(`${dataType} for campaign ${campaignId}`);
        
        return data;
    };

    const invalidateCache = (campaignId, dataTypes) => {
        logger.invalidate(dataTypes, `for campaign ${campaignId}`);
        // ... invalidation logic
    };

    return (
        <CampaignCacheContext.Provider value={{ getCachedData, invalidateCache }}>
            {children}
        </CampaignCacheContext.Provider>
    );
};
*/

// ============================================================================
// Example 6: Custom Log Types (Using Generic log method)
// ============================================================================

const customLogger = createCacheLogger('CustomCache');

// Use generic log for custom messages
customLogger.log('HIT', 'Custom cache hit message', { custom: 'data' });
customLogger.log('INVALIDATE', 'Batch invalidation', { count: 50 });

// ============================================================================
// Console Output Examples
// ============================================================================

/*
Expected console output with colors (Spanish, no emojis):

[CampaignCache] [ACIERTO] queue for campaign 1
[CampaignCache] [FALLO] Loading leads for campaign 1
[CampaignCache] [GUARDADO] leads for campaign 1
[CampaignCache] [INVALIDADO] queue, history, leads for campaign 1
[CampaignCache] [LIMPIADO] All data for campaign 1

Colors:
- ACIERTO (HIT): Green
- FALLO (MISS): Orange
- ESPERANDO (WAITING): Blue
- GUARDADO (STORED): Purple
- INVALIDADO (INVALIDATE): Red
- LIMPIADO (CLEAR): Dark Red
*/
