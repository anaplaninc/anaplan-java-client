package com.anaplan.client;

/**
 * AnaplanCryptoException is thrown during execution of CryptoUtil
 */
public class AnaplanCyptoException extends RuntimeException {
  public AnaplanCyptoException(String message) {
    super(message);
  }

  public AnaplanCyptoException(String message, final Throwable cause) {
    super(message, cause);
  }
}
