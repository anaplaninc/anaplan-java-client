package com.anaplan.client.ex;

public class AnaplanRetryableException extends Exception {
    /**
     * Create an exception with the specified message.
     */
    public AnaplanRetryableException(String message) {
        super(message);
    }

    /**
     * Create an exception with the specified message and cause.
     */
    public AnaplanRetryableException(String message, Throwable cause) {
        super(message, cause);
    }
}

