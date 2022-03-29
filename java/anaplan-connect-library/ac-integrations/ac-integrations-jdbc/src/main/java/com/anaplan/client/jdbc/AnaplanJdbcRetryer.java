package com.anaplan.client.jdbc;

import com.anaplan.client.transport.retryer.AnaplanRetryer;

/**
 * Anaplan retryer that emulates Spring's BackoffExponentialPolicy for setting intervals/periods
 * between attempts.
 */
public class AnaplanJdbcRetryer implements AnaplanRetryer {

  private final Long period;
  private final Long maxPeriod;
  private final Double backoffMultiplier;

  public AnaplanJdbcRetryer(Long period, Long maxPeriod, Double backoffMultiplier) {
    this.period = (period == null) ? AnaplanRetryer.DEFAULT_PERIOD : period;
    this.maxPeriod = (maxPeriod == null) ? AnaplanRetryer.DEFAULT_MAX_PERIOD : maxPeriod;
    this.backoffMultiplier =
        (backoffMultiplier == null) ? AnaplanRetryer.DEFAULT_BACKOFF_MULTIPLIER : backoffMultiplier;
  }

  /**
   * Implements Spring's ExponentialBackOffPolicy with some modifications
   *
   * @return An interval that is set by the backOffMultiplier
   */
  @Override
  public long nextMaxInterval(int noOfAttempts) {
    long interval = (long) ((double) this.period * Math
        .pow(backoffMultiplier, noOfAttempts));
    return interval > this.maxPeriod ? this.maxPeriod : interval;
  }

}
