package com.anaplan.client.exceptions;

import java.io.IOException;

public class AnaplanIOException extends RuntimeException {

  public AnaplanIOException(final IOException ioException) {
    super(ioException);
  }

}
