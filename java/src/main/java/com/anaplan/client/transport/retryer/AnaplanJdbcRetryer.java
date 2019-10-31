package com.anaplan.client.transport.retryer;

/**
 * Anaplan retryer that emulates Spring's BackoffExponentialPolicy for setting intervals/periods
 * between attempts.
 */
public class AnaplanJdbcRetryer implements AnaplanRetryer {

    private Long period;
    private Long maxPeriod;
    private Double backoffMultiplier;

    public AnaplanJdbcRetryer(Long period, Long maxPeriod, Double backoffMultiplier) {
        this.period = (period == null) ? DEFAULT_PERIOD : period;
        this.maxPeriod = (maxPeriod == null) ? DEFAULT_MAX_PERIOD : maxPeriod;
        this.backoffMultiplier = (backoffMultiplier == null) ? DEFAULT_BACKOFF_MULTIPLIER : backoffMultiplier;
    }

    /**
     * Implements Spring's ExponentialBackOffPolicy with some modifications
     *
     * @return An interval that is set by the backOffMultiplier
     */
    @Override
    public long nextMaxInterval(int noOfAttempts) {
        long interval = (long) ((double) this.period * Math.pow(backoffMultiplier, (double) (noOfAttempts)));
        return interval > this.maxPeriod ? this.maxPeriod : interval;
    }

}
