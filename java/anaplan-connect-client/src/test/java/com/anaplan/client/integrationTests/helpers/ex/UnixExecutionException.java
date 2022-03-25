package com.anaplan.client.integrationTests.helpers.ex;

/**
 * exception for when we execute unix commands in tests.
 */
public class UnixExecutionException extends RuntimeException {

  /**
   * Create an exception with the specified message.
   */
  public UnixExecutionException(String message) {
    super(message);
  }

  /**
   * Create an exception with the specified message and cause.
   */
  public UnixExecutionException(String message, Throwable cause) {
    super(message, cause);
  }
}