package com.anaplan.client.exceptions;

public class ListNotFoundException  extends RuntimeException {

  private static final String MSG = "List not found for Model-ID: ";

  public ListNotFoundException(String modelId) {
    super(MSG + modelId);
  }

  public ListNotFoundException(String modelId, Throwable t) {
    super(MSG + modelId, t);
  }
}
