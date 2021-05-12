package com.anaplan.client.integrationTests.helpers.ex;

/**
 * exception for when we execute unix commands in tests.
 */
public class FileComparisonException extends RuntimeException {

  /**
   * Create an exception with the specified message.
   */
  public FileComparisonException(String message) {
    super(message);
  }

  /**
   * Create an exception with the specified message and cause.
   */
  public FileComparisonException(String message, Throwable cause) {
    super(message, cause);
  }
}