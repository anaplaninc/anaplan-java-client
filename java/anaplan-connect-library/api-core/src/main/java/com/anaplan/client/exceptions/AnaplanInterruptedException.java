package com.anaplan.client.exceptions;

public class AnaplanInterruptedException extends RuntimeException {

  public AnaplanInterruptedException(final String msg, final InterruptedException interruptedException) {
    super(msg, interruptedException);
  }
}
