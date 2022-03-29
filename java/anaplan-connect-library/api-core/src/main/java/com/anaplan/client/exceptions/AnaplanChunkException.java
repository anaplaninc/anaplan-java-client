package com.anaplan.client.exceptions;

public class AnaplanChunkException extends RuntimeException {

  public AnaplanChunkException(final String msg, final Exception ex) {
    super(msg, ex);
  }
}
