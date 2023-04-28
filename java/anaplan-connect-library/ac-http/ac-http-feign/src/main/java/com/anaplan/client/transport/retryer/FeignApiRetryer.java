package com.anaplan.client.transport.retryer;

import com.anaplan.client.Constants;
import feign.RetryableException;
import feign.Retryer;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Feign retryer that emulates Spring's BackoffExponentialPolicy for setting intervals/periods between attempts.
 */
public class FeignApiRetryer extends Retryer.Default {

  private static final Logger LOG = LoggerFactory.getLogger(FeignApiRetryer.class);

  public static final long DEFAULT_PERIOD = Constants.MIN_RETRY_TIMEOUT_SECS * 1000L;
  public static final long DEFAULT_MAX_PERIOD = Constants.MAX_RETRY_TIMEOUT_SECS * 1000L;
  public static final int DEFAULT_MAX_ATTEMPTS = Constants.MIN_RETRY_COUNT;
  public static final double DEFAULT_BACKOFF_MULTIPLIER = Constants.DEFAULT_BACKOFF_MULTIPLIER;

  private final Long period;
  private final Long maxPeriod;
  private final Integer maxAttempts;
  private int retries;
  private final Double backoffMultiplier;

  public FeignApiRetryer(@Nullable Long period, @Nullable Long maxPeriod, @Nullable Integer maxAttempts, @Nullable Double backoffMultiplier) {
    this.period = (period == null) ? DEFAULT_PERIOD : period;
    this.maxPeriod = (maxPeriod == null) ? DEFAULT_MAX_PERIOD : maxPeriod;
    this.maxAttempts = (maxAttempts == null) ? DEFAULT_MAX_ATTEMPTS : maxAttempts;
    this.backoffMultiplier = (backoffMultiplier == null) ? DEFAULT_BACKOFF_MULTIPLIER : backoffMultiplier;
  }

  /**
   * Straight up ripped from Retryer.Default because nextMaxInterval is not polymorphic and hence not overridable.
   * Otherwise would have invoked super.continueOrPropagate(e, lambda...)
   * <p>
   * TODO: Contribute back to Feign OSS
   *
   * @param e RetryableException containing details of the retryable error
   */
  @Override
  public void continueOrPropagate(RetryableException e) {
    if (this.retries++ >= this.maxAttempts) {
      throw e;
    } else {
      LOG.info("Retrying API request: Attempt ({})", this.retries);
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
        interval = this.nextMaxInterval(period, backoffMultiplier, retries, maxPeriod);
      }
      try {
        Thread.sleep(interval);
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
      }
    }
  }

  /**
   * Implements Spring's ExponentialBackOffPolicy with some modifications
   *
   * @return An interval that is set by the backOffMultiplier
   */
  private long nextMaxInterval(final Long period, final Double backoffMultiplier, final int retries, final Long maxPeriod) {
    long interval = (long) ((double) period * Math.pow(backoffMultiplier, (retries - 1)));
    return interval > maxPeriod ? maxPeriod : interval;
  }

  @Override
  public Retryer clone() {
    return new FeignApiRetryer(period, maxPeriod, maxAttempts, backoffMultiplier);
  }
}
