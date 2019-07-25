package com.anaplan.client.transport.retryer;

import com.anaplan.client.Constants;
import feign.RetryableException;
import feign.Retryer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Feign retryer that emulates Spring's BackoffExponentialPolicy for setting intervals/periods
 * between attempts.
 */
public class FeignApiRetryer extends Retryer.Default {

    private static final Logger LOG = LoggerFactory.getLogger(FeignApiRetryer.class);

    public static final long DEFAULT_PERIOD = Constants.MIN_RETRY_TIMEOUT_SECS * 1000L;
    public static final long DEFAULT_MAX_PERIOD = Constants.MAX_RETRY_TIMEOUT_SECS * 1000L;
    public static final int DEFAULT_MAX_ATTEMPTS = Constants.MIN_RETRY_COUNT;
    public static final double DEFAULT_BACKOFF_MULTIPLIER = Constants.DEFAULT_BACKOFF_MULTIPLIER;

    private Long period;
    private Long maxPeriod;
    private Integer maxAttempts;
    private int attempt;
    private Double backoffMultiplier;
    private long sleptForMillis;

    public FeignApiRetryer(Long period, Long maxPeriod, Integer maxAttempts, Double backoffMultiplier) {
        this.period = (period == null) ? DEFAULT_PERIOD : period;
        this.maxPeriod = (maxPeriod == null) ? DEFAULT_MAX_PERIOD : maxPeriod;
        this.maxAttempts = (maxAttempts == null) ? DEFAULT_MAX_ATTEMPTS : maxAttempts;
        this.backoffMultiplier = (backoffMultiplier == null) ? DEFAULT_BACKOFF_MULTIPLIER : backoffMultiplier;
    }

    /**
     * Straight up ripped from Retryer.Default because nextMaxInterval is not polymorphic and hence
     * not overridable. Otherwise would have invoked super.continueOrPropagate(e, lambda...)
     * <p>
     * TODO: Contribute back to Feign OSS
     *
     * @param e RetryableException containing details of the retryable error
     */
    @Override
    public void continueOrPropagate(RetryableException e) {
        if (this.attempt++ >= this.maxAttempts) {
            throw e;
        } else {
            LOG.info("Retrying API request: Attempt ({})", this.attempt);
            LOG.debug("Request details: {}", e.getMessage());
            long interval;
            if (e.retryAfter() != null) {
                interval = e.retryAfter().getTime() - this.currentTimeMillis();
                if (interval > this.maxPeriod) {
                    interval = this.maxPeriod;
                }
                if (interval < 0L) {
                    return;
                }
            } else {
                interval = this.nextMaxInterval();
            }
            try {
                Thread.sleep(interval);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            this.sleptForMillis += interval;
        }
    }

    /**
     * Implements Spring's ExponentialBackOffPolicy with some modifications
     *
     * @return An interval that is set by the backOffMultiplier
     */
    private long nextMaxInterval() {
        long interval = (long) ((double) this.period * Math.pow(backoffMultiplier, (double) (this.attempt - 1)));
        return interval > this.maxPeriod ? this.maxPeriod : interval;
    }

    @Override
    public Retryer clone() {
        return new FeignApiRetryer(period, maxPeriod, maxAttempts, backoffMultiplier);
    }
}
