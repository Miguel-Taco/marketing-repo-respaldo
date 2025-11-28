package pe.unmsm.crm.marketing.campanas.telefonicas.domain.model;

/**
 * Estrategias de reintentos para llamadas fallidas.
 */
public enum RetryPolicyType {
    FIXED_INTERVAL,
    EXPONENTIAL_BACKOFF,
    MAX_N_ATTEMPTS
}

