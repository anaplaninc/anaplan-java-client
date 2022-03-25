package com.anaplan.client.exceptions;

public class ListNotFoundException  extends RuntimeException {

  private static final String msg = "List not found for Model-ID: ";

  public ListNotFoundException(String modelId) {
    super(msg + modelId);
  }

  public ListNotFoundException(String modelId, Throwable t) {
    super(msg + modelId, t);
  }
}
