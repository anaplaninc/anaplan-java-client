package com.anaplan.client.transport.retryer;

/**
 * Anaplan retryer that emulates Spring's BackoffExponentialPolicy for setting intervals/periods
 * between attempts.
 */

import com.anaplan.client.Constants;

public interface AnaplanRetryer {
    long DEFAULT_PERIOD = Constants.MIN_RETRY_TIMEOUT_SECS * 1000L;
    long DEFAULT_MAX_PERIOD = Constants.MAX_RETRY_TIMEOUT_SECS * 1000L;
    int DEFAULT_MAX_ATTEMPTS = Constants.MIN_RETRY_COUNT;
    double DEFAULT_BACKOFF_MULTIPLIER = Constants.DEFAULT_BACKOFF_MULTIPLIER;

    long nextMaxInterval(int noOfAttempts);

}
