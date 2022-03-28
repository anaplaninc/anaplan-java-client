package com.anaplan.client.exceptions;

public class ImportsNotFoundException extends RuntimeException {

  public ImportsNotFoundException(String modelId, Throwable t) {
    super("Imports not found for Model-ID=" + modelId, t);
  }
}
